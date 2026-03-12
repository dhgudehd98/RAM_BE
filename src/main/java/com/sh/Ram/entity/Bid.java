package com.sh.Ram.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@NoArgsConstructor
@Entity
@Table(
        indexes = {
                @Index(name = "idx_bid_auction_price", columnList = "auction_id, bid_price")
        }
)
@Getter
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    private Integer bidPrice;

    private LocalDateTime bidTime;

    public Bid(Member member, Auction auction, Integer bidPrice) {
        this.member = member;
        this.auction = auction;
        this.bidPrice = bidPrice;
        this.bidTime = LocalDateTime.now();
    }
}
