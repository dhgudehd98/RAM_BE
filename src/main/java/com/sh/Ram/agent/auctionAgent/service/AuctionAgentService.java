package com.sh.Ram.agent.auctionAgent.service;

import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.auction.dto.AuctionAgentDto;
import com.sh.Ram.auction.repository.AuctionAgentRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.bid.service.BidService;
import com.sh.Ram.common.exception.auctionAgent.AuctionAgentException;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.AuctionAgent;
import com.sh.Ram.entity.Member;
import com.sh.Ram.member.repository.MemberRepository;
import com.sh.Ram.redis.auction.service.AuctionStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionAgentService {

    private final AuctionAgentRepository auctionAgentRepository;
    private final AuctionStreamService auctionStreamService;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final BidService bidService;

    @Transactional
    public Map<String, String> auctionAgentRegist(AuctionAgentDto auctionAgentDto, Long memberId) {
        try{
            Auction auction = auctionRepository.getReferenceById(auctionAgentDto.getAuctionId());
            Member member = memberRepository.getReferenceById(memberId);

            // 동일한 경매에 동일한 사용자가 자동경매 신청해놨는지 확인 -> 중복 신청 불가
            if(auctionAgentRepository.existsByAuctionAndMemberAndAgentStatus(auction, member, AgentStatus.ACTIVE)) throw new AuctionAgentException("해당 경매에 자동 입찰이 설정된 내역이 존재합니다.");

            AuctionAgent auctionAgent = new AuctionAgent(auction, member, auctionAgentDto);
            auctionAgentRepository.save(auctionAgent);

            // 경매 입찰 전략이 추적인 경우 Redis Stream에 저장된 해당 경매 구독 설정
            if (auctionAgentDto.getStrategyValue().equals("1")) {
                auctionStreamService.registerAgentToStream(auctionAgent); // 해당 경매에 입찰이 들어오는지
            }

            return Map.of(
                    "result", "Y",
                    "message", "자동 입찰이 성공적으로 등록되었습니다."
            );

        }catch(Exception e){
            log.error("[Auction Agent Regist Error] : {}", e.getMessage());
            throw e;
        }

    }

    @Async
    public void findTrackingAgents(Long auctionId, Long memberId, Integer bidPrice) {

        log.info("[Find Tracking Agents]");
        // Agent의 예산 한도에 대한 값은 다음 입찰 가격보다 큰 값을 조회
        Integer nextBidPrice = bidPrice + calculateBidUnit(bidPrice);
        Optional<AuctionAgent> trackingAgents = auctionAgentRepository.findTrackingAgents(auctionId, nextBidPrice);

        // 해당 결과에 알맞는 Agent가 있으면 바로 입찰 기능 구현
        if (trackingAgents.isPresent()) {
            AuctionAgent auctionAgent = trackingAgents.get();

            // 입찰한 memberId에 대한 값과 Agent의 memberId에 대한값이 동일한 값이면 종료
            if(auctionAgent.getMember().getId().equals(memberId)) {
                log.info("[Not Found Regist Auction Agent]");
                return;
            }

            // Agent를 통해서 다시 입찰
            bidService.submitBid(auctionAgent.getAuction().getId(), auctionAgent.getMember().getId(), nextBidPrice);
        }
    }

    private Integer calculateBidUnit(Integer price) {

        if (price < 10_000) return 100;
        if (price < 50_000) return 500;
        if (price < 100_000) return 1_000;
        if (price < 500_000) return 5_000;
        if (price < 1_000_000) return 10_000;
        if (price < 5_000_000) return 50_000;
        if (price < 10_000_000) return 100_000;

        return 500_000;
    }
}