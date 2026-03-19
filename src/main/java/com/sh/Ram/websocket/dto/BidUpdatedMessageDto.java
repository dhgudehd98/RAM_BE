package com.sh.Ram.websocket.dto;

import java.time.LocalDateTime;

public record BidUpdatedMessageDto (
        Long auctionId,
        Integer currentPrice,
        String highestBidderNickname,
        LocalDateTime lastBidTime,
        Integer nextBidPrice,
        String auctionStatus
){}
