package com.sh.Ram.redis.login;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLoginToken {

    private final StringRedisTemplate redisTemplate;

    private static final String refreshToken = "refreshToken";

    public void setRefreshToken(String token, Long memberId) {
        redisTemplate.opsForValue().set(
                refreshToken + ":" + memberId,
                token,
                7, TimeUnit.DAYS
        );
    }

    public Optional<String> getRefreshToken(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(refreshToken + ":" + memberId));
    }

    public void deleteAccessToken(Long memberId) {
        redisTemplate.delete(refreshToken + ":" + memberId);
    }
}