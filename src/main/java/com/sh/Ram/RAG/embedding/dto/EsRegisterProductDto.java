package com.sh.Ram.RAG.embedding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EsRegisterProductDto {
    private Long productId;
    private Long memberId;
    private String productName;
    private String brandName;
    private Integer price;
    private String imageUrl;
    private String category;
    private String description;
    private List<String> tags;


    @Override
    public String toString() {
        return "EsRegisterProductDto{" +
                "productId=" + productId +
                ", memberId=" + memberId +
                ", productName='" + productName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                '}';
    }
}