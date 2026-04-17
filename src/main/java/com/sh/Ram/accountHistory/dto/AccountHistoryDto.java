package com.sh.Ram.accountHistory.dto;

import com.sh.Ram.entity.AccountHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccountHistoryDto {

    private Long id;
    private Long amount;
    private String type;
    private LocalDateTime creadtedAt;

    public static AccountHistoryDto from(AccountHistory accountHistory) {
        return AccountHistoryDto.builder()
                .id(accountHistory.getId())
                .amount(accountHistory.getAmount())
                .type(accountHistory.getType())
                .creadtedAt(accountHistory.getCreatedAt())
                .build();
    }
}
