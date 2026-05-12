package com.sh.Ram.redis.product.consumer;


import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductIndexConsumer implements ApplicationRunner {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final StringRedisTemplate redisTemplate;
    private final EmbeddingModel embeddingModel;
    private static final String STREAM_KEY = "product:index:stream";
    private static final String GROUP_NAME = "product-group";
    @Override
    public void run(ApplicationArguments args) throws Exception {
        initStream();

        container.receive(
                Consumer.from(GROUP_NAME, "product-index-consumer-1"),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                this::handleMessage
        );
    }

    private void handleMessage(MapRecord<String, String, String> message) {

        try {
            // 상품 정보 가져 오기
            Long productId = Long.parseLong(message.getValue().get("productId"));
            Product product = productRepository.findByIdWithBrand(productId).get();

            log.info("[Redis Stream In Product Info] : " + productId);
            // 상품 상세 설명 -> Embedding 모델을 통해서 벡터화
            float[] vectors = embeddingModel.embed(product.getDescription());

            // ES 객체 생성 및 저장
            ProductDocument productDocument = new ProductDocument(product, vectors);
            productDocumentRepository.save(productDocument);

            redisTemplate.opsForStream()
                    .acknowledge(STREAM_KEY, GROUP_NAME, message.getId());

            log.info("[ES 색인 과정 성공] : {}  처리 Message Id : {}", message.getId());
        } catch (Exception e) {
            log.error("[ES 색인 과정 실패] : {}", e.getMessage());
        }
    }

    // Stream 초기화 -> Stream 생성 및 Consumer / Consumer-Group 생성
    private void initStream() {
        try {
            log.info("[Product Index Stream & Consumer group Create");
            redisTemplate.opsForStream()
                    .createGroup(STREAM_KEY, ReadOffset.from("0"), GROUP_NAME);
        } catch (Exception e) {
            log.info("[Consumer Group 이미 존재]");
        }
    }
}