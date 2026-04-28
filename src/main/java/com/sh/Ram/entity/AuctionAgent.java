package com.sh.Ram.entity;

import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.auction.dto.AuctionAgentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "auctionAgent")
    private List<AuctionAgentLog> logs = new ArrayList<>();

    private Integer maxBudget; // 최대 한도

    private String strategyValue; // 입찰전략 -> 1. 추적 2. 마감 시간 30분전에 설정
    private String bidStrategy; // 입찰 전략

    @Enumerated(EnumType.STRING)
    private AgentStatus agentStatus; // Agent 상태

    @CreatedDate
    private LocalDateTime createdAt; // Agent 생성시간
    @LastModifiedDate
    private LocalDateTime updateAt; // Agent 수정시간

    public AuctionAgent(Auction auction , Member member, AuctionAgentDto auctionAgentDto){
        this.auction = auction;
        this.member = member;
        this.maxBudget = auctionAgentDto.getMaxBudget();
        this.strategyValue = auctionAgentDto.getStrategyValue();
        this.bidStrategy = auctionAgentDto.getBidStrategy();
        this.agentStatus = AgentStatus.ACTIVE;
    }
}