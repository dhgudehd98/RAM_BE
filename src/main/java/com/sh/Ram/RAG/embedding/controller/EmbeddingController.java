package com.sh.Ram.RAG.embedding.controller;

import com.sh.Ram.RAG.embedding.sevice.EmbeddingService;
import com.sh.Ram.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/embedded")
@Slf4j
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    @GetMapping("/weather")
    public List<ProductDto> recommendProductByWeather() {
        String embeddedKeyword = "지금 날씨에 알맞은 상품 추천해줘";
        return embeddingService.recommendProductByWeather(embeddedKeyword);
    }
}