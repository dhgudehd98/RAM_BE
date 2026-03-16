package com.sh.Ram.redis.login;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLoginToken {

    private final StringRedisTemplate redisTemplate;

    private static final String accessToken = "accessToken";

    public void setAccessToken(String token, Long memberId) {
        redisTemplate.opsForValue().set(
                accessToken + ":" + memberId,
                token,
                24, TimeUnit.HOURS
        );
    }

    public String getAccessToken(Long memberId) {
        return redisTemplate.opsForValue().get(accessToken + ":" + memberId);
    }

    public void deleteAccessToken(Long memberId) {
        redisTemplate.delete(accessToken + ":" + memberId);
    }
}