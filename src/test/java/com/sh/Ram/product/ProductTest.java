package com.sh.Ram.product;


import com.sh.Ram.entity.Product;
import com.sh.Ram.product.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@SpringBootTest
public class ProductTest {

    @Autowired
    private ProductRepository productRepository;
    @Test
    @DisplayName("Find All Product")
    void findAllProduct() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());

        // when
        Page<Product> productPage = productRepository.findAll(pageable);

        // then
        List<Product> products = productPage.getContent();
        System.out.println("=== 가격 내림차순 ===");
        products.forEach(p -> System.out.println(p.getName() + " : " + p.getPrice()));

        System.out.println("전체 상품 수 : " + productPage.getTotalElements());
        System.out.println("전체 페이지 수 : " + productPage.getTotalPages());
        System.out.println("현재 페이지 상품 수 : " + productPage.getContent().size());

        Assertions.assertThat(products.get(0).getPrice())
                .isGreaterThanOrEqualTo(products.get(1).getPrice());
    }
}