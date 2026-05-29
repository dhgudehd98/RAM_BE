//package com.sh.Ram.redis.auction.service;
//
//
//import com.sh.Ram.agent.auctionAgent.tracking.AuctionAgentTracking;
//import com.sh.Ram.entity.Auction;
//import com.sh.Ram.entity.AuctionAgent;
//import com.sh.Ram.entity.Bid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.RedisSystemException;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AuctionStreamService {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final AuctionAgentTracking auctionAgentTracking;
//    private final StreamMessageListenerContainer<String, MapRecord<String,String,String>> listenerContainer;
//
//    public void createAuctionStream(Auction auction) {
//        Long auctionId = auction.getId();
//        String streamKey = "auction:stream:" + auctionId;
//
//        try {
//            // 1. 스트림 생성 및 초기 데이터 삽입 (스트림은 데이터가 있어야 그룹 생성이 가능함)
//            Map<String, String> initialData = new HashMap<>();
//            initialData.put("event", "AUCTION_CREATED");
//            initialData.put("auctionId", String.valueOf(auctionId));
//            initialData.put("memberId", String.valueOf(auction.getProduct().getMember().getId()));
//            initialData.put("startPrice", String.valueOf(auction.getStartPrice())); // 경매 시작가격
//            initialData.put("currentPrice", auction.getCurrentPrice() != null ? String.valueOf(auction.getCurrentPrice()) : String.valueOf(auction.getStartPrice())); // 현재 최고 입찰가
//            initialData.put("minBidPrice", String.valueOf(calculateBidUnit(auction.getStartPrice()))); // 경매 최소 입찰 단위
//            initialData.put("auctionStartDate", String.valueOf(auction.getEndDate())); // 경매 시작 날짜
//            initialData.put("auctionEndDate", String.valueOf(auction.getEndDate())); // 경매 종료 날짜
//
//            MapRecord<String, String, String> record = StreamRecords.newRecord()
//                    .in(streamKey)
//                    .ofMap(initialData);
//
//            redisTemplate.opsForStream().add(record);
//
//            // 2. Consumer-group 생성
//            createConsumerGroup(streamKey, auctionId);
//
//            log.info("[Redis] Stream & Group created for Auction: {}", auction);
//
//        } catch (RedisSystemException e) {
//            // 이미 그룹이 존재하는 경우 등의 예외 처리
//            log.warn("[Redis] Stream Group already exists for Auction: {}", auctionId);
//        }
//    }
//
//    // Consumer-group 생성
//    private void createConsumerGroup(String streamKey, Long auctionId) {
//        String groupName = "coordinator-group";
//        String consumerName = "coordinator-" + auctionId;
//
//        try {
//            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName); // consumer-group 생성
//        } catch (Exception e) {
//            log.warn("[Error Exception in create Consumer grouo] : {}", e.getMessage());
//        }
//
//        // 해당 Redis-Stream에 이벤트 리스너 설정
//        listenerContainer.receive(
//                Consumer.from(groupName, consumerName), // 1. 누가 읽는지
//                StreamOffset.create(streamKey, ReadOffset.from("0")), // 2. 어디서부터 읽는지
//                auctionAgentTracking // 3. 어떤 리스너가 처리할지
//        );
//
//        log.info("[Redis] listenerContainer 실행 상태 (receive 후): {}", listenerContainer.isRunning()); // 추가
//        log.info("[Redis] 리스너 등록 완료 - consumer: {}", consumerName);
//    }
//
//    public void registerAgentToStream(AuctionAgent auctionAgent) {
//        String agentKey = "auction:" + auctionAgent.getAuction().getId() + ":agents:" + auctionAgent.getId(); // key : auction:경매번호:agents:에이전트번호
//        double maxBudget = auctionAgent.getMaxBudget();
//        String memberId = auctionAgent.getId() + ":" + auctionAgent.getMember().getId();
//
//        redisTemplate.opsForZSet().add(agentKey, memberId, maxBudget);
//
//        log.info("[Redis] Agent Sorted Set 등록 - auctionId: {}, agentId: {}, maxBudget: {}",
//                auctionAgent.getAuction().getId(),
//                auctionAgent.getId(),
//                auctionAgent.getMaxBudget());
//    }
//
//    public void saveBidEventInStream(Bid bid) {
//        String streamKey = "auction:stream:" + bid.getAuction().getId();
//
//        Map<String, String> bidData = new HashMap<>();
//        bidData.put("event", "BID");
//        bidData.put("bidId", String.valueOf(bid.getId()));
//        bidData.put("auctionId", String.valueOf(bid.getAuction().getId()));
//        bidData.put("bidPrice", String.valueOf(bid.getBidPrice()));
//        bidData.put("bidTime", LocalDateTime.now().toString());
//
//        MapRecord<String, String, String> record = StreamRecords.newRecord()
//                .in(streamKey)
//                .ofMap(bidData);
//
//        // 3. Redis Stream에 추가 (XADD 명령 실행)
//        this.redisTemplate.opsForStream().add(record);
//
//        log.info("[Redis] Bid Event 추가 완료 - Stream: {}, Price: {}", streamKey, bid.getBidPrice());
//    }
//
//    private Integer calculateBidUnit(Integer price) {
//
//        if (price < 10_000) return 100;
//        if (price < 50_000) return 500;
//        if (price < 100_000) return 1_000;
//        if (price < 500_000) return 5_000;
//        if (price < 1_000_000) return 10_000;
//        if (price < 5_000_000) return 50_000;
//        if (price < 10_000_000) return 100_000;
//
//        return 500_000;
//    }
//
//}