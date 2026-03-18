package com.sh.Ram.product.controller;

import com.sh.Ram.product.dto.AiProductDto;
import com.sh.Ram.product.dto.ProductDto;
import com.sh.Ram.product.dto.RegisterProductDto;
import com.sh.Ram.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    @ResponseBody
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

    @PostMapping("/regist")
    @ResponseBody
    public Map<String,String> regist(
            @RequestPart("data") RegisterProductDto registerProductDto,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        //! 나중에 멤버 아이디 값으로 변경
        Long memberId = 1L;
        return productService.regist(registerProductDto, image, memberId);
    }

    @PostMapping("/ai")
    @ResponseBody
    public AiProductDto getProductInfo(
            @RequestPart("image") MultipartFile image
    ) {
        AiProductDto block = productService.getProductInfo(image).block();
        log.info("=== 상품 정보 ===");
        log.info(block.toString());

        return productService.getProductInfo(image).block();
    }

    @GetMapping("{id}")
    public String productDetail(
            @PathVariable Long id,
            Model model
    ){
        ProductDto productDto = productService.getProductDetail(id);
        model.addAttribute("product", productDto);

        return "product/productDetail";
    }

    @PostMapping("/wishList/{id}")
    @ResponseBody
    public Map<String, Object> addWishList(
            @PathVariable("id") Long productId
    ) {
        Long memberId = 1L;
        return productService.addWishList(productId, memberId);
    }
}