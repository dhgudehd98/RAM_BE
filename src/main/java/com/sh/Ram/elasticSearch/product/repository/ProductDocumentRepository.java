package com.sh.Ram.elasticSearch.product.repository;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * keyword : 검색어
     * -> 검색어와 상품 or 브랜드에 통합하여 일치하는 결과 값 출력
     * -> fuzziness를 활용하여 오타에 대한 정확성 증가
     *  * 검색어 : NIKI -> Nike로 정상적인 데이터 출력
     *  * 검색어 : 아이얩 -> 데이터 출력 불가 , 출력이 안되는 이유는 ElasticSearch에서 오타어를 잡는 알고리즘에서 "앱" -> "얩"으로 변경해서 검색했지만
     *  한국어로 검색했을 떄는 유니코드 값의 차이가 커서 fuzziness가 내부적으로 잡을 수 없음. 해결 방법으로는 동의어 추가 설정하기
     * @param keyword
     * @return
     */
//    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"brand\"], \"fuzziness\": \"AUTO\"}}")

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
            "\"multi_match\": {" +
            "\"query\": \"?0\"," +
            "\"type\": \"cross_fields\"," +
            "\"fields\": [\"brand^50\", \"name^1\"]," +
            "\"minimum_should_match\": \"2<1 3<2\"" + // A < B : 단어 개수가 A개를 초과하면 B개 만큼 맞추기 2개 초과하면 1개잇
            "}" +
            "}")
    List<ProductDocument> searchByKeyword(String keyword);
}
