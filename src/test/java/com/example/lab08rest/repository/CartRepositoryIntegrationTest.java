package com.example.lab08rest.repository;

import com.example.lab08rest.entity.Cart;
import com.example.lab08rest.entity.Customer;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.enums.DiscountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartRepositoryIntegrationTest {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void should_find_all_cart_with_discount_type_is_amount_based(){
        List<Cart> cartList = cartRepository.findAllByDiscountDiscountType(DiscountType.AMOUNT_BASED);
        assertThat(cartList).hasSize(259);
    }

    @Test
    public void should_find_all_cart_with_discount_type_is_rate_based(){
        List<Cart> cartList = cartRepository.findAllByDiscountDiscountType(DiscountType.RATE_BASED);
        assertThat(cartList).hasSize(258);
    }

    @Test
    public void should_save_cart(){
        List<Cart> cartList = cartRepository.findAll();
        assertThat(cartList).hasSize(1000);

        Customer customer = customerRepository.findById(1L).get();
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setCartState(CartState.CREATED);
        cartRepository.save(cart);

        List<Cart> cartListAfterSave = cartRepository.findAll();

        assertThat(cartListAfterSave).hasSize(1001);
    }

    @Test
    public void should_find_all_by_customer_id_and_cart_state_and_discount(){
        List<Cart> cartList = cartRepository.findAllByCustomerIdAndCartStateAndDiscountIsNotNull(CartState.CREATED.name(),1L);
        assertThat(cartList).hasSize(500);
    }

}
