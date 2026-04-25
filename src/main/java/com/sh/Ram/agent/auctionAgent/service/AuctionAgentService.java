package com.sh.Ram.agent.auctionAgent.service;

import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.auction.dto.AuctionAgentDto;
import com.sh.Ram.auction.repository.AuctionAgentRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionAgentService {

    private final AuctionAgentRepository auctionAgentRepository;
    private final AuctionStreamService auctionStreamService;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
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

}