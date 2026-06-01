package com.sh.Ram.entity;

import com.sh.Ram.enums.TradeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long id;

    @ManyToOne()
    @JoinColumn(
            name = "result_id",
            unique = true,
            nullable=false
    )
    private AuctionResult auctionResult;

    @Column(nullable = false)
    private Long buyerId;
    @Column(nullable = false)
    private Long sellerId;
    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus tradeStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Trade createTrade(AuctionResult auctionResult) {
        Trade trade = new Trade();
        trade.auctionResult = auctionResult;
        trade.buyerId = auctionResult.getBuyerId();
        trade.sellerId = auctionResult.getSellerId();
        trade.price = auctionResult.getFinalPrice();
        trade.tradeStatus = TradeStatus.WAITING_DELIVERY;
        trade.createdAt = LocalDateTime.now();

        return trade;
    }


}