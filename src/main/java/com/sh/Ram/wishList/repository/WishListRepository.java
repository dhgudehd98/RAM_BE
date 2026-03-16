package com.sh.Ram.wishList.repository;

import com.sh.Ram.entity.Member;
import com.sh.Ram.entity.Product;
import com.sh.Ram.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    Optional<WishList> findByMemberAndProduct(Member member, Product product);

    int countByProductId(Long productId);

    List<WishList> findByProductId(Long productId);
}
