package com.sh.Ram.RAG.embedding.sevice;

import com.sh.Ram.RAG.embedding.dto.EsRegisterProductDto;
import com.sh.Ram.RAG.embedding.dto.WeatherAPIResponseDto;
import com.sh.Ram.auction.repository.AuctionRepository;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.redis.weather.RedisWeatherVector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ProductDocumentRepository productDocumentRepository;
    private final AuctionRepository auctionRepository;
    private final RedisWeatherVector redisWeatherVector;

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

    public List<ProductDto> recommendProductByWeather() {
        List<ProductDocument> documents = productDocumentRepository.findProductsByVector(redisWeatherVector.getWeatherVector());
        List<Long> productIds = documents.stream()
                .map(document -> document.getId())
                .toList();

        Map<Long, AuctionStatus> auctionStatusMap = auctionRepository
                .findAuctionStatusByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (AuctionStatus) row[1]
                ));

        return documents
                .stream()
                .map(productDocument -> {
                    ProductDto productDto = new ProductDto(productDocument);
                    productDto.setAuctionStatus(auctionStatusMap.get(productDocument.getId()));
                    return productDto;
                })
                .toList();
    }
}