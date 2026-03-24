package com.sh.Ram.account.repository;

import com.sh.Ram.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Account findByMemberIdWithLock(@Param("id") Long id);

    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Account findByMemberId(@Param("id") Long id);
}
