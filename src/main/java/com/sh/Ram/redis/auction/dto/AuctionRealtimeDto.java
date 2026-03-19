package com.sh.Ram.redis.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AuctionRealtimeDto {

    private Integer currentPrice;

    private String highestBidderNickname;

    private LocalDateTime lastBidTime;

    private Integer nextBidPrice;

    private String auctionStatus;
}
