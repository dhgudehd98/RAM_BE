package com.sh.Ram.bid.service;


import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.bid.dto.BidListDto;
import com.sh.Ram.bid.dto.BidResponseDto;
import com.sh.Ram.bid.dto.HighestBidDto;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.common.exception.auction.AuctionException;
import com.sh.Ram.common.exception.bid.BidException;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Bid;
import com.sh.Ram.entity.Member;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;

    /**
     * 입찰 내역 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<BidListDto> getBidList(Long auctionId, Pageable pageable) {

        Page<Bid> bids = bidRepository.findByAuctionId(auctionId, pageable);

        return bids.map(bid -> BidListDto.builder()
                .bidderNickName(bid.getMember().getNickname())
                .bidPrice(bid.getBidPrice())
                .bidTime(bid.getBidTime())
                .build());
    }

    /**
     * 최고 입찰 조회
     */
    @Transactional(readOnly = true)
    public HighestBidDto getHighestBid(Long auctionId) {

        Bid bid = bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId)
                .orElseThrow(() -> new BidException("입찰이 존재하지 않습니다."));

        return HighestBidDto.builder()
                .nickname(bid.getMember().getNickname())
                .price(bid.getBidPrice())
                .build();
    }

    /**
     * 입찰
     */
    @Transactional
    public BidResponseDto submitBid(Long auctionId, Long memberId) {

        // 경매 조회 + Lock
        Auction auction = auctionRepository.findByAuctionWithLock(auctionId)
                .orElseThrow(() -> new AuctionException("경매가 존재하지 않습니다."));

        // 경매 상태 검증
        if (auction.getAuctionStatus() != AuctionStatus.PROGRESS) {
            throw new AuctionException("경매가 진행 중이 아닙니다.");
        }

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("회원이 존재하지 않습니다."));

        // 최고 입찰자 조회
        Optional<Bid> topBid =
                bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId);

        // 최고 입찰자가 본인인지 체크
        topBid.ifPresent(bid -> {
            if (bid.getMember().getId().equals(memberId)) {
                throw new BidException("이미 최고 입찰자입니다.");
            }
        });

        Integer nextBidPrice;

        // 첫 입찰
        if (topBid.isEmpty()) {
            nextBidPrice = auction.getStartPrice();
        } else {
            Integer currentPrice = auction.getCurrentPrice();
            Integer bidUnit = calculateBidUnit(currentPrice);

            nextBidPrice = currentPrice + bidUnit;
        }

        // Bid 저장
        Bid bid = new Bid(member, auction, nextBidPrice);
        bidRepository.save(bid);

        // currentPrice 업데이트
        auction.setCurrentPrice(nextBidPrice);

        // DTO 반환
        return BidResponseDto.builder()
                .currentPrice(nextBidPrice)
                .highestBidderNickname(member.getNickname())
                .bidTime(bid.getBidTime())
                .build();
    }

    private Integer calculateBidUnit(Integer price) {

        if (price < 10_000) return 100;
        if (price < 50_000) return 500;
        if (price < 100_000) return 1_000;
        if (price < 500_000) return 5_000;
        if (price < 1_000_000) return 10_000;
        if (price < 5_000_000) return 50_000;
        if (price < 10_000_000) return 100_000;

        return 500_000;
    }
}
