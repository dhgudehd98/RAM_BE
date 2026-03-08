package com.sh.Ram.product.service;

import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Brand;
import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.dto.RegisterProductDto;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;

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
    public Map<String, String> regist(RegisterProductDto registerProductDto, MultipartFile image, Long memberId) {
        Map<String, String> response = new HashMap<>();

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException("존재하지 않는 회원입니다. 로그인을 먼저 진행 후 상품을 등록해주세요."));
        String imageUrl = "imageURL";

        //! 상품 -> 브랜드에 대한 부분도 해야되는데 이걸 어떻게 해야되지 .. 미리 브랜드를 등록을 해놔야되나 DB + ES에다가
        Product product = new Product(
                member,
                registerProductDto.getProductName(),
                registerProductDto.getDescription(),
                registerProductDto.getPrice(),
                // 카테고리 , 브랜드 추가해서 상품 만들기
                imageUrl
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
}