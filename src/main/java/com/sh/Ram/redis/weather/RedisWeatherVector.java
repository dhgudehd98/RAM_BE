package com.sh.Ram.redis.weather;

import com.querydsl.core.annotations.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisWeatherVector {

    private final RedisTemplate<String, Object> weatherRedisTemplate;
    private static final String WEATHER_KEY = "Weather:Vector";

    // Vector에 대한값 저장
    public void setWeatherVector(float[] vectors) {
        weatherRedisTemplate.opsForValue().set(WEATHER_KEY, vectors);
    }

    // Vector에 대한값 가져오기
    public float[] getWeatherVector() {
        Object vectors = weatherRedisTemplate.opsForValue().get(WEATHER_KEY);

        if (vectors instanceof List<?>) {
            List<?> list = (List<?>) vectors;
            float[] vectorArray = new float[list.size()];

            for (int i = 0; i < list.size(); i++) {
                vectorArray[i] = ((Number) list.get(i)).floatValue();
            }

            return vectorArray;
        }

        if (vectors instanceof float[]) {
            return (float[]) vectors;
        }

        return null;
    }


}