package com.sh.Ram.account.repository;

import com.sh.Ram.account.dto.AccountDto;
import com.sh.Ram.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Account findByMemberIdWithLock(@Param("id") Long id);

    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Account findByMemberId(@Param("id") Long id);

    @Query("""
        select new com.sh.Ram.account.dto.AccountDto(
            a.id,
            a.bankName,
            a.bankCode,
            a.accountNum,
            a.accountBalance
        )
        from Account a
        where a.member.id = :memberId
""")
    AccountDto findByMemberId2(@Param("memberId") Long memberId);

    @Query(
        """
        select a
        from Account a
        join a.member m
        where a.id = :accountId and m.id = :memberId
        """
    )
    Optional<Account> findByIdAndMemberId(
            @Param("accountId") Long accountId,
            @Param("memberId") Long memberId
    );

    @Query(
    """
    select a
    from Account a
    join a.member m
    where a.accountNum = :accountNum and m.id = :memberId
    """
    )
    boolean existsByMemberIdAndAccountNum(
            @Param("memberId") Long memberId,
            @Param("accountNum") String accountNum);

}
