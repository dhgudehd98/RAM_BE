package com.sh.Ram.product;

import com.sh.Ram.RAG.embedding.sevice.EmbeddingService;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class SaveElasticSearch  {
    private final ProductDocumentRepository documentRepository;
    private final ProductRepository repository;
    private final EmbeddingModel embeddingModel;


//    @Bean
    @Transactional
    public void run() {
        List<Product> products = repository.findAll();
        List<String> list = new ArrayList<>();
        list.add("후드집업");
        list.add("후드티");
        list.add("상의");
        int count = 1;
        for (Product product : products) {

            if(count == 301) break;

            Long memberId = 4L;
            ProductDocument document = new ProductDocument(
                    product,
                    list
            );

            document.setDescriptionVector(embeddingModel.embed(product.getDescription()));
            documentRepository.save(document);

            count ++;
        }
    }
}