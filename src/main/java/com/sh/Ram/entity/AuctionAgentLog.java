package com.sh.Ram.entity;

import com.sh.Ram.agent.auctionAgent.dto.AgentDecisionResponseDto;
import com.sh.Ram.enums.AgentDecision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionAgentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AuctionAgent auctionAgent;

    @Enumerated(EnumType.STRING)
    private AgentDecision decision; // BID, STAY

    private Integer bidPriceAtDecision; // 입찰 당시 최고 입찰가

    private Integer suggestedBidPrice; // AI가 제안한 금액

    @Column(columnDefinition = "TEXT")
    private String reason; // AI의 판단 근거 (reason)

    // --- 최종 실행 결과 ---
    private Boolean isSuccess; // 실제 입찰 성공 여부
    private String bidFailReason; // 입찰 실패시 에러 메세지

    @CreatedDate
    private LocalDateTime createdAt;

    public AuctionAgentLog(AgentDecisionResponseDto decisionResponseDto, AuctionAgent auctionAgent, boolean isSuccess, String bidFailReason){
        this.auctionAgent = auctionAgent;
        this.decision = AgentDecision.valueOf(decisionResponseDto.getDecision());
        this.bidPriceAtDecision = decisionResponseDto.getCurrentPrice();
        this.suggestedBidPrice = decisionResponseDto.getSuggestedBidPrice();
        this.reason = decisionResponseDto.getReason();
        this.isSuccess = isSuccess;
        this.bidFailReason = (bidFailReason != null) ? bidFailReason : null;
    }
}