package com.sh.Ram.agent.dto;

import com.sh.Ram.entity.AuctionAgent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AgentDecisionRequestDto {

    private Long auctionId; // 경매 번호
    private Integer currentPrice; // 현재가
    private Integer maxBudget; // 사용자의 최대 한도
    private String agentStrategy; // Agent 사용 전략

    public AgentDecisionRequestDto(Integer currentPrice, AuctionAgent auctionAgent) {
        this.currentPrice = currentPrice;
        this.auctionId = auctionAgent.getAuction().getId();
        this.maxBudget = auctionAgent.getMaxBudget();
        this.agentStrategy = auctionAgent.getBidStrategy();
    }
}