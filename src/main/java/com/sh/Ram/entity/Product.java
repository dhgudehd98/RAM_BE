package com.sh.Ram.entity;

import com.sh.Ram.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;
    private String brand;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String imageUrl;

    private Integer price;

    @OneToMany(mappedBy = "product")
    private List<WishList> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Auction> auctions = new ArrayList<>();

    public Product(Member member, String name, String brand, String description, Integer price, String imageUrl) {
        this.member = member;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
