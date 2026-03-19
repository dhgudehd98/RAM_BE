package com.sh.Ram.websocket.publisher;

import com.sh.Ram.websocket.dto.BidUpdatedMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionWebsocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishBidUpdated(BidUpdatedMessageDto message) {
        messagingTemplate.convertAndSend(
                "/topic/auctions/" + message.auctionId(),
                message
        );
    }
}
