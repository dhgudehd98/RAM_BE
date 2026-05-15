package com.sh.Ram.product.productIndexFailLog.repository;

import com.sh.Ram.entity.ProductIndexFailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductIndexFailLogRepository extends JpaRepository<ProductIndexFailLog, Long> {
}
