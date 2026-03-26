package com.sh.Ram.batch;

import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.accountHistory.AccountHistoryRepository;
import com.sh.Ram.adminAccount.repository.AdminAccountRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.auctionResult.repository.AuctionResultRepository;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.entity.*;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.access.expiration=3600000",
        "jwt.refresh.expiration=86400000"
})
@Rollback(false)
public class BatchIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionResultRepository auctionResultRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private AccountHistoryRepository accountHistoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job auctionEndBulkJob;

    @Autowired
    private Job auctionResultJob;

    @Autowired
    private Job settlementJob;

    @Test
    void 배치_전체_흐름_검증() throws Exception {

        // 1️⃣ 상품
        Product product = productRepository.findAll().get(0);

        accountHistoryRepository.deleteAll();
        auctionResultRepository.deleteAll();
        bidRepository.deleteAll();
        auctionRepository.deleteAll();

        Auction auction = new Auction(
                product,
                50000,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                AuctionStatus.PROGRESS
        );

        auction.setCurrentPrice(null);
        auctionRepository.save(auction);

        Long auctionId = auction.getId();

        // 2️⃣ 입찰자
        List<Member> members = memberRepository.findAll();
        Long bidder1 = members.get(0).getId();
        Long bidder2 = members.get(1).getId();

        // 초기 잔액 저장
        Account buyerBefore =
                accountRepository.findByMemberId(bidder1);

        Account sellerBefore =
                accountRepository.findByMemberId(product.getMember().getId());

        AdminAccount adminBefore =
                adminAccountRepository.findById(1L);

        Long buyerBalanceBefore = buyerBefore.getAccountBalance();
        Long sellerBalanceBefore = sellerBefore.getAccountBalance();
        Long adminBalanceBefore = adminBefore.getBalance();

        // 3️⃣ 입찰
        bidService.submitBid(auctionId, bidder1, 50000);
        bidService.submitBid(auctionId, bidder2, 51000);

        // 4️⃣ 종료
        jobLauncher.run(
                auctionEndBulkJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        auction.setAuctionStatus(AuctionStatus.CLOSED);
        auctionRepository.save(auction);

        auctionRepository.flush();

        // ================= IN =================
        jobLauncher.run(
                auctionResultJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        // 5️⃣ 결과 검증
        AuctionResult result = auctionResultRepository.findByAuctionId(auctionId)
                        .orElseThrow();

        assertNotNull(result);
        assertEquals("SUCCESS", result.getResultStatus());
        assertEquals("HELD", result.getSettlementStatus());

        // buyer 확인
        Account buyerAfter =
                accountRepository.findByMemberId(result.getBuyerId());

        AdminAccount adminAfter =
                adminAccountRepository.findById(1L);

        // ✔ buyer 돈 감소
        assertTrue(buyerAfter.getAccountBalance() < buyerBalanceBefore);

        // ✔ admin 돈 증가
        assertTrue(adminAfter.getBalance() > adminBalanceBefore);

        // ✔ history 생성 (OUT)
        List<AccountHistory> histories = accountHistoryRepository.findAll();
        assertTrue(histories.size() >= 1);

        boolean hasOut = histories.stream()
                .anyMatch(h -> "OUT".equals(h.getType()));

        assertTrue(hasOut);

        // ================= OUT =================
        // 테스트용 시간 조정
        result.setResultTime(LocalDateTime.now().minusDays(1));

        auctionResultRepository.save(result);

        jobLauncher.run(
                settlementJob,
                new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters()
        );

        // 6️⃣ OUT 검증
        AuctionResult updated =
                auctionResultRepository.findById(result.getId()).orElseThrow();

        assertEquals("SETTLED", updated.getSettlementStatus());

        Account sellerAfter =
                accountRepository.findByMemberId(product.getMember().getId());

        AdminAccount adminFinal =
                adminAccountRepository.findById(1L);

        // ✔ seller 돈 증가
        assertTrue(sellerAfter.getAccountBalance() > sellerBalanceBefore);

        // ✔ admin 돈 감소
        assertTrue(adminFinal.getBalance() < adminAfter.getBalance());

        // ✔ history IN 존재
        List<AccountHistory> histories2 = accountHistoryRepository.findAll();

        boolean hasIn = histories2.stream()
                .anyMatch(h -> "IN".equals(h.getType()));

        assertTrue(hasIn);
    }
}
