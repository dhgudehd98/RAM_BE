package com.sh.Ram.elasticSearch.product.repository;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

    // 입력한 값 => 상품 + 브랜드에 대한 부분으로 검색
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"brand\"], \"fuzziness\": \"AUTO\"}}")
    List<ProductDocument> searchByKeyword(String keyword);
}
