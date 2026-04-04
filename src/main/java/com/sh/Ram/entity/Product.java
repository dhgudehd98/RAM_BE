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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="brand_id")
    private Brand brand;

    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String imageUrl;

    private Integer price;

    // 경매 마감되면 상품에 대한 값 노출 x
    @Column(nullable = false)
    private boolean onSale = false;

    @OneToMany(mappedBy = "product")
    private List<WishList> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<Auction> auctions = new ArrayList<>();

    public Product(Member member, String name, Brand brand, String description, Integer price, String imageUrl) {
        this.member = member;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Product(Member member, String name, Brand brand, String description, Integer price, String imageUrl, boolean onSale) {
        this.member = member;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.onSale = onSale;
    }


    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", member=" + member +
                ", name='" + name + '\'' +
                ", brand=" + brand +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", imageUrl='" + imageUrl + '\'' +
                ", price=" + price +
                ", wishList=" + wishList +
                ", auctions=" + auctions +
                '}';
    }
}
