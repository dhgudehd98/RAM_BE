package com.sh.Ram.weather.scheduler;


import com.sh.Ram.RAG.embedding.dto.WeatherAPIResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.apiKey}")
    private String weatherApiKey;

    public String getWeatherInfo() {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder builder = new StringBuilder();

        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTimeStr = now.format(DateTimeFormatter.ofPattern("HH")) + "00";

        // 날씨 URL 연결 - 초단기실황조회
        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("serviceKey", weatherApiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 10)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTimeStr)
                .queryParam("nx", 60)
                .queryParam("ny", 127);

        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = uriBuilder.build(false).toUriString();

        // 날씨 정보 API 요청
        WeatherAPIResponseDto weatherAPIResponseDto = restTemplate.getForObject(requestUrl, WeatherAPIResponseDto.class);

        if (weatherAPIResponseDto != null && weatherAPIResponseDto.response().body() != null) {

            // 기온값 가져오기
            String temperature = weatherAPIResponseDto.response().body().items().item().stream()
                    .filter(it -> "T1H".equals(it.category()))
                    .map(it -> it.obsrValue())
                    .findFirst()
                    .orElse("0.0");

            log.info("[Weather Info Temperature] : Temperature" + temperature);

            //강수형태 가져오기 0: 맑음(비 안오는 날씨) 1 : 비오는 날씨 , 2 : 비 + 눈 , 3: 눈이 오는 날씨 , 4 : 소나기
            String ptyCode = weatherAPIResponseDto.response().body().items().item().stream()
                    .filter(it -> "PTY".equals(it.category()))
                    .map(it -> it.obsrValue())
                    .findFirst().get();

            log.info("[Weather Info PTY] : " + ptyCode);


            String finalQuery = String.format("현재 기온은 %s도이고, 날씨는 %s입니다. %s",
                    temperature, convertPtyCode(ptyCode), "현재 날씨 기반으로 상품 추천해줘");

            System.out.println("[Embedding Query] : " + finalQuery);
            builder.append(finalQuery);
        }


        return builder.toString();
    }


    private String convertPtyCode(String ptyCode) {
        return switch (ptyCode){
            case "1" -> "비가오는 날씨";
            case "2" -> "비나 눈이 섞여 오는";
            case "3" -> "눈이 오는";
            case "4" -> "소나기가 내리는";
            default -> "맑은(비 안오는)";

        };
    }
}