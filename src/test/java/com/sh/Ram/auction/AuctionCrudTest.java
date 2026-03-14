package com.sh.Ram.auction;

import com.sh.Ram.Auction.dto.AuctionCreateRequest;
import com.sh.Ram.Auction.dto.AuctionDto;
import com.sh.Ram.Auction.dto.AuctionUpdateRequest;
import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.Auction.service.AuctionService;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuctionCrudTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Test
    void 경매_CRUD_테스트() {

        System.out.println("===== TEST START =====");

        // 1️⃣ 상품 조회
        List<Product> products = productRepository.findAll();

        System.out.println("product count = " + products.size());

        assertTrue(products.size() >= 2);

        Product product1 = products.get(0);
        Product product2 = products.get(1);

        System.out.println("product1 id = " + product1.getId());
        System.out.println("product2 id = " + product2.getId());

        auctionRepository.deleteAll();

        // 2️⃣ 경매 생성 1
        AuctionCreateRequest request1 = new AuctionCreateRequest();
        request1.setProductId(product1.getId());
        request1.setStartPrice(10000);
        request1.setStartDate(LocalDate.now());
        request1.setEndDate(LocalDate.now().plusDays(3));

        AuctionDto auction1 = auctionService.createAuction(request1);

        System.out.println("created auction1 id = " + auction1.getId());


        // 3️⃣ 경매 생성 2
        AuctionCreateRequest request2 = new AuctionCreateRequest();
        request2.setProductId(product2.getId());
        request2.setStartPrice(20000);
        request2.setStartDate(LocalDate.now());
        request2.setEndDate(LocalDate.now().plusDays(5));

        AuctionDto auction2 = auctionService.createAuction(request2);

        System.out.println("created auction2 id = " + auction2.getId());


        // 4️⃣ 상세 조회
        AuctionDto detail = auctionService.getAuction(auction1.getId());

        System.out.println("detail auction id = " + detail.getId());
        System.out.println("detail start price = " + detail.getStartPrice());

        assertEquals(10000, detail.getStartPrice());


        // 5️⃣ 리스트 조회
        Page<AuctionDto> page = auctionService.auctionList(0, null);

        System.out.println("===== AUCTION PAGE =====");
        System.out.println("total elements = " + page.getTotalElements());
        System.out.println("total pages = " + page.getTotalPages());

        for (AuctionDto dto : page.getContent()) {
            System.out.println(
                    "auctionId=" + dto.getId() +
                            ", productId=" + dto.getProductId() +
                            ", startPrice=" + dto.getStartPrice() +
                            ", currentPrice=" + dto.getCurrentPrice()
            );
        }

        assertNotNull(page);


        // 6️⃣ 수정
        AuctionUpdateRequest update = new AuctionUpdateRequest();
        update.setStartPrice(30000);
        update.setStartDate(LocalDate.now());
        update.setEndDate(LocalDate.now().plusDays(7));

        AuctionDto updated = auctionService.updatedAuction(auction1.getId(), update);

        System.out.println("updated start price = " + updated.getStartPrice());

        assertEquals(30000, updated.getStartPrice());


        // 7️⃣ 삭제
        Map<String, String> result = auctionService.deleteAuction(auction2.getId());

        System.out.println("delete result = " + result);

        assertEquals("Y", result.get("result"));

        System.out.println("===== TEST END =====");

    }

    @Test
    void 경매_중복_방지_테스트() {

        System.out.println("=== AUCTION DUPLICATE TEST START ===");

        // 상품 조회
        Product product = productRepository.findAll().get(0);

        System.out.println("product id = " + product.getId());

        bidRepository.deleteAll();
        auctionRepository.deleteAll();

        // 첫 경매 생성
        AuctionCreateRequest request = new AuctionCreateRequest();
        request.setProductId(product.getId());
        request.setStartPrice(10000);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));

        AuctionDto auction = auctionService.createAuction(request);

        System.out.println("auction id = " + auction.getId());

        AuctionCreateRequest request2 = new AuctionCreateRequest();
        request2.setProductId(product.getId());
        request2.setStartPrice(20000);
        request2.setStartDate(LocalDate.now());
        request2.setEndDate(LocalDate.now().plusDays(2));

        // 예외 발생 확인
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> auctionService.createAuction(request2)
        );

        System.out.println("exception msg = " + exception.getMessage());


        System.out.println("=== AUCTION DUPLICATE TEST END ===");
    }

}
