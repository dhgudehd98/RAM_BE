package com.sh.Ram.bid.controller;


import com.sh.Ram.bid.dto.BidListDto;
import com.sh.Ram.bid.dto.BidRequestDto;
import com.sh.Ram.bid.dto.BidResponseDto;
import com.sh.Ram.bid.dto.HighestBidDto;
import com.sh.Ram.bid.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class BidController {

    private final BidService bidService;

    /**
     * 입찰 API
     */
//    @PostMapping("/{auctionId}/bids")
//    public BidResponseDto submitBid(
//            @PathVariable Long auctionId,
//            @RequestBody BidRequestDto request
//    ) {
//        return bidService.submitBid(auctionId,
//                request.getMemberId(),
//                request.getExpectedPrice()
//        );
//    }

    @PostMapping("/{auctionId}/bids")
    public BidResponseDto submitBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequestDto request
    ){
        return bidService.submitBid(auctionId, request);
    }

    /**
     * 입찰 목록 조회 API (페이징당 5개)
     */
    @GetMapping("/{auctionId}/bids")
    public Page<BidListDto> getBidList(
            @PathVariable Long auctionId,
            @PageableDefault(size = 5, sort = "bidTime", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return bidService.getBidList(auctionId, pageable);
    }

    /**
     * 최고 입찰 조회 API
     */
    @GetMapping("/{auctionId}/bids/highest")
    public HighestBidDto getHighestBid(@PathVariable Long auctionId) {
        return bidService.getHighestBid(auctionId);
    }
}
