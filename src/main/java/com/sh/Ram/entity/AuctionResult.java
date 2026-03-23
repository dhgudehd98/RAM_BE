package com.sh.Ram.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@Setter
public class AuctionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "auction_id", unique = true)
    private Auction auction;

    @Column(name = "buyer_id", nullable = true)
    private Long buyerId;

    private Integer finalPrice;

    private Integer bidCount;

    /**
     * 경매 결과 상태
     * SUCCESS / FAILED
     */
    private String resultStatus;

    /**
     * 정산 상태
     * NONE / HELD / SETTLED
     */
    private String settlementStatus;

    /**
     * 결과 생성 시간
     */
    private LocalDateTime resultTime;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "adminAccount_id")
    private AdminAccount adminAccount;

}
