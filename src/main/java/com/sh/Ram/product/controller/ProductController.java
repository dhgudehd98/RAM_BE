package com.sh.Ram.product.controller;

import com.sh.Ram.elasticSearch.product.repository.ProductDocumentRepository;
import com.sh.Ram.entity.Product;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    @GetMapping("/productsAll")
    @ResponseBody
    public Page<ProductDto> findAllProduct(
            @RequestParam(required = false) String sort
    ) {
        return productService.findAllProduct(sort);
    }

    @GetMapping("/search")
    @ResponseBody
    public List<ProductDto> searchByKeyword(
            @RequestParam(required = false) String keyword
    ){
        return productService.searchByKeyword(keyword);
    }

}