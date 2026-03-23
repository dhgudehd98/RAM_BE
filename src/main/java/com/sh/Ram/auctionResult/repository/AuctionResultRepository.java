package com.sh.Ram.auctionResult.repository;

import com.sh.Ram.entity.AuctionResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionResultRepository extends JpaRepository<AuctionResult, Long> {

    /**
     * 특정 경매 결과 존재 여부 (멱등성)
     */
    boolean existsByAuctionId(Long auctionId);
}
