package com.sh.Ram.elasticSearch.product.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;


@RequiredArgsConstructor
public class ProductDocumentNativeQueryImpl implements ProductDocumentNativeQuery{

    private final ElasticsearchOperations operations;

    @Override
    public List<ProductDto> searchByKeywordByNoOffSet(String keyword,  List<Object> searchAfter) {
        // 동적 쿼리 빌더 사용
        NativeQuery query = NativeQuery.builder()
                // Query 설정
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(keyword)
                                                .fields("brand^100", "name^10")
                                                .type(TextQueryType.CrossFields)
                                                .minimumShouldMatch("70%")
                                        )
                                )
                                .should(s -> s
                                        .match(ma -> ma
                                                .field("tags")
                                                .query(keyword)
                                                .boost(5f)
                                        )
                                )
                        )
                )
                // 2. 정렬 설정 - 스코어에대한 값 기준으로 내림차순 , 스코어에 대한 부분이 동일하다면 id에 대한 값은 오름차순으로 설정
                .withSort(s -> s
                        .field(f -> f.field("_score").order(SortOrder.Desc))
                )
                .withSort(s -> s
                        .field(f -> f.field("id").order(SortOrder.Asc))
                )
                // 페이징 설정
                .withPageable(PageRequest.of(0, 10))
                .build();

        if (searchAfter != null && !searchAfter.isEmpty()) {
            query.setSearchAfter(searchAfter);
        }

        SearchHits<ProductDocument> searchHits = operations.search(query, ProductDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(hit -> {
                    ProductDto dto = new ProductDto(hit.getContent());
                    dto.setScore(hit.getScore());
                    return dto;
                })
                .toList();
    }
}