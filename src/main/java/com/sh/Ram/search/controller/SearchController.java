package com.sh.Ram.search.controller;

import com.sh.Ram.elasticSearch.brand.document.BrandDocument;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.ranking.dto.RankingDto;
import com.sh.Ram.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
@Slf4j
public class SearchController {

    private final SearchService searchService;

    // 상품 / 브랜드 검색
    @GetMapping("/result")
    public String searchByKeyword(
            @RequestParam(required = false) String keyword,
            Model model)
    {
        List<ProductDto> products = searchService.searchByKeyword(keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("products", products);
        return "search/result";
    }

//    @GetMapping("/result")
    public String searchByKeywordByDB(
            @RequestParam String keyword,
            Model model
    ){
        List<ProductDto> products = searchService.searchByKeywordByDB(keyword);


        model.addAttribute("products", products);
        return "search/result";
    }

    @GetMapping("/autoCompletion")
    @ResponseBody
    public List<BrandDocument> autoCompletion(@RequestParam(required = false) String prefix) throws IOException {
        return searchService.autoCompletion(prefix);
    }

    @GetMapping("/ranking")
    @ResponseBody
    public List<RankingDto> getKeywordRanking() {

       return searchService.getKeywordRanking();
    }

    @GetMapping("")
    public String main() {
        return "search/main";
    }

}