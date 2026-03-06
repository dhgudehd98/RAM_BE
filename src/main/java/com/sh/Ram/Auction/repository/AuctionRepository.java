package com.sh.Ram.Auction.repository;

import com.sh.Ram.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
}
