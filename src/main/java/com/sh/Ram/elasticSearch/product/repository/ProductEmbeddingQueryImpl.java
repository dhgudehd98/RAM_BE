package com.sh.Ram.elasticSearch.product.repository;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingQueryImpl implements ProductEmbeddingQuery{

    private final ElasticsearchOperations elasticsearchOperations;
    @Override
    public List<ProductDocument> findProductsByVector(float[] vector) {
        List<Float> vectors = new ArrayList<>();

        for (float f : vector) {
            vectors.add(f);
        }

        // K개의 가장 가까 이웃을 찾는다는 알고리즘을 사용
        KnnQuery knnQuery = KnnQuery.of(k -> k
                .field("descriptionVector") // ES에 정의된 필드명
                .queryVector(vectors)
                .k(50)                       // 가져올 결과 개수
                .numCandidates(100)
        );

        // Spring Data elasticSearch에서 제공하는 쿼리로 ES가 knnQuery를 ES가 이해할 수 있는 쿼리로 변경
        NativeQuery query = NativeQuery.builder()
                .withKnnQuery(knnQuery)
                .withPageable(PageRequest.of(0, 50))
                .build();

        // SearchHits : 작성한 쿼리를 바탕으로 ES 검색 요청
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
