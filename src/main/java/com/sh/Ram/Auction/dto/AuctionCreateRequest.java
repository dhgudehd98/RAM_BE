package com.sh.Ram.Auction.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AuctionCreateRequest {

    private Long productId;

    private Integer startPrice;

    private LocalDate startDate;

    private LocalDate endDate;
}

