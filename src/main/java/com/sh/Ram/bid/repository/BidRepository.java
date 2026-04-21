package com.sh.Ram.bid.repository;


import com.sh.Ram.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 특정 경매의 입찰 목록 (페이징)
     */
    Page<Bid> findByAuctionId(Long auctionId, Pageable pageable);

    /**
     * 최고 입찰가 조회
     */
    Optional<Bid> findTopByAuctionIdOrderByBidPriceDesc(Long auctionId);

    /**
     * 입찰 수 조회
     */
    long countByAuctionId(Long auctionId);

    // BidRepository.java
    @Query("SELECT b FROM Bid b JOIN FETCH b.member JOIN FETCH b.auction WHERE b.auction.id = :auctionId ORDER BY b.id DESC LIMIT 1")
    Optional<Bid> findFirstWithMemberAndAuction(@Param("auctionId") Long auctionId);

}
