package com.sh.Ram.Auction.controller;

import com.sh.Ram.Auction.dto.AuctionDto;
import com.sh.Ram.Auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/list")
    public String auctionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "status", required = false) String status,
            Model model
    ) {

        Page<AuctionDto> auctions = auctionService.auctionList(page, status);

        model.addAttribute("auctions", auctions);
        model.addAttribute("status", status);
        return "auction/auctionList";
    }

    @PostMapping("{id}")
    @ResponseBody
    public Map<String, String> registAuction(
            @PathVariable("id") Long productId
    ) {
        auctionService.registAuction(productId);

        return Map.of("Y", "경매 상품으로 정상적으로 등록되었습니다.");
    }
}