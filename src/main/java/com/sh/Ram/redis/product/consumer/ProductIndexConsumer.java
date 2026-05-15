package com.sh.Ram.redis.product.consumer;


import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.entity.ProductIndexFailLog;
import com.sh.Ram.product.productIndexFailLog.repository.ProductIndexFailLogRepository;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final ProductIndexFailLogRepository productIndexFailLogRepository;
    private static final String STREAM_NAME = "product:index:stream";
    private static final String GROUP_NAME = "product-group";
    private static final String CONSUMER_NAME = "product-index-consumer-1";
    @Override
    public void run(ApplicationArguments args) throws Exception {
        initStream();
        processPendingProduct();
        container.receive(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
                this::handleMessage
        );
    }

    // Stream 초기화 -> Stream 생성 및 Consumer / Consumer-Group 생성
    private void initStream() {
        try {
            log.info("[Product Index Stream & Consumer group Create");
            redisTemplate.opsForStream()
                    .createGroup(STREAM_NAME, ReadOffset.from("0"), GROUP_NAME);
        } catch (Exception e) {
            log.info("[Consumer Group 이미 존재]");
        }
    }


    private void processPendingProduct() {

        /**
         * PendingMessages
         *  - Pending되어 있는 메세지의 메타정보만 가져옴
         *  메타정보 -> messageId (productId에 대한 값은 없음, consumer-name, pending 시간, 재시도 횟수)
         */
        PendingMessages pendingMessages = redisTemplate.opsForStream()
                .pending(STREAM_NAME, Consumer.from(GROUP_NAME, CONSUMER_NAME), Range.unbounded(), 100L);

        //Pending Message가 없는 경우에는 종료
        if(pendingMessages == null || pendingMessages.isEmpty()) return;

        for (PendingMessage message : pendingMessages) {

            List<MapRecord<String, String, String>> range =  (List<MapRecord<String, String, String>>) (List<?>)redisTemplate.opsForStream()
                    .range(STREAM_NAME, Range.closed(
                            message.getId().getValue(),
                            message.getId().getValue()
                    ));

            if(range == null || range.isEmpty()) continue;

            if (message.getTotalDeliveryCount() > 3) {
                log.error("[ES 색인 과정 재시도 횟수 초과] 해당 productId : {} 재시도 횟수 : {} ", message.getId(), message.getTotalDeliveryCount());

                Long productId = Long.parseLong(range.get(0).getValue().get("productId"));
                String originMessageId = message.getId().getValue();
                String action = range.get(0).getValue().get("action");

                // 재시도 횟수가 초과되면 강제적으로 ack 날리고 Pending에서 제거 후 , 색인 실패 DB에 저장 -> 색인 실패한 상품은 수동으로 처리하거나 에러 로그 확인 후 색인 처리
                productIndexFailLogRepository.save(new ProductIndexFailLog(productId, originMessageId, action, "재시도 횟수 초과"));
                redisTemplate.opsForStream().acknowledge(STREAM_NAME, GROUP_NAME, message.getId());
                continue;
            }
            handleMessage(range.get(0));
        }

    }

    private void handleMessage(MapRecord<String, String, String> message) {
        log.info("[HandleMessage] messageId : {} , productId : {}", message.getId(), message.getValue().get("productId"));

        // 상품 action 상태 및 상품 정보 가져오기
        String action = message.getValue().get("action"); // CREATE : 상품 생성 , UPDATE : 상품정보 업데이트 , DELETE : 상품 삭제
        Long productId = Long.parseLong(message.getValue().get("productId"));

        // 상품을 삭제하는 과정에서는 Product를 조회할 필요가 없기 떄문에 productId에 대한 값만 설정
        if(action.equals("DELETE")) {
            deleteProductIndex(productId, message);
            return;
        }

        Product product = productRepository.findByIdWithBrand(productId).get();
        log.info("[Product Index Action] : {}", action);

        switch (action) {
            case "CREATE" -> createProductIndex(product, message);
            case "UPDATE" -> updateProductIndex(product, message);
        }
    }

    private void createProductIndex(Product product, MapRecord<String, String, String> message) {
        log.info("[Create Product Index] productId : {}", String.valueOf(product.getId()));

        try {
            // 상품 상세 설명 -> Embedding 모델을 통해서 벡터화
            float[] vectors = embeddingModel.embed(product.getDescription());

            // ES 객체 생성 및 저장
            ProductDocument productDocument = new ProductDocument(product, vectors);
            productDocumentRepository.save(productDocument);

            redisTemplate.opsForStream()
                    .acknowledge(STREAM_NAME, GROUP_NAME, message.getId());

            log.info("[ES Product Create 완료] : {}  처리 Message Id : {}", message.getId());
        } catch (Exception e) {
            log.error("[ES Product Create 실패] : {}", e.getMessage());
        }
    }

    private void updateProductIndex(Product product, MapRecord<String, String, String> message) {
        log.info("[Update Product Index] productId : {}", String.valueOf(product.getId()));

        try{
            // 상품 상세 설명 -> Embedding 모델을 통해서 벡터화
            float[] vectors = embeddingModel.embed(product.getDescription());

            // ES에서 save => upsert
            ProductDocument productDocument = new ProductDocument(product, vectors);
            productDocumentRepository.save(productDocument);

            redisTemplate.opsForStream()
                    .acknowledge(STREAM_NAME, GROUP_NAME, message.getId());

            log.info("[ES Product Update 완료] 처리 Message Id : {}", message.getId());
        } catch (Exception e) {
            log.error("[ES Product Update 실패] : {}", e.getMessage());
        }

    }

    private void deleteProductIndex(Long productId, MapRecord<String, String, String> message) {
        try{
            productDocumentRepository.deleteById(String.valueOf(productId));

            redisTemplate.opsForStream()
                    .acknowledge(STREAM_NAME, GROUP_NAME, message.getId());

            log.info("[ES Product DELETE 완료] productId : {}", productId);
        } catch (Exception e) {
            log.error("[ES Product DELETE 실패] : {}", e.getMessage());
        }
    }
}