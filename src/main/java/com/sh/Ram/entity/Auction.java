package com.sh.Ram.entity;

import com.sh.Ram.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Setter
@Getter
@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer startPrice;

    private Integer currentPrice;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private AuctionStatus auctionStatus;

    @OneToMany(mappedBy = "auction")
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "auctionResult_id")
    private AuctionResult auctionResult;

    @OneToMany(mappedBy = "auction")
    private List<Bid> bids = new ArrayList<>();

    public void startAuction() {
        if (this.auctionStatus == AuctionStatus.PENDING) {
            this.auctionStatus = AuctionStatus.PROGRESS;
        }
    }

    public void endAuction() {
        if (this.auctionStatus == AuctionStatus.PROGRESS) {
            this.auctionStatus = AuctionStatus.CLOSED;
        }
    }
}
