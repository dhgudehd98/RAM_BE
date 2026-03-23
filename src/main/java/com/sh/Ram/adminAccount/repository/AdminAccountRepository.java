package com.sh.Ram.adminAccount.repository;

import com.sh.Ram.entity.AdminAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AdminAccount a WHERE a.id = :id")
    AdminAccount findByIdWithLock(@Param("id") Long id);

    @Query("SELECT a FROM AdminAccount a WHERE a.id = :id")
    AdminAccount findById(@Param("id") Long id);
}
