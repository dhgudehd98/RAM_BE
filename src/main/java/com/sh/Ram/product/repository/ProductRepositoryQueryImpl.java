package com.sh.Ram.product.repository;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.Ram.entity.Product;
import com.sh.Ram.entity.QProduct;
import com.sh.Ram.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sh.Ram.entity.QProduct.*;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryQueryImpl implements ProductRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<Product> findAllProduct(Long lastId, String sort) {
        return jpaQueryFactory
                .selectFrom(product)
                .join(product.brand).fetchJoin()
                .where(
                        product.onSale.eq(false),
                        lastId != null ? product.id.lt(lastId) : null
                )
                .orderBy(
                        getOrderBy(sort)
                )
                .limit(10)
                .fetch();
    }

    // 사용자가 요청한 상품 정렬에 대한 값 바탕으로 정렬 기준값 변경 
    private OrderSpecifier<?> getOrderBy(String sort) {
        if(sort == null) return product.id.desc();

        return switch (sort) {
            case "price" -> product.price.desc();
            case "name" -> product.name.desc();
            default -> product.id.desc();
        };
    }
}