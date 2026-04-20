package com.sh.Ram.auction.dto;

import com.sh.Ram.auction.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionAgentDto {
    private Long auctionId;
//    private Long memberId;

    private Integer maxBudget; // 최대 한도
    private Long currentBid; // 입찰 금액

    private String bidStrategy; // 입찰 전략
    private AgentStatus agentStatus; // Agent 상태

    private LocalDateTime createdAt; // Agent 생성시간
    private LocalDateTime updateAt;
}