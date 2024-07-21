package com.example.lab08rest.service.impl;

import com.example.lab08rest.dto.OrderDTO;
import com.example.lab08rest.dto.UpdateOrderDTO;
import com.example.lab08rest.entity.*;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.enums.PaymentMethod;
import com.example.lab08rest.exception.NotFoundException;
import com.example.lab08rest.mapper.MapperUtil;
import com.example.lab08rest.repository.*;
import com.example.lab08rest.service.CartService;
import com.example.lab08rest.service.CustomerService;
import com.example.lab08rest.service.OrderService;
import com.example.lab08rest.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MapperUtil mapperUtil;
    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(OrderRepository orderRepository, MapperUtil mapperUtil, CustomerService customerService, PaymentService paymentService, CartService cartService, CustomerRepository customerRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.mapperUtil = mapperUtil;
        this.customerService = customerService;
        this.paymentService = paymentService;
        this.cartService = cartService;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.paymentRepository = paymentRepository;
    }


    @Override
    public List<OrderDTO> retrieveOrderList() {
        //retrieve all orders and return as a List of OrderDTO
        return orderRepository.findAll().stream()
                .map(order -> mapperUtil.convert(order,new OrderDTO())).collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO) {
        //look for the orderId inside the database and throw the exception
        Order order = orderRepository.findById(orderDTO.getId()).orElseThrow(
                () -> new NotFoundException("Order could not be found."));

        //then we need to check if the order fields are exist or not
        validateRelatedFieldsAreExist(orderDTO);

        //if fields are exists, then convert orderDTO to order and save it
        Order willBeUpdatedOrder = mapperUtil.convert(orderDTO, new Order());
        Order updatedOrder = orderRepository.save(willBeUpdatedOrder);
        //convert again the updated one and return it
        return mapperUtil.convert(updatedOrder,new OrderDTO());
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        validateRelatedFieldsAreExist(orderDTO);
        Order willBeCreatedOrder = mapperUtil.convert(orderDTO, new Order());

        Order createdOrder = orderRepository.save(willBeCreatedOrder);
        return mapperUtil.convert(createdOrder, new OrderDTO());
    }

    @Override
    public List<OrderDTO> retrieveOrderByPaymentMethod(PaymentMethod paymentMethod) {
        return orderRepository.findAllByPayment_PaymentMethod(paymentMethod).stream().
                map(order -> mapperUtil.convert(order, new OrderDTO())).collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> retrieveOrderByEmail(String email) {
        return orderRepository.findAllByCustomer_Email(email).stream()
                .map(order -> mapperUtil.convert(order, new OrderDTO())).collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderById(Long id, UpdateOrderDTO updateOrderDTO) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Order could not be found."));
        //if we are getting the same value, it is not necessary to update the actual value

        boolean changeDetected = false;

        if (!order.getPaidPrice().equals(updateOrderDTO.getPaidPrice())){
           order.setPaidPrice(updateOrderDTO.getPaidPrice());
           changeDetected =true;
        }

        if(!order.getTotalPrice().equals(updateOrderDTO.getTotalPrice())){
            order.setTotalPrice(updateOrderDTO.getTotalPrice());
            changeDetected=true;
        }

        //if there is any change, update the order and return it
        if(changeDetected){
            Order updateOrder = orderRepository.save(order);
            return mapperUtil.convert(updateOrder,new OrderDTO());
        }else{
            throw new RuntimeException("No changes detected");
        }

    }

    private void validateRelatedFieldsAreExist(OrderDTO orderDTO) {
        //in this method we have 3 different service and make sure they have those fields
        //we will create service and existById method and verify

        if(!customerService.existById(orderDTO.getCustomerId())){
            throw new NotFoundException("Customer could not be found");
        }

        if(!paymentService.existById(orderDTO.getPaymentId())){
            throw new NotFoundException("Payment could not be found");
        }

        if(!cartService.existById(orderDTO.getCartId())){
            throw new NotFoundException("Cart could not be found");
        }
    }


    // Now, it's time to pay, show us you really want it, Also you get your dream discount. Well deserved.
    // Calm down... You will have it. There is only one click left...
    // You have selected desired product. We will have customer's money and place the order.
    // This method responsibility is to place the shiny order.
    // After that you can have a fresh air and refresh the page to see your order is shipped.
    // But first we have to accept the order. This method does that.
    @Override
    public BigDecimal placeOrder(PaymentMethod paymentMethod, Long cartId, Long customerId) {
        // we retrieve customer from DB, if not exists we need to throw exception
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer couldn't find"));

        // if a customer would like to place an order, cart should be created before.
        List<Cart> cartList = cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED);
        // if there is no cart in DB, we need to throw exception
        // Once customer place order cart status will be SOLD and after that if customer would like to buy something
        // again a new cart will be created. That's why a customer can have multiple carts but only one cart with CREATE status
        // can be existed during a run time. All other carts should be SOLD status
        if (cartList == null || cartList.size() == 0) {
            throw new RuntimeException("Cart couldn't find or cart is empty");
        }

        // according to business requirement there always be 1 cart with created state
        // That's why we are retrieving first index of cart list
        Cart cart = cartList.get(0);

        // We retrieve cart items in the cart
        List<CartItem> cartItemList = cartItemRepository.findAllByCart(cartList.get(0));

        // if there is an item quantity that exceeds product remains quantity, we have to remove it from cart item list;
        cartItemList.removeIf(cartItem ->
                cartItem.getQuantity() > cartItem.getProduct().getRemainingQuantity());
        // if there is no item in the cart we are returning ZERO because this method should be returned paid price.
        // No item means you can not pay anything
        if (cartItemList.size() == 0){
            return BigDecimal.ZERO;
        }

        // Discounts can be applied to cart, but it is not mandatory. At first discount amount will be ZERO
        // If a discount can be applicable to cart, we will have discounted amount depends on discount
        // Before placing order discount must have been applied to cart.
        BigDecimal lastDiscountAmount = BigDecimal.ZERO;
        if (cart.getDiscount() != null) {
            lastDiscountAmount = cartService.applyDiscountToCartIfApplicableAndCalculateDiscountAmount(cart.getDiscount().getName(), cart);
        }

        // we are calculating the cart total amount to have a gross amount
        BigDecimal totalCartAmount = calculateTotalCartAmount(cartItemList);

        Payment payment = new Payment();
        Order order = new Order();

        order.setCart(cart);
        order.setCustomer(customer);
        order.setTotalPrice(totalCartAmount);

        // total price $600
        // after discount = $50 -> $550

        // we are calculating the cart total amount after discount
        order.setPaidPrice(totalCartAmount.subtract(lastDiscountAmount));


        // let's assume if you pay with credit card we deduct 10 $ during the campaign period (Reward!!!)
        // additional discount for specific payment method for credit card
        if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
            order.setPaidPrice(order.getPaidPrice().subtract(BigDecimal.TEN));
        }

        // initialising payment entity
        // in the recordings, initialising payment was not inserted database directly.
        // But it caused other problems, so I decided to insert it into DB first
        // After that we are setting payment value to Order Entity.
        payment.setPaidPrice(order.getPaidPrice());
        payment.setPaymentMethod(paymentMethod);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        // after successful order we have decrease product remaining quantity
        // this stream is subtracting cart item quantity from product remaining quantity
        cartItemList.forEach(cartItem -> {
            cartItem.getProduct().setRemainingQuantity(
                    cartItem.getProduct().getRemainingQuantity() - cartItem.getQuantity());
            cartItemRepository.save(cartItem);
        });

        // stock is 50
        // customer bought 18
        // new stock will be 32
        orderRepository.save(order);
        return order.getPaidPrice();
    }

    private BigDecimal calculateTotalCartAmount(List<CartItem> cartItemList) {
        // this stream basically calculates the cart total amount depends on how many product is added to cart and theirs quantity
        // there is also another same method that calculate cart total amount in CartService we should use it for readability,
        // but I paste it here to be able to show you more test cases.
        Function<CartItem, BigDecimal> totalMapper = cartItem -> cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return cartItemList.stream()
                .map(totalMapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
