package com.sh.Ram.Auction.service;

import com.sh.Ram.Auction.dto.AuctionCreateRequest;
import com.sh.Ram.Auction.dto.AuctionDto;
import com.sh.Ram.Auction.dto.AuctionUpdateRequest;
import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.common.exception.auction.AuctionException;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Product;
import com.sh.Ram.entity.WishList;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.enums.NotificationType;
import com.sh.Ram.notification.service.NotificationService;
import com.sh.Ram.product.repository.ProductRepository;
import com.sh.Ram.wishList.repository.WishListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;

    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AuctionDto> auctionList(int page, String status) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("startDate").descending());
        Page<Auction> auctionList ;

        // status에 대한 부분이 없으면 전체 조회
        if (status == null) {
            auctionList = auctionRepository.findAllWithProduct(pageable);
        }else{
            AuctionStatus auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
            auctionList = auctionRepository.findAuctionByStatus(pageable, auctionStatus);
        }

        return auctionList.map(AuctionDto::new);
    }

    @Transactional(readOnly = true)
    public AuctionDto getAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException("경매를 찾을 수 없습니다."));

        return new AuctionDto(auction);
    }

    @Transactional
    public AuctionDto createAuction(AuctionCreateRequest request) {

        // 경매 등록조회 (중복 경매 방지)
        boolean exists = auctionRepository.existsActiveAuctionByProductId(
                request.getProductId(),
                AuctionStatus.CLOSED
        );

        if (exists) {
            throw new AuctionException("이미 진행 중인 경매가 존재합니다.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AuctionException("경매를 찾을 수 없습니다."));

        Auction auction = new Auction();
        auction.setProduct(product);
        auction.setStartPrice(request.getStartPrice());
        auction.setCurrentPrice(request.getStartPrice());
        auction.setStartDate(request.getStartDate());
        auction.setEndDate(request.getEndDate());
        auction.setAuctionStatus(AuctionStatus.PENDING);

        Auction saved = auctionRepository.save(auction);

        return new AuctionDto(saved);
    }

    @Transactional
    public Map<String, String> deleteAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException("경매를 찾을 수 없습니다."));

        if (auction.getAuctionStatus() != AuctionStatus.PENDING) {
            throw new AuctionException("진행중이거나 종료된 경매는 삭제할 수 없습니다.");
        }

        auctionRepository.delete(auction);

        return Map.of(
                "result", "Y",
                "msg", "경매가 삭제되었습니다."
        );
    }

    @Transactional
    public AuctionDto updatedAuction(Long auctionId, AuctionUpdateRequest request) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException("경매를 찾을 수 없습니다."));

        if (auction.getAuctionStatus() != AuctionStatus.PENDING) {
            throw new AuctionException("진행중이거나 종료된 경매는 수정할 수 없습니다.");
        }

        auction.setStartPrice(request.getStartPrice());
        auction.setStartDate(request.getStartDate());
        auction.setEndDate(request.getEndDate());

        return new AuctionDto(auction);
    }

    public void registAuction(Long productId) {
        List<WishList> wishLists = wishListRepository.findByProductId(productId);

        wishLists.forEach(wishList -> {
            notificationService.send(
                    wishList.getMember().getId(),
                    "찜한 상품이 경매 상품으로 등록돼었어요. 확인해보세요.",
                    NotificationType.WISHLIST
            );
        });

    }
}