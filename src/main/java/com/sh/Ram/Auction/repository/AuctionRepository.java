package com.sh.Ram.Auction.repository;

import com.sh.Ram.entity.Auction;
import com.sh.Ram.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Query(value = "SELECT a FROM Auction a JOIN FETCH a.product",
            countQuery = "SELECT count(a) FROM Auction a")
    Page<Auction> findAllWithProduct(Pageable pageable);

    // 상품별 조회
    @Query(value = "SELECT a FROM Auction a JOIN FETCH a.product where a.auctionStatus = :status",
            countQuery = "SELECT  count(a) FROM Auction a where a.auctionStatus = :status")
    Page<Auction> findAuctionByStatus(Pageable pageable, @Param("status")AuctionStatus auctionStatus);
}
