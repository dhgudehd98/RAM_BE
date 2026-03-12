package com.sh.Ram.product.repository;

import com.sh.Ram.entity.Auction;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.brand b ",
            countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAll(Pageable pageable);


//    @Query("SELECT p FROM Product p WHERE p.brand.brandName LIKE %:keyword% OR p.name LIKE %:keyword%")
    @Query("SELECT p FROM Product p WHERE p.brand.brandName LIKE %:keyword% OR p.name LIKE %:keyword%")
    List<Product> findProductByKeyword(@Param("keyword") String keyword);
}
