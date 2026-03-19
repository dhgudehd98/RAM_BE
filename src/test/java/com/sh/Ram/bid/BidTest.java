package com.sh.Ram.bid;


import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.bid.dto.BidListDto;
import com.sh.Ram.bid.dto.BidResponseDto;
import com.sh.Ram.bid.dto.HighestBidDto;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class BidTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 입찰_기능_테스트() {

        System.out.println("=== BID TEST START ===");

        // Product 조회
        List<Product> products = productRepository.findAll();

        assertTrue(products.size() >= 1);

        Product product = products.get(0);

        System.out.println("product id = " + product.getId());

        auctionRepository.deleteAll();

        // Auction 생성 (PROGRESS)
        Auction auction = new Auction(
                product,
                50000,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                AuctionStatus.PROGRESS
        );

        auction.setCurrentPrice(50000);

        auctionRepository.save(auction);

        System.out.println("auction id = " + auction.getId());

        // Member 조회
        Long sellerId = product.getMember().getId();

        List<Long> memberIds = memberRepository.findAll()
                .stream()
                .map(m -> m.getId())
                .toList();

        assertTrue(memberIds.size() >= 2);

        Long bidder1 = memberIds.get(0);
        Long bidder2 = memberIds.get(1);

        // 입찰 실행
        BidResponseDto bid1 = bidService.submitBid(auction.getId(), bidder1, 10000);

        System.out.println("bid1 price = " + bid1.getCurrentPrice());

        BidResponseDto bid2 = bidService.submitBid(auction.getId(), bidder2, 10000);

        // 입찰 목록 조회
        Page<BidListDto> bidList =
                bidService.getBidList(auction.getId(), PageRequest.of(0, 5));

        System.out.println("=== BID LIST ===");

        bidList.getContent().forEach(b ->
                System.out.println(
                    "bidder = " + b.getBidderNickName()
                    + ", price = " + b.getBidPrice()
                    + ", time = " + b.getBidTime()
                )
        );

        assertTrue(bidList.getContent().size() >= 2);

        // 최고 입찰 조회
        HighestBidDto highestBid = bidService.getHighestBid(auction.getId());

        System.out.println("highest bidder =  " + highestBid.getNickname());
        System.out.println("highest price =  " + highestBid.getPrice());

        System.out.println("=== BID TEST END ===");
    }
}
