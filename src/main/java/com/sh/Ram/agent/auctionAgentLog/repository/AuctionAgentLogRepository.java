package com.sh.Ram.agent.auctionAgentLog.repository;

import com.sh.Ram.entity.AuctionAgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionAgentLogRepository extends JpaRepository<AuctionAgentLog, Long> {
}
