package com.sh.Ram.bid.service;


import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.bid.dto.BidListDto;
import com.sh.Ram.bid.dto.BidResponseDto;
import com.sh.Ram.bid.dto.HighestBidDto;
import com.sh.Ram.bid.event.BidSubmittedEvent;
import com.sh.Ram.bid.repository.BidRepository;
import com.sh.Ram.common.exception.account.AccountException;
import com.sh.Ram.common.exception.auction.AuctionException;
import com.sh.Ram.common.exception.bid.BidException;
import com.sh.Ram.common.exception.member.MemberException;
import com.sh.Ram.entity.Account;
import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Bid;
import com.sh.Ram.entity.Member;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
    public BidResponseDto submitBid(Long auctionId, Long memberId, Integer expectedPrice) {

        // 경매 조회 + Lock
        Auction auction = auctionRepository.findByAuctionWithLock(auctionId)
                .orElseThrow(() -> new AuctionException("경매가 존재하지 않습니다."));

        // 경매 상태 검증
        if (auction.getAuctionStatus() != AuctionStatus.PROGRESS) {
            throw new AuctionException("경매가 진행 중이 아닙니다.");
        }

        Integer currentPrice = auction.getCurrentPrice();
        Integer bidPrice;
        Integer nextBidPrice;

        // 첫 입찰
        if (currentPrice == null) {

            // 첫 입찰은 시작가
            if (!expectedPrice.equals(auction.getStartPrice())) {
                throw new BidException(
                        "첫 입찰은 시작가여야 합니다.",
                        auction.getStartPrice(),
                        auction.getStartPrice()
                );
            }

            bidPrice = auction.getStartPrice();
            nextBidPrice = bidPrice + calculateBidUnit(bidPrice);

        } else {
            // 이후 입찰
            Integer bidUnit = calculateBidUnit(currentPrice);
            Integer expectedNextPrice = currentPrice + bidUnit;

            // 가격 검증
            if (!expectedPrice.equals(expectedNextPrice)) {
                throw new BidException(
                        "가격이 변경되었습니다. 다시 입찰해주세요",
                        currentPrice,
                        expectedNextPrice
                );
            }

            bidPrice = expectedNextPrice;
            nextBidPrice = bidPrice + calculateBidUnit(bidPrice);
        }

        // 최고 입찰자 중복 방지
        Optional<Bid> topBid =
                bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId);

        topBid.ifPresent(bid -> {
            if (bid.getMember().getId().equals(memberId)) {
                throw new BidException("이미 최고 입찰자입니다.");
            }
        });

        // 6. 새 입찰자 예약금 확보 (하드 체크)
        int reserved = accountRepository.increaseReservedBalanceIfAvailable(
                memberId,
                bidPrice.longValue()
        );

        if (reserved == 0) {
            throw new BidException("가용 잔액이 부족하여 입찰할 수 없습니다.");
        }

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("회원이 존재하지 않습니다."));

        // Bid 저장
        Bid bid = new Bid(member, auction, bidPrice);
        bidRepository.save(bid);

        // currentPrice 업데이트
        auction.setCurrentPrice(bidPrice);

        // 이전 최고 입찰자 예약금 해제
        topBid.ifPresent(prevTopBid -> {
            int released = accountRepository.decreaseReservedBalance(
                    prevTopBid.getMember().getId(),
                    prevTopBid.getBidPrice().longValue()
            );

            if (released == 0) {
                throw new AccountException("이전 최고 입찰자의 예약금 해제에 실패했습니다.");
            }
        });

        // 이벤트 발행
        applicationEventPublisher.publishEvent(
                new BidSubmittedEvent(
                        auction.getId(),
                        bidPrice,
                        member.getNickname(),
                        bid.getBidTime(),
                        nextBidPrice,
                        auction.getAuctionStatus().name(),
                        auction.getEndDate()
                )
        );

        // DTO 반환
        return BidResponseDto.builder()
                .currentPrice(bidPrice)
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
