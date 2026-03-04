package com.sh.Ram.product.dto;

import com.sh.Ram.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private Integer price;
    private String imageUrl;
//    private Category category;

    // Entity → DTO 변환
    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getBrand().getBrandName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl()
//                product.getCategory()
        );
    }
}