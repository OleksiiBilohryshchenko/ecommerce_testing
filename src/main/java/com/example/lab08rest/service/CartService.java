package com.example.lab08rest.service;

import com.example.lab08rest.dto.CartDTO;
import com.example.lab08rest.entity.Cart;
import com.example.lab08rest.entity.CartItem;
import com.example.lab08rest.entity.Customer;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    boolean existById(Long id);

    CartDTO findById(Long id);

    boolean addToCart(Customer customer, Long productId, Integer quantity);

    BigDecimal applyDiscountToCartIfApplicableAndCalculateDiscountAmount(String discountName, Cart cart);

    BigDecimal calculateTotalCartAmount(List<CartItem> cartItemList);
}
