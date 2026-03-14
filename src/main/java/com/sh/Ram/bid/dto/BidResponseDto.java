package com.sh.Ram.bid.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BidResponseDto {

    private Integer currentPrice;

    private String highestBidderNickname;

    private LocalDateTime bidTime;
}
