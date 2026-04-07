package com.sh.Ram.RAG.embedding.sevice;

import com.sh.Ram.RAG.embedding.dto.EsRegisterProductDto;
import com.sh.Ram.RAG.embedding.dto.WeatherAPIResponseDto;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ProductDocumentRepository productDocumentRepository;

    @Value("${weather.apiKey}")
    private String apiKey;

    @Async("embeddingExecutor")
    public void embedAndSave(EsRegisterProductDto esRegisterProductDto) {
        try {

            ProductDocument document = new ProductDocument(esRegisterProductDto);

            // embedding-model을 통해서 description에 대한 부분을 embedding
            float[] vector = embeddingModel.embed(esRegisterProductDto.getDescription());
            document.setDescriptionVector(vector);

            //ES에 저장
            productDocumentRepository.save(document);
            log.info("[Embedding] 벡터 저장 완료 - productId: {}", document.getId());
        } catch (Exception e) {
            log.error("[Embedding] 벡터 저장 실패 에러 메세지 : {}", e.getMessage());
        }
    }

    public void recommendProductByWeather(String embeddedKeyword) {
        /**
         * 1. 날씨 API 요청
         * 2. 날씨 API 요청 응답 값 + embeddedKeyword 값 기반으로 Embedding
         * 3. Embedding된 벡터값을 바탕으로 ES에 저장되어 있는 상품에서 description Vector값 비교 후 추출
         * 4. 추출된 상품 return
         */
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime baseTime = date.minusMinutes(30); // 날씨는 정각 기준으로 출력을 해주기 때문에 30분전 기준으로 시간 설

        String baseDate = baseTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTimeStr = baseTime.format(DateTimeFormatter.ofPattern("HH")) + "00";

        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 10)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTimeStr)
                .queryParam("nx", 60)
                .queryParam("ny", 127);

        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = uriBuilder.build(false).toUriString();

        WeatherAPIResponseDto weatherAPIResponseDto = restTemplate.getForObject(requestUrl, WeatherAPIResponseDto.class);

        System.out.println("==== Weather Response API ====");
        System.out.println(weatherAPIResponseDto.toString());
        System.out.println("==============================");

        if (weatherAPIResponseDto != null && weatherAPIResponseDto.response().body() != null) {
            String temp = weatherAPIResponseDto.response().body().items().item().stream()
                    .filter(it -> "T1H".equals(it.category()))
                    .map(it -> it.obsrValue())
                    .findFirst()
                    .orElse("0.0");

            String ptyCode = weatherAPIResponseDto.response().body().items().item().stream()
                    .filter(it -> "PTY".equals(it.category()))
                    .map(it -> it.obsrValue())
                    .findFirst().get();

            System.out.println("강수형태 : " + ptyCode);
            System.out.println("추출된 기온: " + temp);

            String finalQuery = String.format("현재 기온은 %s도이고, 날씨는 %s입니다. %s",
                    temp, convertPtyCode(ptyCode), embeddedKeyword);

            System.out.println("======Embedding Query =====");
            System.out.println(finalQuery);
        }

    }

    private String convertPtyCode(String code) {
        return switch (code) {
            case "1" -> "비가오는 날씨";
            case "2" -> "비나 눈이 섞여 오는";
            case "3" -> "눈이 오는";
            case "4" -> "소나기가 내리는";
            default -> "맑은(비 안오는)";
        };
    }
}