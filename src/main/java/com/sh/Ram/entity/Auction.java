package com.sh.Ram.entity;

import com.sh.Ram.enums.AuctionStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Auction {

    @Id @GeneratedValue
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
}
