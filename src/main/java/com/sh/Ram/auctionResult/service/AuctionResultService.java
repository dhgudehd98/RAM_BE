package com.sh.Ram.auctionResult.service;

import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.accountHistory.AccountHistoryRepository;
import com.sh.Ram.adminAccount.repository.AdminAccountRepository;
import com.sh.Ram.common.exception.auctionResult.AuctionResultException;
import com.sh.Ram.entity.Account;
import com.sh.Ram.entity.AccountHistory;
import com.sh.Ram.entity.AdminAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionResultService {

    private final AccountRepository accountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final AccountHistoryRepository accountHistoryRepository;

    /**
     * 낙찰 시 IN 처리
     * buyer -> AdminAccount 계좌
     */
    @Transactional
    public void processIn(Long buyerId, Long price) {

        Account buyer = accountRepository.findByMemberIdWithLock(buyerId);
        AdminAccount admin = adminAccountRepository.findByIdWithLock(1L);

        if (buyer.getAccountBalance() < price) {
            throw new AuctionResultException("계좌 잔액이 부족합니다.");
        }

        buyer.setAccountBalance(buyer.getAccountBalance() - price);
        admin.setBalance(admin.getBalance() + price);

        /**
         * 계좌 히스토리 등록
         */
        AccountHistory history = new AccountHistory(
                buyer,
                price,
                "OUT"
        );

        accountHistoryRepository.save(history);
    }

    /**
     * 정산 시 OUT 처리
     * AdminAccount 계좌 -> seller
     */
    @Transactional
    public void processOut(Long sellerId, Long price) {

        Account seller = accountRepository.findByMemberIdWithLock(sellerId);
        AdminAccount admin = adminAccountRepository.findByIdWithLock(1L);

        if (admin.getBalance() < price) {
            throw new AuctionResultException("관리용 계좌 잔액이 부족합니다.");
        }

        admin.setBalance(admin.getBalance() - price);
        seller.setAccountBalance(seller.getAccountBalance() + price);

        /**
         * 계좌 히스토리 등록
         */
        AccountHistory history = new AccountHistory(
                seller,
                price,
                "IN"
        );

        accountHistoryRepository.save(history);
    }
}
