package com.sh.Ram.elasticSearch.product.repository;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String>, ProductEmbeddingQuery, ProductDocumentNativeQuery {

    /**
     * 검색어에 대한 부분이 브랜드와 일치하는 부분이 있으면 브랜드에 가중치 +
     * 검색어가 (브랜드 + 상품명) 합해서 최소 2개는 매칭 되야 데이터 검색
     *
     * minimum_should_match : 서로 다른 검색어 조건 중 몇 종류가 발견되었는가 ? 판별
     *  검색어 : 스투시 -> 토큰 : [stussy , 스투시 , 스튜시], 스튜 , 시
     *  brand : stussy에 대한 부분에서 하나의 토큰이 일치 -> [stussy , 스투시, 스튜시]
     *  name : 여기에서는 [stussy , 스투시, 스튜시]를 제외하고 남은 검색어 조각 "스튜" , "시"에서 하나 더 일치해야함.
     */
    @Query("{" +
            "\"bool\": {" +
            "  \"must\": [" +
            "    {\"multi_match\": {" +
            "      \"query\": \"?0\"," +
            "      \"type\": \"cross_fields\"," +
            "      \"fields\": [\"brand^100\", \"name^10\"]," +
            "      \"minimum_should_match\": \"70%\"" +
            "    }}" +
            "  ]," +
            "  \"should\": [" +
            "    {\"match\": {" +
            "      \"tags\": {\"query\": \"?0\", \"boost\": 5}" +
            "    }}" +
            "  ]" +
            "}" +
            "}")
    List<ProductDocument> searchByKeyword(String keyword);

}
