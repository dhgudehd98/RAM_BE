package com.sh.Ram.accountHistory.repository;

import com.sh.Ram.entity.AccountHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {

    @Query("""
            select ah
            from AccountHistory ah
            where ah.account.member.id = :memberId
            and (:type is null or ah.type = :type)
            """)
    Page<AccountHistory> findMyAccountHistory(
            @Param("memberId") Long memberId,
            @Param("type") String type,
            Pageable pageable
    );
}
