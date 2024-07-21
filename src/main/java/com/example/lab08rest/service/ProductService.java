package com.example.lab08rest.service;

import com.example.lab08rest.dto.ProductDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<ProductDTO> retrieveProductList();

    ProductDTO updateProduct(ProductDTO productDTO);

    ProductDTO createProduct(ProductDTO productDTO);

    List<ProductDTO> retrieveAllProductByCategoryAndPrice(List<Long> categoryList, BigDecimal price);

    ProductDTO retrieveByName(String name);

    List<ProductDTO> retrieveProductByTop3ProductPrice();

    Integer countProductByPrice(BigDecimal price);

    List<ProductDTO> retrieveProductByPriceAndQuantity(BigDecimal price, Integer quantity);

    List<ProductDTO> retrieveByCategory(Long categoryId);
}
