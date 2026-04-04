package com.sh.Ram.brand.controller;

import com.sh.Ram.brand.dto.BrandDto;
import com.sh.Ram.brand.service.BrandService;
import com.sh.Ram.entity.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/brand")
public class BrandController {

    private final BrandService brandService;

    @GetMapping("list")
    @ResponseBody
    public List<BrandDto> brandList() {
        return brandService.brandList();
    }
}