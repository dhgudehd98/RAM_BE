package com.sh.Ram.bid.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HighestBidDto {

    private String nickname;
    private Integer price;
}
