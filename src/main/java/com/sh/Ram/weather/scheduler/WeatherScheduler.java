package com.sh.Ram.weather.scheduler;


import com.sh.Ram.redis.weather.RedisWeatherVector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherScheduler {


    private final WeatherService weatherService;
    private final EmbeddingModel embeddingModel;
    private final RedisWeatherVector redisWeatherVector;

//    @Scheduled(cron = "0 0 * * * *")
    public void updateWeatherVector() {

        log.info("[날씨 + 문구 임베딩 작업 시작]");

        String weatherInfo = weatherService.getWeatherInfo();
        log.info("[Weather Info] : " + weatherInfo);

        // 날씨에 대한 정보 + 추천 상품에 대한 문구 임베딩 모델을 통해 데이터 임베딩
        float[] vectors = embeddingModel.embed(weatherInfo);

        // Redis에 데이터 저장
        redisWeatherVector.setWeatherVector(vectors);
    }
}