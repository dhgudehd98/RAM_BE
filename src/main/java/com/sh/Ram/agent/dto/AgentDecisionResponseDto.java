package com.sh.Ram.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentDecisionResponseDto {

    private Long auctionId; // 경매 번호
    private String decision; // 입찰할지 안할지 -> BID || STAY ?
    private Integer suggestedBidPrice; // 입찰 금액
    private String reason; // 판단 근거 요약

    @Override
    public String toString() {
        return "AgentDecisionResponseDto{" +
                "auctionId=" + auctionId +
                ", decision='" + decision + '\'' +
                ", suggestedBidPrice=" + suggestedBidPrice +
                ", decisionReason='" + reason + '\'' +
                '}';
    }
}