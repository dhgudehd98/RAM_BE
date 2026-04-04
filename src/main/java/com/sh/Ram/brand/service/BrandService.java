package com.sh.Ram.brand.service;

import com.sh.Ram.brand.dto.BrandDto;
import com.sh.Ram.brand.repository.BrandRepository;
import com.sh.Ram.entity.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    public List<BrandDto> brandList() {
        List<Brand> brands = brandRepository.findAll();

        return brands.stream()
                .map(brand -> new BrandDto(brand.getId(), brand.getBrandName()))
                .collect(Collectors.toList());
    }
}