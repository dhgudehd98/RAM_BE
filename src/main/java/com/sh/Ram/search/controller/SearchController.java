package com.sh.Ram.search.controller;

import com.sh.Ram.elasticSearch.brand.document.BrandDocument;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
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

    // 모든 상품 조회 -> 메인 페이지로 생각
    @GetMapping("/productsAll")
    @ResponseBody
    public Page<ProductDto> findAllProduct(
            @RequestParam(required = false) String sort
    ) {
        return searchService.findAllProduct(sort);
    }

    // 상품 / 브랜드 검색
    @GetMapping("/products")
    @ResponseBody
    public List<ProductDto> searchByKeyword(@RequestParam(required = false) String keyword){
        return searchService.searchByKeyword(keyword);
    }

    @GetMapping("/autoCompletion")
    @ResponseBody
    public List<BrandDocument> autoCompletion(@RequestParam(required = false) String prefix) throws IOException {
        return searchService.autoCompletion(prefix);
    }

}