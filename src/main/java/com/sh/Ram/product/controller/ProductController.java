package com.sh.Ram.product.controller;

import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    @GetMapping("/list")
    public String findAllProduct(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<ProductDto> products = productService.findAllProduct(sort, page);

        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        return "product/list";
    }
}