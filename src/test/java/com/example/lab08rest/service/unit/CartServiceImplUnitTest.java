package com.example.lab08rest.service.unit;

import com.example.lab08rest.entity.Customer;
import com.example.lab08rest.repository.ProductRepository;
import com.example.lab08rest.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplUnitTest {

    @Mock
    private ProductRepository productRepository;

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


}
