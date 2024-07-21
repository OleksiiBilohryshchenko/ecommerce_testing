package com.example.lab08rest.service.unit;

import com.example.lab08rest.entity.*;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.enums.PaymentMethod;
import com.example.lab08rest.repository.*;
import com.example.lab08rest.service.CartService;
import com.example.lab08rest.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    public void should_throw_exception_when_the_customer_does_not_exist(){
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.TRANSFER,134L,1L));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_exception_when_cart_list_of_the_customer_is_null(){
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED))
                .thenReturn(null);
        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.TRANSFER,134L,customer.getId()));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_exception_when_cart_list_of_the_customer_size_is_zero(){
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED))
                .thenReturn(new ArrayList<>());
        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.TRANSFER,134L,customer.getId()));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_place_order_without_discount(){
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(5));
        product.setRemainingQuantity(60);

        Customer customer = new Customer();
        customer.setId(1L);

        Cart cart = new Cart();
        cart.setId(1L);
        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(4);
        cartItem.setProduct(product);
        cartItem.setCart(cart);

        List<CartItem> cartItemList = new ArrayList<>();
        cartItemList.add(cartItem);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED)).thenReturn(cartList);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(cartItemList);

        BigDecimal result = orderService.placeOrder(PaymentMethod.TRANSFER, cart.getId(),customer.getId());
        assertThat(result).isEqualTo(BigDecimal.valueOf(20));
        assertThat(product.getRemainingQuantity()).isEqualTo(56);
    }

    @Test
    public void should_place_order_with_discount(){
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(5));
        product.setRemainingQuantity(60);

        Customer customer = new Customer();
        customer.setId(1L);

        Discount discount = new Discount();
        discount.setName("discount");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setDiscount(discount);

        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(4);
        cartItem.setProduct(product);
        cartItem.setCart(cart);

        List<CartItem> cartItemList = new ArrayList<>();
        cartItemList.add(cartItem);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED)).thenReturn(cartList);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(cartItemList);
        when(cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount
                (cart.getDiscount().getName(),cart)).thenReturn(BigDecimal.TEN);

        BigDecimal result = orderService.placeOrder(PaymentMethod.TRANSFER, cart.getId(),customer.getId());
        assertThat(result).isEqualTo(BigDecimal.valueOf(10));
        assertThat(product.getRemainingQuantity()).isEqualTo(56);
    }

    @Test
    public void should_place_order_with_discount_and_extra_credit_cart_discount(){
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(5));
        product.setRemainingQuantity(60);

        Customer customer = new Customer();
        customer.setId(1L);

        Discount discount = new Discount();
        discount.setName("discount");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setDiscount(discount);

        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(10);
        cartItem.setProduct(product);
        cartItem.setCart(cart);

        List<CartItem> cartItemList = new ArrayList<>();
        cartItemList.add(cartItem);

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED)).thenReturn(cartList);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(cartItemList);
        when(cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount
                (cart.getDiscount().getName(),cart)).thenReturn(BigDecimal.valueOf(15));

        BigDecimal result = orderService.placeOrder(PaymentMethod.CREDIT_CARD, cart.getId(),customer.getId());
        assertThat(result).isEqualTo(BigDecimal.valueOf(25));
        assertThat(product.getRemainingQuantity()).isEqualTo(50);
    }




}
