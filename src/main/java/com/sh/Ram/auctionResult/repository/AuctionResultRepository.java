package com.sh.Ram.auctionResult.repository;

import com.sh.Ram.entity.AuctionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuctionResultRepository extends JpaRepository<AuctionResult, Long> {

    /**
     * 특정 경매 결과 존재 여부 (멱등성)
     */
    boolean existsByAuctionId(Long auctionId);

    @Query("SELECT ar FROM AuctionResult ar WHERE ar.auction.id = :auctionId")
    Optional<AuctionResult> findByAuctionId(@Param("auctionId") Long auctionId);
}
