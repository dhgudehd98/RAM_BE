package com.sh.Ram.product.dto;

import com.sh.Ram.elasticSearch.product.document.ProductDocument;
import com.sh.Ram.entity.Product;
import com.sh.Ram.enums.AuctionStatus;
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
    private AuctionStatus auctionStatus;
//    private Category category;


    public ProductDto(Long id, String name, String brand, String description, Integer price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

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
                ", auctionStatus=" + auctionStatus +
                '}';
    }
}