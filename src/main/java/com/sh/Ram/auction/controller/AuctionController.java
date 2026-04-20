package com.sh.Ram.auction.controller;

import com.sh.Ram.auction.dto.AuctionAgentDto;
import com.sh.Ram.auction.dto.AuctionDto;
import com.sh.Ram.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auction")
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;

//    @GetMapping("/list")
//    @ResponseBody
//    public Page<AuctionDto> auctionList(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(required = false) String sort,
//            @RequestParam(name = "status", required = false) String status
//    ) {
//        log.info("=== 경매 상품 리스트 요청 ===");
//        return auctionService.auctionList(page, sort, status);
//    }

    @GetMapping("/list")
    @ResponseBody
    public List<AuctionDto> auctionList(
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "status", required = false) String status
    ) {
        return auctionService.auctionList(lastId, sort, status);
    }

    @PostMapping("{id}")
    @ResponseBody
    public Map<String, String> registAuction(
            @PathVariable("id") Long productId
    ) {
        auctionService.registAuction(productId);

        return Map.of("Y", "경매 상품으로 정상적으로 등록되었습니다.");
    }

    @PostMapping("/agent")
    @ResponseBody
    public Map<String, String> auctionAgentRegist(
            @RequestBody AuctionAgentDto auctionAgentDto,
            Authentication authentication
    ){
        return auctionService.auctionAgentRegist(auctionAgentDto, (Long)authentication.getPrincipal());
    }
}