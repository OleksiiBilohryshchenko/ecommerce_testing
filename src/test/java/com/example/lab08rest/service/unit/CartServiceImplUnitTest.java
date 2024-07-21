package com.example.lab08rest.service.unit;

import com.example.lab08rest.entity.*;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.enums.DiscountType;
import com.example.lab08rest.repository.CartItemRepository;
import com.example.lab08rest.repository.CartRepository;
import com.example.lab08rest.repository.DiscountRepository;
import com.example.lab08rest.repository.ProductRepository;
import com.example.lab08rest.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
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

    @Mock
    private DiscountRepository discountRepository;

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

    @Test
    public void should_throw_an_exception_when_cart_size_is_two(){

        Product product = new Product();
        product.setId(1L);
        product.setRemainingQuantity(10);

        Cart cart = new Cart();
        cart.setCartState(CartState.CREATED);

        Cart cart2 = new Cart();
        cart.setCartState(CartState.CREATED);

        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);
        cartList.add(cart2);

        Customer customer = new Customer();
        customer.setId(1L);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findAllByCustomerIdAndCartState
                (customer.getId(), CartState.CREATED)).thenReturn(cartList);
        Throwable throwable = catchThrowable(() ->
                cartService.addToCart(customer,1L,8));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_add_item_to_cart_when_cart_doesnt_exist() {
        Product product = new Product();
        product.setId(1L);
        product.setQuantity(10);
        product.setName("Orange");
        product.setPrice(BigDecimal.TEN);
        product.setRemainingQuantity(10);

        Customer customer = new Customer();
        customer.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED)).thenReturn(null);
        when(cartItemRepository.findAllByCartAndProduct(any(), any())).thenReturn(null);

        boolean result = cartService.addToCart(customer, product.getId(), 3);
        assertTrue(result);
    }

    @Test
    public void should_add_to_cart_when_cart_exists_and_cart_item_not_exist(){
        Product product = new Product();
        product.setId(1L);
        product.setRemainingQuantity(10);

        Cart cart = new Cart();
        cart.setCartState(CartState.CREATED);


        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        Customer customer = new Customer();
        customer.setId(1L);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findAllByCustomerIdAndCartState
                (customer.getId(), CartState.CREATED)).thenReturn(cartList);
        when(cartItemRepository.findAllByCartAndProduct(cart, product)).thenReturn(null);


        boolean result = cartService.addToCart(customer, product.getId(), 8);
        assertTrue(result);
    }

    @Test
    public void should_throw_exception_when_discount_not_exists(){
        when(discountRepository.findFirstByName("discount")).thenReturn(null);

        Throwable throwable = catchThrowable(() ->
                cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("discount", new Cart()));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("Discount couldn't find ");
    }

    @Test
    public void should_throw_exception_when_discount_amount_is_null(){

        Discount discount = new Discount();

        when(discountRepository.findFirstByName("discount")).thenReturn(discount);

        Throwable throwable = catchThrowable(() ->
                cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("discount", new Cart()));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("Discount amount can not be null ");
    }

    @Test
    public void should_throw_exception_when_discount_minimum_amount_is_null(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.TEN);

        when(discountRepository.findFirstByName("Dummy Name")).thenReturn(discount);

        Throwable throwable = catchThrowable(() ->  cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("Dummy Name",new Cart()));
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Discount minimum amount can not be null ");
    }

    @Test
    public void should_throw_exception_when_discount_minimum_amount_is_zero(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.TEN);
        discount.setMinimumAmount(BigDecimal.ZERO);
        when(discountRepository.findFirstByName("Dummy Name")).thenReturn(discount);

        Throwable throwable = catchThrowable(() ->  cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("Dummy Name",new Cart()));
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Discount amount needs be bigger than zero ");
    }


    @Test
    public void should_throw_exception_when_discount_amount_is_zero(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.ZERO);
        discount.setMinimumAmount(BigDecimal.TEN);
        when(discountRepository.findFirstByName("Dummy Name")).thenReturn(discount);

        Throwable throwable = catchThrowable(() ->  cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount("Dummy Name",new Cart()));
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Discount amount needs be bigger than zero ");
    }
    @Test
    public void should_throw_exception_when_cart_item_list_is_zero(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.TEN);
        discount.setMinimumAmount(BigDecimal.valueOf(100));
        discount.setName("discount");
        discount.setDiscountType(DiscountType.AMOUNT_BASED);
        Cart cart = new Cart();

        when(discountRepository.findFirstByName(discount.getName())).thenReturn(discount);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(new ArrayList<>());

        Throwable throwable = catchThrowable(() ->
                cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount(discount.getName(), cart));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("There is no item in the cart");
    }

    @Test
    public void should_throw_exception_when_cart_item_list_null(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.TEN);
        discount.setMinimumAmount(BigDecimal.valueOf(100));
        discount.setName("discount");
        discount.setDiscountType(DiscountType.AMOUNT_BASED);
        Cart cart = new Cart();

        when(discountRepository.findFirstByName(discount.getName())).thenReturn(discount);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(null);

        Throwable throwable = catchThrowable(() ->
                cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount(discount.getName(), cart));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessage("There is no item in the cart");
    }

    @Test
    public void should_return_zero_discount_when_total_cart_amount_less_than_minimum_amount(){
        Discount discount = new Discount();
        discount.setDiscount(BigDecimal.TEN);
        discount.setMinimumAmount(BigDecimal.valueOf(100));
        discount.setName("discount");
        discount.setDiscountType(DiscountType.AMOUNT_BASED);

        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(5));

        Cart cart = new Cart();

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setCart(cart);
        cartItem.setQuantity(6);

        List<CartItem> cartItemList = new ArrayList<>();
        cartItemList.add(cartItem);

        when(discountRepository.findFirstByName(discount.getName())).thenReturn(discount);
        when(cartItemRepository.findAllByCart(cart)).thenReturn(cartItemList);

        BigDecimal result = cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount(discount.getName(), cart);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
        assertNull(cart.getDiscount());
    }


}
