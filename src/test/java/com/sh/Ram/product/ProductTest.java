package com.sh.Ram.product;


import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class ProductTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    @DisplayName("Find All Product")
    void findAllProduct() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        // when
        Page<Product> productPage = productRepository.findAll(pageable);

        // then
        List<Product> products = productPage.getContent();
        System.out.println("=== 가격 내림차순 ===");
        products.forEach(p -> System.out.println(p.getName() + " : " + p.getPrice()));

        System.out.println("전체 상품 수 : " + productPage.getTotalElements());
        System.out.println("전체 페이지 수 : " + productPage.getTotalPages());
        System.out.println("현재 페이지 상품 수 : " + productPage.getContent().size());

        Assertions.assertThat(products.get(0).getPrice())
                .isGreaterThanOrEqualTo(products.get(1).getPrice());
    }

    @Test
    @DisplayName("상품 경매에 등록되어 있는지 확인하기 ")
    void findProductGetAuctionStatus () {
        /**
         * 상품이 경매에 등록되어 있는지 확인하는 로직 작성
         * 1. Keyword를 통해 상품을 검색 했을 때 ES에 등록되어 있는 상품 추출
         * 2. productDocument(ES에 저장된 데이터 리턴)에 저장되어 있는 productId를 통해 Auction 테이블에 저장되어 있는지 확인
         * 3. 저장되어 있으면 Client에 Return 할 productDto에 값 설정 , 안되어 이씅면 ProductDto 에 null 값 지정
         */
        String keyword = "아이앱";
        // ES에 저장된 데이터 가져오기
        List<ProductDocument> documents = productDocumentRepository.searchByKeyword(keyword);

        List<Long> productIds = documents.stream()
                .map(document -> Long.parseLong(document.getId()))
                .collect(Collectors.toList());

        /**
         * 저장형태
         * key : productId -> value : AuctionStatus
         */
        Map<Long, AuctionStatus> auctionMap = auctionRepository.findAuctionStatusByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        raw -> (Long) raw[0],
                        raw -> (AuctionStatus) raw[1]
                ));

        //ProductDto에 반환
        List<ProductDto> productDtoList = documents.stream()
                .map(document -> {
                    ProductDto dto = new ProductDto(document);
                    dto.setAuctionStatus(auctionMap.get(Long.parseLong(document.getId())));

                    return dto;
                })
                .collect(Collectors.toList());

        // DB에 저장되어 있는 아이앱으로 검색할 떄 나오는 수 : 34 , 아이앱으로 검색했을 때 ES에서 호출된 데이터가 34개인지 확인
        Assertions.assertThat(productDtoList.size()).isEqualTo(34);

        // 경매에 저장되어 있는 상품의 ID는 1이고 , 나머지는 모두 등록되어 있지 않아서 정상적으로 AuctionStatus에 대한 값을 추출하는지 확인
        for (ProductDto dto : productDtoList) {
            if (dto.getId() == 1L) {
                AuctionStatus status = dto.getAuctionStatus();
                String string = status.toString();

                Assertions.assertThat(string).isEqualTo("PROGRESS");
            }
        }
    }
}