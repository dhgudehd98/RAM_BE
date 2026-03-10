package com.sh.Ram.product.dto;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
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

    public ProductDto(ProductDocument document) {
        this.id = Long.parseLong(document.getId());
        this.name = document.getName();
        this.brand = document.getBrand();
        this.price = document.getPrice();
        this.imageUrl = document.getImageUrl();
    }

    @Override
    public String toString() {
        return "ProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}