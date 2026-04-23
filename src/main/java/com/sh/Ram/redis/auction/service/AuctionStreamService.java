package com.sh.Ram.redis.auction.service;


import com.sh.Ram.entity.Auction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStreamService {

    private final RedisTemplate<String, String> redisTemplate;

    public void createAuctionStream(Auction auction) {
        Long auctionId = auction.getId();
        String streamKey = "auction:stream:" + auctionId;
        String group = "agent-group"; // consumer-group


        try {
            // 1. 스트림 생성 및 초기 데이터 삽입 (스트림은 데이터가 있어야 그룹 생성이 가능함)
            Map<String, String> initialData = new HashMap<>();
            initialData.put("event", "AUCTION_CREATED");
            initialData.put("auctionId", String.valueOf(auctionId));
            initialData.put("memberId", String.valueOf(auction.getProduct().getMember().getId()));
            initialData.put("startPrice", String.valueOf(auction.getStartPrice())); // 경매 시작가격
            initialData.put("currentPrice", auction.getCurrentPrice() != null ? String.valueOf(auction.getCurrentPrice()) : String.valueOf(auction.getStartPrice())); // 현재 최고 입찰가
            initialData.put("minBidPrice", String.valueOf(calculateBidUnit(auction.getStartPrice()))); // 경매 최소 입찰 단위
            initialData.put("auctionStartDate", String.valueOf(auction.getEndDate())); // 경매 시작 날짜
            initialData.put("auctionEndDate", String.valueOf(auction.getEndDate())); // 경매 종료 날짜

            ObjectRecord<String, Map<String, String>> record = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofObject(initialData);

            redisTemplate.opsForStream().add(record);

            // 2. Consumer Group 생성 (경매 에이전트들이 공유할 그룹)
            // MKSTREAM 옵션을 쓰면 스트림이 없을 때 자동 생성하지만, 위에서 add를 했으므로 안전함
            redisTemplate.opsForStream().createGroup(streamKey, group);

            log.info("[Redis] Stream & Group created for Auction: {}", auction);

        } catch (RedisSystemException e) {
            // 이미 그룹이 존재하는 경우 등의 예외 처리
            log.warn("[Redis] Stream Group already exists for Auction: {}", auctionId);
        }
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