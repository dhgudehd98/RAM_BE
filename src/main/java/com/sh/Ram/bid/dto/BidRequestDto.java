package com.sh.Ram.bid.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BidRequestDto {

    private Long memberId;
    private Integer expectedPrice;
    private int bidPrice; // 입찰금액

}
