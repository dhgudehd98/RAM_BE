package com.sh.Ram.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sh.Ram.agent.auctionAgentLog.repository.AuctionAgentLogRepository;
import com.sh.Ram.agent.dto.AgentDecisionRequestDto;
import com.sh.Ram.agent.dto.AgentDecisionResponseDto;
import com.sh.Ram.agent.dto.LlmResponseDto;
import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.auction.repository.AuctionAgentRepository;
import com.sh.Ram.bid.dto.BidResponseDto;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.AuctionAgent;
import com.sh.Ram.entity.AuctionAgentLog;
import com.sh.Ram.entity.Bid;
import com.sh.Ram.product.dto.AiProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionAgentScheduler {

    private final AuctionAgentRepository auctionAgentRepository;
    private final AuctionAgentLogRepository auctionAgentLogRepository;
    private final BidService bidService;
    private final BidRepository bidRepository;
    private final WebClient webClient;


    /**
     * 전략
     * 1. 마감 30초 전에 마지막 입찰자가 본인이 아니라면 + 1000 원에서 입찰
     * 2. 한도 내에서까지 경쟁자가 있으면 계속 해당 입찰자보다 1000원 입찰
     */
//    @Scheduled(cron = "0 * * * * *")
    public void processAuctionAgent() {
        // 1. 현재 활성화 되어 있는 Agent들 조회
        List<AuctionAgent> agents = auctionAgentRepository.findByAuctionAgentStatus(AgentStatus.ACTIVE);

        for (AuctionAgent agent : agents) {
            Long auctionId = agent.getAuction().getId();

            //2. Agent에 등록한 경매에 최고 입찰자 내역 가져오기
            Optional<Bid> lastBid = bidRepository.findFirstWithMemberAndAuction(auctionId);

            //3. AI 요청 조건 -> Agent에 저장된 사용자가 최고 입찰자거나 경매에 입찰한 내역이 있으면 AI한테 요청
            boolean isBidLastMemberId = lastBid
                    .map(bid -> bid.getMember().getId().equals(agent.getMember().getId()))
                    .orElse(false);

            // 위 조건에 해당하지 않으면 AI한테 요청
            if(!isBidLastMemberId) {
                AgentDecisionResponseDto agentDecisionResponseDto = requestAiDecision(agent, lastBid.orElse(null));
                log.info("[AgentDecisionResponseDto] : " + agentDecisionResponseDto.toString());
                agentResultDecision(agent, agentDecisionResponseDto);
            }
        }
    }

    private AgentDecisionResponseDto requestAiDecision(AuctionAgent agent, Bid bid) {

        Integer currentPrice = (bid != null) ? bid.getBidPrice() : agent.getAuction().getStartPrice();
        AgentDecisionRequestDto dto = new AgentDecisionRequestDto(currentPrice, agent);
        return webClient.post()
                .uri("http://localhost:8081/agent/decision")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(LlmResponseDto.class)
                .map(llmResponseDto -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(llmResponseDto.getResponse(), AgentDecisionResponseDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("응답 파싱 실패 ");
                    }
                }).block();
    }

    private void agentResultDecision(AuctionAgent auctionAgent, AgentDecisionResponseDto agentDecisionResponseDto) {
        boolean isSuccess = false; // 결과 설정
        String bidFailReason = null;

        if (agentDecisionResponseDto.getDecision().equals("BID")) {
            try{
                bidService.submitBid(agentDecisionResponseDto.getAuctionId(), auctionAgent.getMember().getId(), agentDecisionResponseDto.getSuggestedBidPrice());
                isSuccess = true;
            }catch(Exception e){
                log.error("[BID Exception] : " + e.getMessage());
                bidFailReason = e.getMessage();
            }
        }
        else if(agentDecisionResponseDto.getDecision().equals("STAY")){
            isSuccess = true; // STAY에 대한 부분은 항상 true로 설정
        }

        // AuctionAgentLog 저장
        auctionAgentLogRepository.save(new AuctionAgentLog(agentDecisionResponseDto, auctionAgent, isSuccess, bidFailReason ));
    }



}