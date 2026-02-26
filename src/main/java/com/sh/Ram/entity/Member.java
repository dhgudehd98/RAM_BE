package com.sh.Ram.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    private String email;

    private String password;

    private String nickname;

    private String phone;

    private String address;

    private boolean isAccount;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "member")
    private List<WishList> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Product> product = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Bid> bid = new ArrayList<>();
}
