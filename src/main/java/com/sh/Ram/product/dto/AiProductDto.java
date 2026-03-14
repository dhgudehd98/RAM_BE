package com.sh.Ram.product.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiProductDto {
    private String productName;
    private String brandName;
    private String description;

    @Override
    public String toString() {
        return "AiProductDto{" +
                "productName='" + productName + '\'' +
                ", brandName='" + brandName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}