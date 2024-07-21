package com.example.lab08rest.controller;

import com.example.lab08rest.dto.DiscountDTO;
import com.example.lab08rest.model.ResponseWrapper;
import com.example.lab08rest.service.DiscountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/discount")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper> listAllDiscounts() {
        return ResponseEntity.ok(new ResponseWrapper("Discounts are retrieved.",
                discountService.readAll(), HttpStatus.OK));
    }

    @PutMapping
    public ResponseEntity<ResponseWrapper> updateDiscount(@RequestBody DiscountDTO discountDTO) {
        return ResponseEntity.ok(new ResponseWrapper("Discount is updated.",
                discountService.update(discountDTO), HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper> createDiscount(@RequestBody DiscountDTO discountDTO) {
        return ResponseEntity.ok(new ResponseWrapper("Discount is created.",
                discountService.create(discountDTO), HttpStatus.OK));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ResponseWrapper> getDiscountByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(new ResponseWrapper("Discounts are retrieved.",
                discountService.readByName(name), HttpStatus.OK));
    }

}
