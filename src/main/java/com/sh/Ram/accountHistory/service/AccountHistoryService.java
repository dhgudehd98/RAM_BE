package com.sh.Ram.accountHistory.service;

import com.sh.Ram.account.repository.AccountRepository;
import com.sh.Ram.accountHistory.repository.AccountHistoryRepository;
import com.sh.Ram.accountHistory.dto.AccountHistoryDto;
import com.sh.Ram.common.exception.account.AccountException;
import com.sh.Ram.common.exception.accountHistory.AccountHistoryException;
import com.sh.Ram.entity.AccountHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountHistoryService {

    private final AccountHistoryRepository accountHistoryRepository;
    private final AccountRepository accountRepository;

    public Page<AccountHistoryDto> getHistory(Long memberId, String type, Pageable pageable) {

        if (!accountRepository.existsByMemberId(memberId)) {
            throw new AccountException("계좌가 존재하지 않습니다.");
        }

        validateType(type);

        Page<AccountHistory> histories = accountHistoryRepository.findMyAccountHistory(
                memberId, type, pageable);

        return histories.map(AccountHistoryDto::from);
    }

    private void validateType(String type) {

        type = (type == null) ? null : type.toUpperCase();

        if (type == null || type.isBlank()) {
            return;
        }

        if (!"IN".equals(type) && !"OUT".equals(type)) {
            throw new AccountHistoryException("type은 IN 또는 OUT만 가능합니다.");
        }
    }
}
