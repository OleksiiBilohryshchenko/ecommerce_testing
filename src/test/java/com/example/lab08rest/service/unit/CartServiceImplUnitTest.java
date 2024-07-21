package com.example.lab08rest.service.unit;

import com.example.lab08rest.entity.Cart;
import com.example.lab08rest.entity.CartItem;
import com.example.lab08rest.entity.Customer;
import com.example.lab08rest.entity.Product;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.repository.CartItemRepository;
import com.example.lab08rest.repository.CartRepository;
import com.example.lab08rest.repository.ProductRepository;
import com.example.lab08rest.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    public void should_not_add_to_cart_when_product_doesnt_exist(){
        // if findById runs with 1L -> return an empty value
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        Throwable throwable = catchThrowable(() ->
                cartService.addToCart(new Customer(), 1L, 15));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_exception_when_product_remaining_quantity_is_less_than_quantity() {

        Product product = new Product();
        product.setRemainingQuantity(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Throwable throwable = catchThrowable(() ->
                cartService.addToCart(new Customer(), 1L, 15));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("Not enough stock");
    }

    @Test
    public void should_add_item_to_cart_when_cart_exists_and_cart_item_exists_in_the_cart(){
        // product
        // cart and cartlist
        // cart item

        //Gherkin language
        //Given
        //When
        //Then

        //Given
        Product product = new Product();
        product.setId(1L);
        product.setRemainingQuantity(10);

        Cart cart = new Cart();
        cart.setCartState(CartState.CREATED);
        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setCart(cart);
        cartItem.setQuantity(2);

        Customer customer = new Customer();
        customer.setId(1L);

        //When
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findAllByCustomerIdAndCartState
                (customer.getId(), CartState.CREATED)).thenReturn(cartList);
        when(cartItemRepository.findAllByCartAndProduct(cart, product)).thenReturn(cartItem);

        //Then
        boolean result = cartService.addToCart(customer, product.getId(), 8);
        assertTrue(result);
        assertThat(cartItem.getQuantity()).isEqualTo(10);
    }


}
