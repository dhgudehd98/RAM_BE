package com.sh.Ram.apick.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApickRepository {

    private static final String CODE_PREFIX = "Apcik:Code:";
    private static final String VERIFIED_PREFIX = "Apcik:Verified:";
    private static final Duration TTL = Duration.ofMinutes(3);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;

    // == CODE ==
    public void saveCode(Long memberId, String code) {
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + memberId, code, TTL);
    }

    public Optional<String> getCode(Long memberId) {
        return Optional.ofNullable(
                stringRedisTemplate.opsForValue().get(CODE_PREFIX + memberId)
        );
    }

    public void deleteCode(Long memberId) {
        stringRedisTemplate.delete(CODE_PREFIX + memberId);
    }

    // == VERIFIED ==
    public void saveVerified(Long memberId) {
        stringRedisTemplate.opsForValue()
                .set(VERIFIED_PREFIX + memberId, "1", VERIFIED_TTL);
    }

    public boolean isVerified(Long memberId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(VERIFIED_PREFIX + memberId)
        );
    }

    public void deleteVerified(Long memberId) {
        stringRedisTemplate.delete(VERIFIED_PREFIX + memberId);
    }
}
