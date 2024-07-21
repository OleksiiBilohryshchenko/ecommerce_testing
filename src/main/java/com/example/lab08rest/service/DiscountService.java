package com.example.lab08rest.service;

import com.example.lab08rest.dto.DiscountDTO;

import java.util.List;

public interface DiscountService {

    List<DiscountDTO> readAll();
    DiscountDTO update(DiscountDTO discountDTO);
    DiscountDTO create(DiscountDTO discountDTO);
    DiscountDTO readByName(String name);

}
