package com.sh.Ram.auction.repository;

import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.AuctionAgent;
import com.sh.Ram.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionAgentRepository extends JpaRepository<AuctionAgent, Long> {
    boolean existsByAuctionAndMember(Auction auction, Member member);
}
