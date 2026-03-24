package com.sh.Ram.entity;

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
}
