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

    private Long auctionId; // 경매 번호
    private Integer maxBudget; // 최대 한도
    private Long currentBid; // 입찰 금액
    private String bidStrategy; // 입찰 전략
}