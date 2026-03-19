package com.sh.Ram.redis;

import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.repository.ProductRepository;
import com.sh.Ram.redis.auction.dto.AuctionRealtimeDto;
import com.sh.Ram.redis.auction.service.AuctionRedisCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.access.expiration=3600000",
        "jwt.refresh.expiration=86400000"
})
public class RedisPerformanceTest {
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionRedisCacheService auctionRedisCacheService;

    @Test
    @DisplayName("Redis vs DB 조회 속도 비교 테스트")
    void redisVsDbPerformanceTest() {

        // 1. 초기화
        bidRepository.deleteAll();
        auctionRepository.deleteAll();

        Product product = productRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product 없음"));

        Member member = memberRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member 없음"));

        // 2. Auction 생성
        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartPrice(10_000);
        auction.setCurrentPrice(null);
        auction.setStartDate(LocalDate.now());
        auction.setEndDate(LocalDate.now().plusDays(1));
        auction.setAuctionStatus(AuctionStatus.PROGRESS);

        Auction saved = auctionRepository.save(auction);

        // 3. 입찰 → Redis 캐싱 발생
        bidService.submitBid(saved.getId(), member.getId(), 10_000);

        // 4. Redis warm-up (캐시 적재 보장)
        Optional<AuctionRealtimeDto> warmUp =
                auctionRedisCacheService.getAuctionState(saved.getId());

        assertTrue(warmUp.isPresent(), "Redis 캐시가 존재해야 한다.");

        // =========================
        // 5. DB 조회 시간 측정
        // =========================
        long dbStart = System.nanoTime();

        Auction dbAuction = auctionRepository.findById(saved.getId())
                .orElseThrow();

        long dbEnd = System.nanoTime();

        // =========================
        // 6. Redis 조회 시간 측정
        // =========================
        long redisStart = System.nanoTime();

        AuctionRealtimeDto redisState =
                auctionRedisCacheService.getAuctionState(saved.getId())
                        .orElseThrow();

        long redisEnd = System.nanoTime();

        long dbTime = dbEnd - dbStart;
        long redisTime = redisEnd - redisStart;

        System.out.println("DB 조회 시간 (ns): " + dbTime);
        System.out.println("Redis 조회 시간 (ns): " + redisTime);

        // 7. 기본 검증
        assertNotNull(dbAuction);
        assertNotNull(redisState);

        // 8. Redis가 더 빠른지 확인 (참고용)
        assertTrue(redisTime < dbTime,
                "Redis가 DB보다 빨라야 한다.");
    }
}
