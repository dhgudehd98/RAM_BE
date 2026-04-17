package com.sh.Ram.entity;

import com.sh.Ram.common.exception.account.AccountException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @OneToOne(mappedBy = "account")
    private Member member;

    private String accountNum;

    private Long accountBalance;

    /**
     * 입찰로 묶여 있는 총 금액
     */
    @Column(nullable = false)
    private Long reservedBalance = 0L;

    private String bankCode;

    private String bankName;

    @OneToMany(mappedBy = "account")
    private List<AccountHistory> accountHistories = new ArrayList<>();

    /**
     * 가용 잔액 = accountBalance - reservedBalance
     */
    public Long getAvailableBalance() {
        long balance = accountBalance == null ? 0L : accountBalance;
        long reserved = reservedBalance == null ? 0L : reservedBalance;
        return balance - reserved;
    }

    /**
     * 예약금 감소
     */
    public void decreaseReservedBalance(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AccountException("감소 금액은 0보다 커야 합니다.");
        }

        if (reservedBalance == null) {
            reservedBalance = 0L;
        }

        if (reservedBalance < amount) {
            throw new AccountException("예약 금액이 부족하여 감소할 수 없습니다.");
        }

        reservedBalance -= amount;
    }

    /**
     * 실제 잔액 차감
     */
    public void withdraw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AccountException("출금 금액은 0보다 커야 합니다.");
        }

        if (accountBalance == null || accountBalance < amount) {
            throw new AccountException("계좌 잔액이 부족합니다.");
        }

        accountBalance -= amount;
    }

    /**
     * 실제 잔액 증가
     */
    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AccountException("입금 금액은 0보다 커야 합니다.");
        }

        if (accountBalance == null) {
            accountBalance = 0L;
        }

        accountBalance += amount;
    }
}
