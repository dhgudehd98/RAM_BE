package com.sh.Ram.redis.auction.service;


import com.sh.Ram.redis.auction.dto.AuctionRealtimeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionRedisCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PREFIX = "auction:";
    private static final String SUFFIX = ":state";

    private String generateKey(Long auctionId) {
        return PREFIX + auctionId + SUFFIX;
    }

    /**
     * Redis 상태 조회
     */
    public Optional<AuctionRealtimeDto> getAuctionState(Long auctionId) {

        String key = generateKey(auctionId);

        Map<Object, Object> values =
                stringRedisTemplate.opsForHash().entries(key);

        if (values.isEmpty()) {
            return Optional.empty();
        }

        AuctionRealtimeDto state = AuctionRealtimeDto.builder()
                .currentPrice(Integer.valueOf((String) values.get("currentPrice")))
                .highestBidderNickname((String) values.get("highestBidderNickname"))
                .lastBidTime(
                        values.get("lastBidTime") == null
                                ? null
                                : LocalDateTime.parse((String) values.get("lastBidTime"))
                )
                .nextBidPrice(Integer.valueOf((String) values.get("nextBidPrice")))
                .auctionStatus((String) values.get("auctionStatus"))
                .build();

        return Optional.of(state);
    }

    /**
     * Redis 상태 업데이트
     */
    public void updateAuctionState(Long auctionId,
                                   AuctionRealtimeDto state,
                                   LocalDate endDate) {

        String key = generateKey(auctionId);

        Map<String, String> values = new HashMap<>();

        values.put("currentPrice", String.valueOf(state.getCurrentPrice()));
        values.put("highestBidderNickname", state.getHighestBidderNickname());
        values.put("lastBidTime",
                state.getLastBidTime() == null ? "" : state.getLastBidTime().toString());
        values.put("nextBidPrice", String.valueOf(state.getNextBidPrice()));
        values.put("auctionStatus", state.getAuctionStatus());

        stringRedisTemplate.opsForHash().putAll(key, values);

        long seconds = calculateExpireSeconds(endDate);

        if (seconds > 0) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(seconds));
        }
    }

    /**
     * 경매 종료 시 Redis 삭제
     */
    public void deleteAuctionState(Long auctionId) {
        stringRedisTemplate.delete(generateKey(auctionId));
    }

    private long calculateExpireSeconds(LocalDate endDate) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime expireTime = endDate.atStartOfDay().plusHours(1);

        Duration duration = Duration.between(now, expireTime);

        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }

        return duration.getSeconds();
    }
}