package com.sh.Ram.bid.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BidSubmittedEvent (
        Long auctionId,
        Integer currentPrice,
        String highestBidderNickname,
        LocalDateTime lastBidTime,
        Integer nextBidPrice,
        String auctionStatus,
        LocalDate endDate
){}
