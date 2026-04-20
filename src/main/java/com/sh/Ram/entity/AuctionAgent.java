package com.sh.Ram.entity;

import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.auction.dto.AuctionAgentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AuctionAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer maxBudget; // 최대 한도
    private Long currentBid; // 입찰 금액

    private String bidStrategy; // 입찰 전략
    private AgentStatus agentStatus; // Agent 상태

    private LocalDateTime createdAt; // Agent 생성시간
    private LocalDateTime updateAt; // Agent 수정시간

    public AuctionAgent(Auction auction , Member member, AuctionAgentDto auctionAgentDto){
        this.auction = auction;
        this.member = member;
        this.maxBudget = auctionAgentDto.getMaxBudget();
        this.currentBid = auctionAgentDto.getCurrentBid();
        this.bidStrategy = auctionAgentDto.getBidStrategy();
        this.agentStatus = auctionAgentDto.getAgentStatus();
        this.createdAt = auctionAgentDto.getCreatedAt();
        this.updateAt = auctionAgentDto.getUpdateAt();
    }
}