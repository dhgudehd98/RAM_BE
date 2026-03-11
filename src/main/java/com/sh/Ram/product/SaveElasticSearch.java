package com.sh.Ram.product;


import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class SaveElasticSearch  {
    private final ProductDocumentRepository documentRepository;
    private final ProductRepository repository;


//    @Bean
    @Transactional
    public void run() {
        List<Product> products = repository.findAll();

        for (Product product : products) {
            log.info("=== 상품 정보 ====");
            log.info(product.toString());
            Long memberId = 1L;
            ProductDocument document = new ProductDocument(
                    String.valueOf(product.getId()),
                    memberId,
                    product.getName(),
                    product.getBrand().getBrandName(),
                    product.getPrice(),
                    product.getImageUrl());

            documentRepository.save(document);
        }
    }
}