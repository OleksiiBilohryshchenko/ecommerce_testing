package com.example.lab08rest.service.integration;

import com.example.lab08rest.entity.Cart;
import com.example.lab08rest.entity.Customer;
import com.example.lab08rest.entity.Order;
import com.example.lab08rest.enums.PaymentMethod;
import com.example.lab08rest.repository.CartRepository;
import com.example.lab08rest.repository.CustomerRepository;
import com.example.lab08rest.repository.OrderRepository;
import com.example.lab08rest.service.CartService;
import com.example.lab08rest.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class OrderServiceImplIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void should_place_order_when_payment_method_is_transfer(){
        Cart cart = cartRepository.findById(3L).get();
        Order orderBeforePlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNull(orderBeforePlaceOrderMethod);

        BigDecimal result = orderService.placeOrder(PaymentMethod.TRANSFER, 3L, 56L);
        Order orderAfterPlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNotNull(orderAfterPlaceOrderMethod);
        assertThat(result).isEqualTo(new BigDecimal("901.00"));
    }

    @Test
    public void should_place_order_without_discount_when_payment_method_is_credit_card(){
        Cart cart = cartRepository.findById(3L).get();
        Order orderBeforePlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNull(orderBeforePlaceOrderMethod);

        BigDecimal result = orderService.placeOrder(PaymentMethod.CREDIT_CARD, 3L, 56L);
        Order orderAfterPlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNotNull(orderAfterPlaceOrderMethod);
        assertThat(result).isEqualTo(new BigDecimal("891.00"));
    }

    @Test
    public void should_place_order_with_discount_when_payment_method_is_credit_card(){
        Cart cart = cartRepository.findById(3L).get();
        Order orderBeforePlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNull(orderBeforePlaceOrderMethod);
        assertNull(cart.getDiscount());

        BigDecimal discountAmount = cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("50 dollar",cart);

        assertThat(discountAmount).isEqualTo(new BigDecimal("50.00"));
        assertNotNull(cart.getDiscount());

        BigDecimal result = orderService.placeOrder(PaymentMethod.CREDIT_CARD, 3L, 56L);
        Order orderAfterPlaceOrderMethod = orderRepository.findAllByCart(cart);
        assertNotNull(orderAfterPlaceOrderMethod);
        assertThat(result).isEqualTo(new BigDecimal("841.00"));
    }

    @Test
    public void should_not_place_order_when_the_customer_does_not_exist(){
        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.CREDIT_CARD, 1L, 0L));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_not_place_order_when_the_cart_does_not_exist(){
        Customer customer = new Customer();
        customerRepository.save(customer);

        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.CREDIT_CARD, 0L, customer.getId()));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("Cart couldn't find or cart is empty");
    }


}
