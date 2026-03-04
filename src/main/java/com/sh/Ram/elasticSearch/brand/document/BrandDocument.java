package com.sh.Ram.elasticSearch.brand.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Document(indexName = "brands")
@Setting(settingPath = "classpath:elastic/brands_setting.json")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class BrandDocument {
    @Id
    private String id;
    private String name;

    @CompletionField
    private Completion suggest;

    private String imageUrl;
}