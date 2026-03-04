package com.sh.Ram.entity;

import jakarta.persistence.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @OneToOne(mappedBy = "account")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "adminAccount_id")
    private AdminAccount adminAccount;

    private String accountNum;

    private String accountName;

    private String bankCode;

    private String bankName;

}
