package com.sh.Ram.entity;

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

    private String bankCode;

    private String bankName;

    @OneToMany(mappedBy = "account")
    private List<AccountHistory> accountHistories = new ArrayList<>();

}
