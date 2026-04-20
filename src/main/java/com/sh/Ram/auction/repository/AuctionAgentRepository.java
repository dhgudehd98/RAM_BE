package com.sh.Ram.auction.repository;

import com.sh.Ram.entity.AuctionAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionAgentRepository extends JpaRepository<AuctionAgent, Long> {
}
