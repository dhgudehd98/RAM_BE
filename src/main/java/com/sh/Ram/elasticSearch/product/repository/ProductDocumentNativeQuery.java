package com.sh.Ram.elasticSearch.product.repository;


import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.product.dto.ProductDto;

import java.util.List;

public interface ProductDocumentNativeQuery {

    List<ProductDto> searchByKeywordByNoOffSet(String keyword, List<Object> searchAfter);
}