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
import org.springframework.security.core.Authentication;
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

//    @GetMapping("/list")
//    @ResponseBody
//    public Page<ProductDto> findAllProduct(
//            @RequestParam(required = false) String sort,
//            @RequestParam(defaultValue = "0") int page
//    ) {
//         return productService.findAllProduct(sort, page);
//    }

    @GetMapping("/list")
    @ResponseBody
    public List<ProductDto> findAllProduct (
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long lastId
    ) {
        return productService.findAllProduct(lastId, sort);
    }

    @PostMapping("/regist")
    @ResponseBody
    public Map<String,String> regist(
            @RequestPart("data") RegisterProductDto registerProductDto,
            @RequestPart("image") MultipartFile image,
            Authentication authentication
    ) throws IOException {
        return productService.regist(registerProductDto, image, (Long)authentication.getPrincipal());
    }

    @PostMapping("/ai")
    @ResponseBody
    public AiProductDto getProductInfo(
            @RequestPart("image") MultipartFile image
    ) {
        AiProductDto block = productService.getProductInfo(image).block();
        return productService.getProductInfo(image).block();
    }

    @GetMapping("/detail")
    @ResponseBody
    public ProductDto productDetail(
            @RequestParam Long productId
    ){
        return  productService.getProductDetail(productId);
    }

    @PostMapping("/wishList")
    @ResponseBody
    public Map<String, Object> addWishList(
            @RequestParam Long productId,
            Authentication authentication
    ) {
        return productService.addWishList(productId, (Long)authentication.getPrincipal());
    }
}