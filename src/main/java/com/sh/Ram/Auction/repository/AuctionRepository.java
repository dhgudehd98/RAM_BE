package com.sh.Ram.Auction.repository;

import com.sh.Ram.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

}
