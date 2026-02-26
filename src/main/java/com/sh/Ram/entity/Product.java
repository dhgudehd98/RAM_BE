package com.sh.Ram.entity;

import com.sh.Ram.enums.Category;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String imageUrl;

    @OneToMany(mappedBy = "product")
    private List<WishList> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Auction> auctions = new ArrayList<>();

}
