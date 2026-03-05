package com.sh.Ram.product;


import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class SaveElasticSearch  {
    private final ProductDocumentRepository documentRepository;
    private final ProductRepository repository;


    public void run() {
        List<Product> products = repository.findAll();

        for (Product product : products) {

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