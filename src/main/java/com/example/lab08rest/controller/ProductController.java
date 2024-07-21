package com.example.lab08rest.controller;

import com.example.lab08rest.dto.ProductDTO;
import com.example.lab08rest.dto.ProductRequest;
import com.example.lab08rest.model.ResponseWrapper;
import com.example.lab08rest.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public ResponseEntity<ResponseWrapper> getProductList(){

        return ResponseEntity.ok(new ResponseWrapper("Products are successfully retrieved.",
                productService.retrieveProductList(), HttpStatus.OK));
    }

    @PutMapping
    public ResponseEntity<ResponseWrapper> updateProduct(@RequestBody ProductDTO productDTO){
        return ResponseEntity.ok(new ResponseWrapper("Product is successfuly updated",
                productService.updateProduct(productDTO),HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper> createProduct(@RequestBody ProductDTO productDTO){
        return ResponseEntity.ok(new ResponseWrapper("Product is successfully created",
                productService.createProduct(productDTO),HttpStatus.OK));
    }

    @PostMapping("/categoryandprice")
    public ResponseEntity<ResponseWrapper> retrieveProductByCategoryAndPrice(@RequestBody ProductRequest productRequest){
        return ResponseEntity.ok(new ResponseWrapper("Products are successfully retrieved.",
                productService.retrieveAllProductByCategoryAndPrice(productRequest.getCategoryList(),productRequest.getPrice()),HttpStatus.OK));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ResponseWrapper> retrieveProductByName(@PathVariable("name") String name){
        return ResponseEntity.ok(new ResponseWrapper("Product is successfully retrieved.",
                productService.retrieveByName(name),HttpStatus.OK));
    }

    @GetMapping("/top3")
    public ResponseEntity<ResponseWrapper> retrieveProductByTop3ProductByPrice(){
        return ResponseEntity.ok(new ResponseWrapper("Product is successfully retrieved.",
                productService.retrieveProductByTop3ProductPrice(),HttpStatus.OK));
    }

    @GetMapping("/price/{price}")
    public ResponseEntity<ResponseWrapper> countProductsByPrice(@PathVariable("price")BigDecimal price){
        return ResponseEntity.ok(new ResponseWrapper("Product is successfully retrieved.",
                productService.countProductByPrice(price),HttpStatus.OK));
    }

    @GetMapping("/price/{price}/quantity/{quantity}")
    public ResponseEntity<ResponseWrapper> retrieveProductByPriceAndQuantity(@PathVariable("price")BigDecimal price,
                                                                             @PathVariable("quantity") Integer quantity){
        return ResponseEntity.ok(new ResponseWrapper("Products are successfully retrieved.",
                productService.retrieveProductByPriceAndQuantity(price,quantity),HttpStatus.OK));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<ResponseWrapper> retriveByCategory(@PathVariable("id") Long categoryId){
        return ResponseEntity.ok(new ResponseWrapper("Products are successfully retrieved.",
                productService.retrieveByCategory(categoryId),HttpStatus.OK));
    }


}
