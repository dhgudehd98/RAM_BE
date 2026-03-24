package com.sh.Ram.entity;

import com.sh.Ram.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_auction_product_status", columnList = "product_id, auction_status")
        }
)
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

    @OneToOne(mappedBy = "auction", fetch = LAZY)
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

    public Auction(Product product, Integer startPrice,LocalDate startDate, LocalDate endDate, AuctionStatus auctionStatus) {
        this.product = product;
        this.startPrice = startPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.auctionStatus = auctionStatus;
    }

    /**
     * 양방향 편의 메서드
     * @param result
     * AuctionResult 등록 시 Auction도 등록
     */
    public void setAuctionResult(AuctionResult result) {
        this.auctionResult = result;
        result.setAuction(this);
    }
}
