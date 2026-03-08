package com.sh.Ram.Auction.repository;

import com.sh.Ram.entity.Auction;
import com.sh.Ram.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Auction a
            SET a.auctionStatus = com.sh.Ram.enums.AuctionStatus.PROGRESS
            WHERE a.auctionStatus = com.sh.Ram.enums.AuctionStatus.PENDING
            AND a.startDate <= CURRENT_DATE
    """)
    int bulkStartAuction();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Auction a
        SET a.auctionStatus = com.sh.Ram.enums.AuctionStatus.CLOSED
        WHERE a.auctionStatus = com.sh.Ram.enums.AuctionStatus.PROGRESS
        AND a.endDate <= CURRENT_DATE
    """)
    int bulkEndAuction();

    @Query(value = "SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.brand",
            countQuery = "SELECT count(a) FROM Auction a")
    Page<Auction> findAllWithProduct(Pageable pageable);

    // 상품별 조회
    @Query(value = "SELECT a FROM Auction a JOIN FETCH a.product p JOIN FETCH p.brand WHERE a.auctionStatus = :status",
            countQuery = "SELECT count(a) FROM Auction a WHERE a.auctionStatus = :status")
    Page<Auction> findAuctionByStatus(Pageable pageable, @Param("status") AuctionStatus auctionStatus);
}
