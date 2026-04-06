package com.sh.Ram.RAG.embedding.sevice;

import com.sh.Ram.RAG.embedding.dto.EsRegisterProductDto;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final ProductDocumentRepository productDocumentRepository;

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
}