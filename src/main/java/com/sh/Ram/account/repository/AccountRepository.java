package com.sh.Ram.account.repository;

import com.sh.Ram.account.dto.AccountDto;
import com.sh.Ram.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Account findByMemberIdWithLock(@Param("id") Long id);

    @Query("SELECT a From Account a WHERE a.member.id = :id")
    Optional<Account> findByMemberId(@Param("id") Long id);

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

    @Query("SELECT a From Account a WHERE a.member.id = :memberId")
    boolean existsByMemberId(@Param("memberId") Long memberId);

    /**
     * 가용 잔액이 충분할 때만 예약금 증가
     * 성공하면 1, 실패하면 0 반환
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Account a
            set a.reservedBalance = a.reservedBalance + :amount
            where a.member.id = :memberId
            and (a.accountBalance - a.reservedBalance) >= :amount
            """)
    int increaseReservedBalanceIfAvailable(
            @Param("memberId") Long memberId,
            @Param("amount") Long amount
    );

    /**
     * 예약금 감소
     * 성공하면 1, 실패하면 0 반환
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Account a
            set a.reservedBalance = a.reservedBalance - :amount
            where a.member.id = :memberId
            and a.reservedBalance >= :amount
           """)
    int decreaseReservedBalance(
            @Param("memberId") Long memberId,
            @Param("amount") Long amount
    );
}
