package com.sh.Ram.websocket.listener;

import com.sh.Ram.bid.event.BidSubmittedEvent;
import com.sh.Ram.redis.auction.dto.AuctionRealtimeDto;
import com.sh.Ram.redis.auction.service.AuctionRedisCacheService;
import com.sh.Ram.websocket.dto.BidUpdatedMessageDto;
import com.sh.Ram.websocket.publisher.AuctionWebsocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidRealtimeEventListener {

    private final AuctionRedisCacheService auctionRedisCacheService;
    private final AuctionWebsocketPublisher auctionWebsocketPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidSubmitted(BidSubmittedEvent event) {

        // Redis 객체 상태 생성
        AuctionRealtimeDto state = AuctionRealtimeDto.builder()
                .currentPrice(event.currentPrice())
                .highestBidderNickname(event.highestBidderNickname())
                .lastBidTime(event.lastBidTime())
                .nextBidPrice(event.nextBidPrice())
                .auctionStatus(event.auctionStatus())
                .build();

        // Redis 캐시 저장
        auctionRedisCacheService.updateAuctionState(
                event.auctionId(),
                state,
                event.endDate()
        );

        // Websocket 메시지 생성
        BidUpdatedMessageDto message = new BidUpdatedMessageDto(
                event.auctionId(),
                event.currentPrice(),
                event.highestBidderNickname(),
                event.lastBidTime(),
                event.nextBidPrice(),
                event.auctionStatus()
        );

        // Websocket 전파
        auctionWebsocketPublisher.publishBidUpdated(message);

        log.info("실시간 입찰 반영 완료 auctionId={}, currentPrice={}",
                event.auctionId(),
                event.currentPrice()
        );
    }
}
