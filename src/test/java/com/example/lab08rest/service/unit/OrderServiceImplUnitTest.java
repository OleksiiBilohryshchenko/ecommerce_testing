package com.example.lab08rest.service.unit;

import com.example.lab08rest.enums.PaymentMethod;
import com.example.lab08rest.repository.CustomerRepository;
import com.example.lab08rest.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplUnitTest {

    @Mock
    CustomerRepository customerRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    public void should_throw_exception_when_the_customer_does_not_exist(){
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        Throwable throwable = catchThrowable(() ->
                orderService.placeOrder(PaymentMethod.TRANSFER,134L,1L));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }


}
