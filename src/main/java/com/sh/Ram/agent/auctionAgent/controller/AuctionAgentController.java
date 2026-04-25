package com.sh.Ram.agent.auctionAgent.controller;

import com.sh.Ram.agent.auctionAgent.service.AuctionAgentService;
import com.sh.Ram.auction.dto.AuctionAgentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auction/agent")
@Slf4j
public class AuctionAgentController {
    private final AuctionAgentService auctionAgentService;

    @PostMapping("")
    @ResponseBody
    public Map<String, String> auctionAgentCreate(
            @RequestBody AuctionAgentDto auctionAgentDto
//            Authentication authentication
    ){
        return auctionAgentService.auctionAgentRegist(auctionAgentDto, 4L);
//        return auctionService.auctionAgentRegist(auctionAgentDto, (Long)authentication.getPrincipal());
    }
}