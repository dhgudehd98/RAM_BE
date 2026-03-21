package com.sh.Ram.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterProductDto {
    private String productName; // 상품명
    private String brandName; // 브랜드명
    private String category; // 카테고리
    private String description; // 상품 설명
    private Integer price; // 상품 가격
    private Boolean isAuction; // 경매 여 / 부

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate auctionStartDate; // 경매 시작 날짜
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate auctionEndDate; // 경매 종료 날짜

    @Override
    public String toString() {
        return "RegisterProductDto{" +
                "productName='" + productName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", isAuction=" + isAuction +
                ", auctionStartDate=" + auctionStartDate +
                ", auctionEndDate=" + auctionEndDate +
                '}';
    }
}