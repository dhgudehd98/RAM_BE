package com.sh.Ram.redis.searchRanking;

import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.ranking.dto.RankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisRealTimeSearchRanking {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String PREV_KEY = "search:keyword:prev";
    private static final String CUR_KEY = "search:keyword:cur";
    private static final String NEW_KEY = "search:keyword:new";




    // 검색을 했을 떄는 CUR_KEY에 저장하도록 설정
    public void setKeyword(String keyword) {
        stringRedisTemplate.opsForZSet().incrementScore(CUR_KEY, keyword, 1);
    }

    // 실시간 검색 순위 가져오기
    public List<RankingDto> getKeywordRanking() {
        Set<String> prevRanking = stringRedisTemplate.opsForZSet().reverseRange(PREV_KEY, 0, 9);
        Set<String> newRanking = stringRedisTemplate.opsForZSet().reverseRange(NEW_KEY, 0, 9);

        return prevRanking.stream()
                .map(keyword -> new RankingDto(
                        keyword,
                        newRanking.contains(keyword) ? "-" : "New"
                ))
                .collect(Collectors.toList());

    }

//    @Scheduled(cron = "0 0 * * * *") // 매정시마다 변경
//    @Scheduled(cron = "0 * * * * *") // 매분마다 변경
    public void rotate() {
        // 1시간마다 실시간 검색어 -> 1시간 전 검색어로 변경
        stringRedisTemplate.rename(PREV_KEY, NEW_KEY);
        stringRedisTemplate.rename(CUR_KEY, PREV_KEY);

    }


}