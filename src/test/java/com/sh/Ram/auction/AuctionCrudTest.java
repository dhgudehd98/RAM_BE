package com.sh.Ram.auction;

import com.sh.Ram.auction.dto.AuctionCreateRequest;
import com.sh.Ram.auction.dto.AuctionDto;
import com.sh.Ram.auction.dto.AuctionUpdateRequest;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.auction.service.AuctionService;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.common.exception.product.ProductException;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Bid;
import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.notification.repository.NotificationRepository;
import com.sh.Ram.notification.service.NotificationService;
import com.sh.Ram.product.repository.ProductRepository;
import com.sh.Ram.redis.auction.dto.AuctionRealtimeDto;
import com.sh.Ram.redis.auction.service.AuctionRedisCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.access.expiration=3600000",
        "jwt.refresh.expiration=86400000"
})
public class AuctionCrudTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BidService bidService;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private AuctionRedisCacheService auctionRedisCacheService;

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
        Page<AuctionDto> page = auctionService.auctionList(0, null,null);

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

//    @Test
//    @DisplayName("동시_입찰_테스트 - 한 명만 성공")
//    void 동시_입찰_테스트() throws Exception {
//
//        // 기존 데이터 정리
//        bidRepository.deleteAll();
//        auctionRepository.deleteAll();
//        bidRepository.flush();
//        auctionRepository.flush();
//
//        // 상품 가져오기
//        Product product = productRepository.findAll().stream()
//                .findFirst()
//                .orElseThrow(() -> new ProductException("상품이 없습니다."));
//
//        // 경매 생성
//        Auction auction = new Auction();
//        auction.setProduct(product);
//        auction.setStartPrice(10_000);
//
//        // ⭐ 핵심 수정
//        auction.setCurrentPrice(null);
//
//        auction.setAuctionStatus(AuctionStatus.PROGRESS);
//        auction.setStartDate(LocalDate.now());
//        auction.setEndDate(LocalDate.now().plusDays(3));
//
//        auction = auctionRepository.saveAndFlush(auction);
//
//        // member 4명 조회
//        List<Member> members = memberRepository.findAll().stream()
//                .limit(4)
//                .toList();
//
//        int threadCount = members.size();
//
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        // ⭐ 동시에 시작
//        CyclicBarrier barrier = new CyclicBarrier(threadCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        Long auctionId = auction.getId();
//
//        for (Member member : members) {
//
//            Long memberId = member.getId();
//
//            executorService.submit(() -> {
//                try {
//                    barrier.await(); // ⭐ 진짜 동시 시작
//
//                    bidService.submitBid(
//                            auctionId,
//                            memberId,
//                            10_000
//                    );
//
//                    successCount.incrementAndGet();
//
//                } catch (Exception e) {
//                    failCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(5, TimeUnit.SECONDS);
//        executorService.shutdown();
//
//        // 검증
//        System.out.println("성공 : " + successCount.get());
//        System.out.println("실패 : " + failCount.get());
//
//        assertEquals(1, successCount.get());
//        assertEquals(threadCount - 1, failCount.get());
//
//        // DB 검증
//        Auction result = auctionRepository.findById(auctionId).orElseThrow();
//
//        System.out.println("최종 가격 : " + result.getCurrentPrice());
//
//        assertEquals(10_000, result.getCurrentPrice());
//
//        // Bid도 검증 (강추)
//        List<Bid> bids = bidRepository.findAll();
//        assertEquals(1, bids.size());
//        assertEquals(10_000, bids.get(0).getBidPrice());
//    }
//
//    @Test
//    @DisplayName("입찰 성공 후 Redis 실시간 상태 캐싱 테스트")
//    void redisCachingAfterBidTest() {
//        // 1. 기존 데이터 정리
//        bidRepository.deleteAll();
//        auctionRepository.deleteAll();
//
//        // 2. 기존 Product 1개 조회
//        Product product = productRepository.findAll().stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("테스트용 Product가 없습니다."));
//
//        // 3. 기존 Member 1명 조회
//        Member member = memberRepository.findAll().stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("테스트용 Member가 없습니다."));
//
//        // 4. 경매 생성 (첫 입찰 테스트를 위해 currentPrice = null)
//        Auction auction = new Auction();
//        auction.setProduct(product);
//        auction.setStartPrice(10_000);
//        auction.setCurrentPrice(null);
//        auction.setStartDate(LocalDate.now());
//        auction.setEndDate(LocalDate.now().plusDays(1));
//        auction.setAuctionStatus(AuctionStatus.PROGRESS);
//
//        Auction savedAuction = auctionRepository.save(auction);
//
//        // 5. 입찰 호출 (첫 입찰은 시작가)
//        bidService.submitBid(savedAuction.getId(), member.getId(), 10_000);
//
//        // 6. Redis 조회
//        Optional<AuctionRealtimeDto> redisStateOpt =
//                auctionRedisCacheService.getAuctionState(savedAuction.getId());
//
//        assertTrue(redisStateOpt.isPresent(), "Redis 캐시가 저장되어 있어야 한다.");
//
//        AuctionRealtimeDto redisState = redisStateOpt.get();
//
//        // 7. 검증
//        assertEquals(10_000, redisState.getCurrentPrice());
//        assertEquals(member.getNickname(), redisState.getHighestBidderNickname());
//        assertNotNull(redisState.getLastBidTime());
//        assertEquals(10_500, redisState.getNextBidPrice()); // 10,000 미만 아니고 50,000 미만 → +500
//        assertEquals(AuctionStatus.PROGRESS.name(), redisState.getAuctionStatus());
//    }

}
