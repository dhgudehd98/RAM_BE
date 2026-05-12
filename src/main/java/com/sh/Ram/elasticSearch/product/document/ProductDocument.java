package com.sh.Ram.elasticSearch.product.document;

import com.sh.Ram.RAG.embedding.dto.EsRegisterProductDto;
import com.sh.Ram.entity.Product;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.ArrayList;
import java.util.List;

@Document(indexName = "products")
@Setting(settingPath = "classpath:elastic/products_setting.json")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class ProductDocument {

    @Id
    private Long id;
    private Long memberId;
    private String name;
    private String brand;
    private Integer price;
    private String imageUrl;
    private String category;
    private List<String> tags = new ArrayList<>();
    private String description;
    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] descriptionVector;

    public ProductDocument(Product product, List<String> tags) {
        this.id = product.getId();
        this.memberId = product.getMember().getId();
        this.name = product.getName();
        this.brand = product.getBrand().getBrandName();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.category = String.valueOf(product.getCategory());
        this.description = product.getDescription();
        this.tags = tags;
    }

    public ProductDocument(EsRegisterProductDto dto) {
        this.id = dto.getProductId();
        this.memberId = dto.getProductId();
        this.name= dto.getProductName();
        this.brand = dto.getBrandName();
        this.price = dto.getPrice();
        this.imageUrl = dto.getImageUrl();
        this.category = dto.getCategory();
        this.tags = dto.getTags();
        this.description = dto.getDescription();
    }

    public ProductDocument(Product product, float[] vectors) {
        this.id = product.getId();
        this.memberId = product.getMember().getId();
        this.name= product.getName();
        this.brand = product.getBrand().getBrandName();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.category = String.valueOf(product.getCategory());
        this.tags = List.of("반팔", "후드집업", "상의");
        this.description = product.getDescription();
        this.descriptionVector = vectors;
    }


}