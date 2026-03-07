package com.sh.Ram.Auction.service;

import com.sh.Ram.Auction.dto.AuctionDto;
import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {
    private final AuctionRepository auctionRepository;

    public Page<AuctionDto> auctionList(int page, String status) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("startDate").descending());
        Page<Auction> auctionList ;

        // status에 대한 부분이 없으면 전체 조회
        if (status == null) {
            auctionList = auctionRepository.findAllWithProduct(pageable);
        }else{
            AuctionStatus auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
            auctionList = auctionRepository.findAuctionByStatus(pageable, auctionStatus);
        }

        return auctionList.map(AuctionDto::new);
    }
}