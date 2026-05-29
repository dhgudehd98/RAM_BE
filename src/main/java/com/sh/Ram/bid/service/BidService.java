package com.sh.Ram.bid.service;


import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.bid.dto.BidListDto;
import com.sh.Ram.bid.dto.BidRequestDto;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final StringRedisTemplate stringRedisTemplate;


    private static final String BID_LOCK_KEY = "lock:auction:bid:";
    private static final Duration BID_LOCK_TTL = Duration.ofSeconds(5); // 락 유지 시간 5초
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

    public BidResponseDto submitBidRedis(Long auctionId, BidRequestDto request) {
        /**
         * Redis 분산락을 통해 입찰 내역 저장
         * 1. auctionId에 현재 락이 걸려 있는지 확인
         * 2. 락이 걸려 있으면 에러 배출
         * 3. 락이 안걸려 있으면 비즈니스 로직 실행
         */

        String lockKey = BID_LOCK_KEY + auctionId;
        String lockValue = UUID.randomUUID().toString();

        if (!tryLock(lockKey, lockValue, request.getMemberId())) {
            throw new BidException("현재 입찰 요청이 많아서 잠시 후에 시도해주세요.");
        }

        try{
            return transactionTemplate.execute(status ->
                    processBid(auctionId, request)
            );
        }
        finally {
            //락 해제 요청
            unLock(lockKey, lockValue);
        }

    }

    /**
     * 실제 입찰 로직
     */
    public BidResponseDto processBid(Long auctionId, BidRequestDto requestDto) {
        Long memberId = requestDto.getMemberId(); // 멤버
        Integer bidPrice = requestDto.getBidPrice(); // 입찰 가격
        Integer currentPrice = null; // 경매 현재가
        Integer nextBidPrice = null; // 다음 촤소 입찰가
        Integer bidUnit = null; // 최소 입찰 단위

        Auction auction = auctionRepository.findAuctionByIdAndStatus(auctionId, AuctionStatus.PROGRESS).orElseThrow(() -> new AuctionException("현재 진행중인 경매가 아닙니다."));

        currentPrice = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : null; //

        log.info("[경매 입찰 진행] 경매 번호 : {}, 현재가 : {},  입찰자 : {} , 입찰금액 : {}", auctionId, auction.getCurrentPrice(),  memberId, bidPrice);

        //첫 입찰시
        if (currentPrice == null) {
            // 첫 입찰시 시작 가격에 대한 값보다 크게 입찰하면 됨.
            if(bidPrice < auction.getStartPrice())throw new BidException("첫 입찰은 입찰 시작가보다 커야됩니다.");
        }
        else{
            bidUnit = calculateBidUnit(currentPrice);
            nextBidPrice = currentPrice + bidUnit;
            // 현재가 30000원 , 최소 입찰 단위 35000
            // 33000원으로 지불 했을 때 최소 입찰가보다 가격이 적기 떄문에 입찰 실패
            if(bidPrice < nextBidPrice) throw new BidException("최소 입찰 단위보다 높게 입찰을 신청해주세요.");
        }

        // 최고 입찰자 확인
        Optional<Bid> topBid =
                bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId);

        topBid.ifPresent(bid -> {
            if (bid.getMember().getId().equals(memberId)) {
                throw new BidException("이미 최고 입찰자입니다.");
            }
        });

        // 입찰자 예약금 확보
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

        // Bid 저장
        Bid bid = new Bid(member, auction, bidPrice);
        bidRepository.save(bid);
        log.info("[입찰 완료] 입찰자 : {} , 입찰 가격 : {} ", memberId, bidPrice);

        // currentPrice 업데이트
        int updateAuctionCurrentPrice = auctionRepository.updateAuctionCurrentPrice(bidPrice, auctionId);
        log.info("[입찰 현재가 결과] updateResult : {}", updateAuctionCurrentPrice);

        if(updateAuctionCurrentPrice == 1) log.info("[입찰 현재가 반영 완료] 경매번호 : {}, 회원번호 : {}, 입찰가격 : {}, 경매 현재가 : {}", auctionId, memberId, bidPrice, currentPrice);
        else log.info("[입찰 현재가 반영 실패] 경매번호 : {}, 회원번호 : {}, 입찰가격 : {}, 경매 현재가 : {}", auctionId, memberId, bidPrice, currentPrice);
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

    // Redis 분산락 확인(락 획득 재시도 로직 없이 확인)
    private boolean tryLock(String lockKey, String lockValue, Long memberId) {
        // setIfAbsent는 레디스에 키가 없을 때만 저장(NX)하므로 원자적(Atomic)으로 락 선점이 가능합니다.
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, BID_LOCK_TTL);

        if (Boolean.TRUE.equals(acquired)) {
            log.info("[Redis 락 획득 성공] 사용자 번호 : {}", memberId);
            return true;
        }

        // 락 획득에 실패하면 미련 없이 즉시 false 반환
        log.info("[Redis 락 획득 실패] 이미 다른 사용자가 입찰 중입니다. 사용자 번호 : {}", memberId);
        return false;
    }

    private void unLock(String lockKey, String lockValue) {

        // lock 삭제 명령어 -> 삭제되면 return 1
        String script = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """;

        stringRedisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(lockKey),
                lockValue
        );
    }

//    /**
//     * 입찰
//     */
//    @Transactional
//    public BidResponseDto submitBid(Long auctionId, Long memberId, Integer expectedPrice) {
//
//        // 경매 조회 + Lock
//        Auction auction = auctionRepository.findByAuctionWithLock(auctionId)
//                .orElseThrow(() -> new AuctionException("경매가 존재하지 않습니다."));
//
//
//        // 경매 상태 검증
//        if (auction.getAuctionStatus() != AuctionStatus.PROGRESS) {
//            throw new AuctionException("경매가 진행 중이 아닙니다.");
//        }
//
//        Integer currentPrice = auction.getCurrentPrice();
//        Integer bidPrice;
//        Integer nextBidPrice;
//
//        // 첫 입찰
//        if (currentPrice == null) {
//
//            // 첫 입찰은 시작가
//            if (!expectedPrice.equals(auction.getStartPrice())) {
//                throw new BidException(
//                        "첫 입찰은 시작가여야 합니다.",
//                        auction.getStartPrice(),
//                        auction.getStartPrice()
//                );
//            }
//
//            bidPrice = auction.getStartPrice();
//            nextBidPrice = bidPrice + calculateBidUnit(bidPrice);
//
//        } else {
//            // 이후 입찰
//            Integer bidUnit = calculateBidUnit(currentPrice);
//            Integer expectedNextPrice = currentPrice + bidUnit;
//
//            if (expectedPrice < expectedNextPrice) {
//                throw new BidException(
//                        "최소 입찰 금액 이상으로 입찰해주세요.", currentPrice, expectedNextPrice
//                );
//            }
//
//            bidPrice = expectedPrice;
//            nextBidPrice = bidPrice + calculateBidUnit(bidPrice);
//        }
//
//        // 최고 입찰자 중복 방지
//        Optional<Bid> topBid =
//                bidRepository.findTopByAuctionIdOrderByBidPriceDesc(auctionId);
//
//        topBid.ifPresent(bid -> {
//            if (bid.getMember().getId().equals(memberId)) {
//                throw new BidException("이미 최고 입찰자입니다.");
//            }
//        });
//
//        // 6. 새 입찰자 예약금 확보 (하드 체크)
//        int reserved = accountRepository.increaseReservedBalanceIfAvailable(
//                memberId,
//                bidPrice.longValue()
//        );
//
//        if (reserved == 0) {
//            log.info("[가용 잔액 부족] : 해당 회원번호 : {}", memberId);
//            throw new BidException("가용 잔액이 부족하여 입찰할 수 없습니다.");
//        }
//
//        // 회원 조회
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new MemberException("회원이 존재하지 않습니다."));
//
//        // Bid 저장
//        Bid bid = new Bid(member, auction, bidPrice);
//        bidRepository.save(bid);
//
//        // currentPrice 업데이트
//        // 위의 로직에서 준영속된 Auction에 대한 값을 다시 영속상태로 만들기
//        Auction refreshAuction = auctionRepository.findById(auctionId).get();
//        refreshAuction.setCurrentPrice(bidPrice);
//
//
//        log.info("[Auction Info] 경매번호 :{} , 현재가 : {} , 입찰가 : {}", auctionId, auction.getCurrentPrice(), bidPrice);
//
//        // 이전 최고 입찰자 예약금 해제
//        topBid.ifPresent(prevTopBid -> {
//            int released = accountRepository.decreaseReservedBalance(
//                    prevTopBid.getMember().getId(),
//                    prevTopBid.getBidPrice().longValue()
//            );
//
//            if (released == 0) {
//                throw new AccountException("이전 최고 입찰자의 예약금 해제에 실패했습니다.");
//            }
//        });
//
//        // 이벤트 발행
//        applicationEventPublisher.publishEvent(
//                new BidSubmittedEvent(
//                        auction.getId(),
//                        bidPrice,
//                        member.getNickname(),
//                        bid.getBidTime(),
//                        nextBidPrice,
//                        auction.getAuctionStatus().name(),
//                        auction.getEndDate()
//                )
//        );
//
//        // DTO 반환
//        return BidResponseDto.builder()
//                .currentPrice(bidPrice)
//                .highestBidderNickname(member.getNickname())
//                .bidTime(bid.getBidTime())
//                .build();
//    }

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
