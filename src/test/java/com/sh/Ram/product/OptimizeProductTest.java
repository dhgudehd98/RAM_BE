package com.sh.Ram.product;


import com.querydsl.core.QueryResults;
import com.sh.Ram.entity.Product;
import com.sh.Ram.entity.QProduct;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.sh.Ram.entity.QProduct.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class OptimizeProductTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;
    @Test
    @DisplayName("QueryDSL을 사용하여 모든 상품 조회하기")
    void findAllProductByQueryDSL() {
        int page = 0 ;
        int size = 10;
        Long lastId = 11L;
        jpaQueryFactory = new JPAQueryFactory(em);

        List<Product> products = jpaQueryFactory
                .selectFrom(product)
                .join(product.brand).fetchJoin()
                .where(
                        product.onSale.eq(false),
                        lastId != null ? product.id.lt(lastId) : null
                )
                .orderBy(product.id.desc())
                .limit(size)
                .fetch();

        System.out.println("조회된 상품 수 : " + products.size());
        for (Product product : products) {
            System.out.println(" 상품명 : " + product.getName());
        }
        Product p = products.get(0);
        System.out.println("Product Id : " + p.getId());
        Assertions.assertThat(p.getId()).isEqualTo(11L);
        Assertions.assertThat(products.size()).isEqualTo(10);
    }
}