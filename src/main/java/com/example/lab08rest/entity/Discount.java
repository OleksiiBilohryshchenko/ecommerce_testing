package com.example.lab08rest.entity;

import com.example.lab08rest.enums.DiscountType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Discount extends BaseEntity{
    private String name;
    private BigDecimal discount;
    @Enumerated(value = EnumType.STRING)
    private DiscountType discountType;

    //newly added
    private BigDecimal minimumAmount;
}
