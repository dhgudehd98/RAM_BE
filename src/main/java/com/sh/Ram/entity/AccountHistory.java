package com.sh.Ram.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_history_id")
    private Long id;

    // 출금 / 입금 주체 계좌
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private Long amount;

    /**
     * IN : 입금, OUT 출금
     */
    private String type;

    private LocalDateTime createdAt;

    public AccountHistory(Account account, Long amount, String type) {
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}
