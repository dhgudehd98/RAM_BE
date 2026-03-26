package com.sh.Ram.auction.repository;

import com.sh.Ram.auction.dto.AuctionDto;
import com.sh.Ram.entity.Auction;

import java.util.List;

public interface AuctionRepositoryQuery {

    List<Auction> findAll(Long lastId, String sort, String status);
}
