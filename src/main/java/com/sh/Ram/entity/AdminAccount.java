package com.sh.Ram.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminAccount_id")
    private Long id;

    @OneToMany(mappedBy = "adminAccount")
    private List<Account> accounts = new ArrayList<>();

    @OneToOne(mappedBy = "adminAccount")
    private AuctionResult auctionResult;

    private Integer amount;

    private String status;
}
