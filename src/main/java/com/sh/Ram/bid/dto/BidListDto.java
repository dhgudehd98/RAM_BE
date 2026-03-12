package com.sh.Ram.bid.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BidListDto {

    private String bidderNickName;
    private Integer bidPrice;
    private LocalDateTime bidTime;
}
