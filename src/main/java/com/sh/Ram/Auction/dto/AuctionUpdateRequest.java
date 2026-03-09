package com.sh.Ram.Auction.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AuctionUpdateRequest {

    private Integer startPrice;

    private LocalDate startDate;

    private LocalDate endDate;
}
