package com.sh.Ram.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.sh.Ram.Auction.repository.AuctionRepository;
import com.sh.Ram.Auction.service.AuctionService;
import com.sh.Ram.elasticSearch.brand.document.BrandDocument;
import com.sh.Ram.elasticSearch.brand.repository.BrandDocumentRepository;
import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.repository.ProductRepository;
import com.sh.Ram.ranking.dto.RankingDto;
import com.sh.Ram.redis.searchRanking.RedisRealTimeSearchRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final RedisRealTimeSearchRanking redisRanking;
    private final AuctionRepository auctionRepository;

    private final ElasticsearchClient elasticsearchClient;


    public List<ProductDto> searchByKeyword(String keyword) {

        //Redis에 keyword 저장
        redisRanking.setKeyword(keyword);

        List<ProductDocument> documents = productDocumentRepository.searchByKeyword(keyword);
        List<Long> productIds = documents.stream()
                .map(document -> Long.parseLong(document.getId()))
                .collect(Collectors.toList());

        Map<Long, AuctionStatus> auctionMap = auctionRepository
                .findAuctionStatusByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (AuctionStatus) row[1]
                ));

        return documents
                .stream()
                .map(document -> {
                    ProductDto productDto = new ProductDto(document);
                    productDto.setAuctionStatus(auctionMap.get(Long.parseLong(document.getId())));
                    return productDto;
                })
                .collect(Collectors.toList());

    }

    // 검색어 자동완성 기능 -> 브랜드 별로 자동완성하고 , 상품으로는 자동완성하지 않음
    public List<BrandDocument> autoCompletion(String prefix) throws IOException {
        /**
         * 자동 완성 요청 형식
         * "suggest": {
         *     "brand_suggest": {
         *       "prefix": "아이앱",
         *       "completion": {
         *         "field": "suggest",
         *         "size": 5
         *       }
         *     }
         *   }
         *
         *   searchRequest : searchRequestBuilder 전체 검색 요청 -> 위의 JSON 형식
         *   suggestBUilder : 자동 완성 요청 묶음
         *   su : Field Suggester Builder -> 특정 필드에 대한 자동 완성 설정
         *   c : completion Suggester Builder -> completion 타입 세부 설정
         */
        SearchResponse<BrandDocument> response = elasticsearchClient.search(searchRequest -> searchRequest
                        .index("brands") // "brands" Index에서 검색
                        .suggest(suggestBuilder -> suggestBuilder
                                // 여기서 suggeter에 대한 요청 값 만들기 key 값은 brand_suggest
                                .suggesters("brand_suggest", su -> su
                                        .prefix(prefix)
                                        .completion(c -> c
                                                .field("suggest")
                                                .size(5)
                                        )
                                )
                        ),
                BrandDocument.class
        );

        return response.suggest()
                .get("brand_suggest")
                .stream()
                .flatMap(s -> s.completion().options().stream())
                .map(option -> option.source())
                .collect(Collectors.toList());
    }


    public List<RankingDto> getKeywordRanking() {
        return redisRanking.getKeywordRanking();
    }

    public List<ProductDto> searchByKeywordByDB(String keyword) {

        long start = System.currentTimeMillis();
        List<Product> productList = productRepository.findProductByKeyword(keyword);

        List<Long> productIds = productList.stream().map(product -> product.getId()).collect(Collectors.toList());

        Map<Long , AuctionStatus> auctionMap = auctionRepository
                .findAuctionStatusByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (AuctionStatus) row[1]
                ));

        List<ProductDto> result = productList.stream()
                .map(product -> {
                    ProductDto dto = new ProductDto(product);
                    dto.setAuctionStatus(auctionMap.get(product.getId()));

                    return dto;

                })
                .collect(Collectors.toList());

        long end = System.currentTimeMillis();

        log.info("Time To End : " + (end - start));

        return result;
    }
}