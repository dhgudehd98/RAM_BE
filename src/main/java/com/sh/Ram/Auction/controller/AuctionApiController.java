package com.sh.Ram.Auction.controller;

import com.sh.Ram.Auction.dto.AuctionCreateRequest;
import com.sh.Ram.Auction.dto.AuctionDto;
import com.sh.Ram.Auction.dto.AuctionUpdateRequest;
import com.sh.Ram.Auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auction")
public class AuctionApiController {

    private final AuctionService auctionService;

    /**
     * 경매 상품 상세 조회 API
     */
    @GetMapping("/{auctionId}")
    public AuctionDto getAuction(
            @PathVariable Long auctionId
    ) {
        return auctionService.getAuction(auctionId);
    }

    /**
     * 경매 상품 리스트 조회 API
     */

    @GetMapping("/list")
    public Page<AuctionDto> getAuctionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status
    ) {
        return auctionService.auctionList(page, status);
    }

    /**
     * 경매 상품 생성 API
     */
    @PostMapping("/create")
    public AuctionDto createAuction(@RequestBody AuctionCreateRequest request) {
        return auctionService.createAuction(request);
    }

    /**
     * 경매 상품 수정 API
     */
    @PutMapping("/{auctionId}")
    public AuctionDto updateAuction(
            @PathVariable Long auctionId,
            @RequestBody AuctionUpdateRequest request
    ) {
       return auctionService.updatedAuction(auctionId, request);
    }


    /**
     * 경매 상품 삭제 API
     */
    @DeleteMapping("/{auctionId}")
    public Map<String, String> deleteAuction(
            @PathVariable Long auctionId
    ) {
        return auctionService.deleteAuction(auctionId);
    }
}
