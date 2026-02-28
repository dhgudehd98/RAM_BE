package com.sh.Ram.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class AuctionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @OneToOne(mappedBy = "auctionResult")
    private Auction auction;

    @Column(name = "buyer_id", nullable = true)
    private Long buyerId;

    private Integer finalPrice;

    private Integer bidCount;

    private String resultStatus;

    private LocalDateTime resultTime;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "adminAccount_id")
    private AdminAccount adminAccount;

}
