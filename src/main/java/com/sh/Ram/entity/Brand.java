package com.sh.Ram.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;

    private String brandName;
    private String imageURL;

    @OneToMany(mappedBy = "brand")
    List<Product> products = new ArrayList<>();

}