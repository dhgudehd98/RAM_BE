package com.sh.Ram.product.repository;

import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;

import java.util.List;

public interface ProductRepositoryQuery {

    List<Product> findAllProduct(Long lastId, String sort);
}
