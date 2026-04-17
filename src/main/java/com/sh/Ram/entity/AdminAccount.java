package com.sh.Ram.entity;

import com.sh.Ram.common.exception.auctionResult.AuctionResultException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminAccount_id")
    private Long id;

    @OneToMany(mappedBy = "adminAccount")
    private List<AuctionResult> auctionResults = new ArrayList<>();

    private Long balance;

    /**
     * 입금 (buyer -> admin, proccessIn)
     */
    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AuctionResultException("금액은 0보다 커야 합니다.");
        }

        if (this.balance == null) {
            this.balance = 0L;
        }

        this.balance += amount;
    }

    /**
     * 출금 (admin -> seller, processOut)
     */
    public void withdraw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AuctionResultException("금액은 0보다 커야 합니다.");
        }

        if (this.balance == null || this.balance < amount) {
            throw new AuctionResultException("관리자 계좌 잔액이 부족합니다.");
        }

        this.balance -= amount;
    }
}
