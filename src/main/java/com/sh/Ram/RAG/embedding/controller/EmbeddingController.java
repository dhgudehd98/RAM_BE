package com.sh.Ram.RAG.embedding.controller;

import com.sh.Ram.RAG.embedding.sevice.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/embedded")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    @GetMapping("/weather")
    public void recommendProductByWeather() {
        String embeddedKeyword = "지금 날씨에 알맞은 상품 추천해줘";

        embeddingService.recommendProductByWeather(embeddedKeyword);

    }
}