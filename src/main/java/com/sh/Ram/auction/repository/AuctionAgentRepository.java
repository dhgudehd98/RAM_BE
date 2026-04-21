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
    boolean existsByAuctionAndMember(Auction auction, Member member);

    @Query("select a, auction from AuctionAgent a join fetch a.auction auction where a.agentStatus = :agentStatus ")
    List<AuctionAgent> findByAuctionAgentStatus(@Param("agentStatus") AgentStatus agentStatus);
}
