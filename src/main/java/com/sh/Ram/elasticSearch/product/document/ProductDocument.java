package com.sh.Ram.elasticSearch.product.document;

import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products")
@Setting(settingPath = "classpath:elastic/products_setting.json")
@Getter
@Setter
@Slf4j
public class ProductDocument {

    @Id
    private String id;
    private String name;
    private String brand;
}