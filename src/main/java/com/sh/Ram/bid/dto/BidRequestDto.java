package com.sh.Ram.bid.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BidRequestDto {

    private Long memberId;
    private int bidPrice; // 입찰금액

}
