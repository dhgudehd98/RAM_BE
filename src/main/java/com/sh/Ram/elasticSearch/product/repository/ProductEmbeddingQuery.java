package com.sh.Ram.elasticSearch.product.repository;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;

import java.util.List;

public interface ProductEmbeddingQuery {

    List<ProductDocument> findProductsByVector(float[] vector);
}
