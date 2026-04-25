package com.sh.Ram.auction.repository;

import com.sh.Ram.auction.AgentStatus;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.AuctionAgent;
import com.sh.Ram.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionAgentRepository extends JpaRepository<AuctionAgent, Long> {


    // 엔티티의 필드명이 agentStatus라고 가정할 때
    boolean existsByAuctionAndMemberAndAgentStatus(Auction auction, Member member, AgentStatus agentStatus);

    @Query("select a, auction from AuctionAgent a join fetch a.auction auction join fetch a.member join fetch auction.auctionResult where a.agentStatus = :agentStatus ")
    List<AuctionAgent> findByAuctionAgentStatus(@Param("agentStatus") AgentStatus agentStatus);
}
