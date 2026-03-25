package com.sh.Ram.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.aws.service.S3Service;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.*;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.dto.AiProductDto;
import com.sh.Ram.product.dto.LlmResponseDto;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.dto.RegisterProductDto;
import com.sh.Ram.product.repository.ProductRepository;
import com.sh.Ram.wishList.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final WishListRepository wishListRepoistory;
    private final WebClient webClient;
    private final S3Service s3service;

    public Page<ProductDto> findAllProduct(String sort, int page) {

        Pageable pageable;

        // 정렬 조건에 따라서 Paging 분류 방법 다르게
        if (sort != null && !sort.isEmpty()) {
            pageable = PageRequest.of(page, 10, Sort.by(sort).descending());
        }
        else {
            pageable = PageRequest.of(page, 10, Sort.by("name").descending());
        }

        return productRepository.findAll(pageable)
                .map(ProductDto::from);

    }

    @Transactional
    public Map<String, String> regist(RegisterProductDto registerProductDto, MultipartFile image, Long memberId) throws IOException {
        Map<String, String> response = new HashMap<>();

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException("존재하지 않는 회원입니다. 로그인을 먼저 진행 후 상품을 등록해주세요."));
        String imageUrl = s3service.imageUpload(image);

        //! 상품 -> 브랜드에 대한 부분도 해야되는데 이걸 어떻게 해야되지 .. 미리 브랜드를 등록을 해놔야되나 DB + ES에다가
        Product product = new Product(
                member,
                registerProductDto.getProductName(),
                registerProductDto.getDescription(),
                registerProductDto.getPrice(),
                // 카테고리 , 브랜드 추가해서 상품 만들기
                imageUrl,
                false
        );

        productRepository.save(product);

        // 경매를 바로 올릴 상품이라면 -> Auction Entity에 저장
        //! ProductService에서 처리할 부분이 아닌 AuctionService에서 처리하도록 로직 변경 처리 필요
        if (registerProductDto.getIsAuction()) {
            Auction auction = new Auction(
                    product,
                    registerProductDto.getPrice(),
                    registerProductDto.getAuctionStartDate(),
                    registerProductDto.getAuctionEndDate(),
                    AuctionStatus.PENDING
            );

            auctionRepository.save(auction);
        }
        response.put("result", "Y");
        response.put("message", "상품이 정상적으로 등록되었습니다.");

        return response;

    }

    public Mono<AiProductDto> getProductInfo(MultipartFile image) {

        // Spring Tomcat에서는 이미지를 받을 때, MultiPartFile로 이미지를 받아야되고 , WebFLux는 이미지 파일을 받을 때 FilePart로 데이터를 받아야 함.
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", image.getResource());
        return webClient.post()
                .uri("http://localhost:8081/chat/image")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(LlmResponseDto.class)
                .map(llmResponseDto -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(llmResponseDto.getResponse(), AiProductDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("응답 파싱 실패 ");
                    }
                });

    }

    public ProductDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId).get();
        return new ProductDto(product);
    }

    @Transactional
    public Map<String , Object> addWishList(Long productId, Long memberId) {

        Member member = memberRepository.getReferenceById(memberId);
        Product product = productRepository.getReferenceById(productId);

        Optional<WishList> existing = wishListRepoistory.findByMemberAndProduct(member, product);

        /**
         * WishList -> 찜 추가되면 true 리턴 , 찜 삭제 false 리턴
         *
         */
        if (existing.isPresent()) {
            wishListRepoistory.delete(existing.get());
        } else {
            wishListRepoistory.save(new WishList(member, product));
        }

        return Map.of(
                "isWished", existing.isEmpty(),
                "wishListSize", wishListRepoistory.countByProductId(productId)
        );
    }

    public List<ProductDto> findAllProduct(Long lastId, String sort) {
        return productRepository.findAllProduct(lastId, sort)
                .stream()
                .map(ProductDto::from)
                .collect(Collectors.toList());
    }
}