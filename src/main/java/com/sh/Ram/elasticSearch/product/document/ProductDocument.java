package com.sh.Ram.elasticSearch.product.document;

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

    public ProductDocument(Long id, Long memberId, String name, String brand, Integer price, String imageUrl, String category, List<String> tags) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.tags = tags;
    }

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] descriptionVector;
}