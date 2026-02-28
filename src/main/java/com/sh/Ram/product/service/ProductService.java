package com.sh.Ram.product.service;

import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    public Page<ProductDto> findAllProduct(String sort) {

        Pageable pageable;

        // 정렬 조건에 따라서 Paging 분류 방법 다르게
        if (sort != null) pageable = PageRequest.of(0, 10, Sort.by(sort).descending());
        else {
            log.info("여기 들어오는데");
            pageable = PageRequest.of(0, 10, Sort.by("name").descending());
        }

        return productRepository.findAll(pageable)
                .map(ProductDto::from);

    }
}