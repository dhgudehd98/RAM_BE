package com.sh.Ram.auction.dto;

import com.sh.Ram.entity.Auction;
import com.sh.Ram.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDto {
    private Long id;
    private Long productId;
    private String name;
    private String brandName;
    private String imgURL;
    private Integer startPrice;
    private Integer currentPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private AuctionStatus auctionStatus;
    private Long auctionResultId;

    public AuctionDto(Auction auction) {
        this.id = auction.getId();
        this.productId = auction.getProduct().getId();
        this.name = auction.getProduct().getName();
        this.brandName = auction.getProduct().getBrand().getBrandName();
        this.imgURL = auction.getProduct().getImageUrl();
        this.startPrice = auction.getStartPrice();
        this.currentPrice = auction.getCurrentPrice();
        this.startDate = auction.getStartDate();
        this.endDate = auction.getEndDate();
        this.auctionStatus = auction.getAuctionStatus();
        this.auctionResultId = auction.getAuctionResult() != null ? auction.getAuctionResult().getId() : null;

    }
}