package com.sh.Ram.elasticSearch.brand.repository;

import com.sh.Ram.elasticSearch.brand.document.BrandDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BrandDocumentRepository extends ElasticsearchRepository<BrandDocument, String> {

}
