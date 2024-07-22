package com.example.lab08rest.service.integration;

import com.example.lab08rest.entity.Cart;
import com.example.lab08rest.entity.CartItem;
import com.example.lab08rest.entity.Customer;
import com.example.lab08rest.entity.Product;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.repository.CartItemRepository;
import com.example.lab08rest.repository.CartRepository;
import com.example.lab08rest.repository.CustomerRepository;
import com.example.lab08rest.repository.ProductRepository;
import com.example.lab08rest.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CartServiceImplIntegrationTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void should_add_to_cart_without_existing_cart(){
        Customer customer = new Customer();
        customer.setEmail("alex@gmail.com");
        customerRepository.save(customer);

        boolean result = cartService.addToCart(customer, 1L, 10);
        List<Cart> cartList = cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED);
        Product product = productRepository.findById(1L).get();
        CartItem cartItem = cartItemRepository.findAllByCartAndProduct(cartList.get(0),product);

        assertNotNull(cartItem);
        assertThat(cartList).hasSize(1);
        assertTrue(result);
    }

    @Test
    public void should_add_to_cart_with_existing_cart(){
        Customer customer = customerRepository.findById(40L).get();

        boolean result = cartService.addToCart(customer, 1L, 10);
        List<Cart> cartList = cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED);
        Product product = productRepository.findById(1L).get();
        CartItem cartItem = cartItemRepository.findAllByCartAndProduct(cartList.get(0),product);

        assertNotNull(cartItem);
        assertThat(cartList).hasSize(1);
        assertTrue(result);
    }

}
