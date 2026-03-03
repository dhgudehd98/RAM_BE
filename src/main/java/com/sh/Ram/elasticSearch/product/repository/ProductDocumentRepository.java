package com.sh.Ram.elasticSearch.product.repository;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
    @Query("{\"match\": {\"name\": \"?0\"}}")
    List<ProductDocument> findByName(String name);
}
