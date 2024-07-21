package com.example.lab08rest.dto;

import com.example.lab08rest.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentDTO {
    private Long id;
    private BigDecimal paidPrice;
    private PaymentMethod paymentMethod;
}
