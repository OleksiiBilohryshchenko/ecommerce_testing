package com.example.lab08rest.service.impl;

import com.example.lab08rest.dto.CartDTO;
import com.example.lab08rest.entity.*;
import com.example.lab08rest.enums.CartState;
import com.example.lab08rest.enums.DiscountType;
import com.example.lab08rest.mapper.MapperUtil;
import com.example.lab08rest.repository.*;
import com.example.lab08rest.service.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final MapperUtil mapperUtil;
    private final CartItemRepository cartItemRepository;

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           MapperUtil mapperUtil,
                           CartItemRepository cartItemRepository,
                           DiscountRepository discountRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.mapperUtil = mapperUtil;
        this.cartItemRepository = cartItemRepository;
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
    }

    @Override
    public CartDTO findById(Long id) {
        return cartRepository.findById(id).stream()
                .map(cart -> mapperUtil.convert(cart, new CartDTO())).findFirst().orElseThrow();
    }

    public boolean existById(Long id) {
        return cartRepository.existsById(id);
    }

    // if a customer would like to buy something, The customer needs to add to cart first.
    // but clicking the Add To Cart button doesn't mean that it is guaranteed product will be added to cart.
    // we have some validations and protections, This method will do the operation.
    @Override
    public boolean addToCart(Customer customer, Long productId, Integer quantity) {
        // we retrieve discount by name and if there is no product with the id, we need to throw exception
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product couldn't find"));

        // quantity that customer would like to buy needs to be bigger than product's remaining quantity
        // if there is any problem with that, we have to throw exception otherwise our stock management will complain to sell products actually we don't have
        if (product.getRemainingQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        // we retrieve customer's cart, if there is no cart that belongs to the customer. No rush we can create one!
        List<Cart> cartList = cartRepository.findAllByCustomerIdAndCartState(customer.getId(), CartState.CREATED);
        // we are checking, if there is any cart, duplication, size
        // if there is any problem we throw exception in checkCartCount method
        Cart cart = checkCartCount(cartList, customer);

        // we retrieve cart item related with the product to decide product is already there or will be added new
        CartItem cartItem = cartItemRepository.findAllByCartAndProduct(cart, product);

        // cart -> 5 milk
        // cart -> 3 milk
        // cart -> 8 milk

        // if product is already added to cart before, we will update the quantity, adding to existing quantity value
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            // if product is not added before, we will create cart item with quantity
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setProduct(product);
            cartItemRepository.save(cartItem);
        }

        // If everything goes well, we will return true. Otherwise, we are throwing some exceptions.
        return true;
    }

    // Customers always search for a discount, right? When you see a great discount you want to have, anyone does ?
    // but clicking the Use Discount button doesn't mean that it is guaranteed discount will be added to cart.
    // we have some validations and protections, This method will do the operation.

    @Override
    public BigDecimal applyDiscountToCartIfApplicableAndCalculateDiscountAmount(String discountName, Cart cart) {
        // we retrieve discount by name and if there is no discount with the name, we need to throw exception
        Discount discount = discountRepository.findFirstByName(discountName);
        if (discount == null) {
            throw new RuntimeException("Discount couldn't find ");
        }

        // discount amount also needs to have a value, otherwise we will throw exception
        if (discount.getDiscount() == null) {
            throw new RuntimeException("Discount amount can not be null ");
        }

        // discount minimum amount also needs to have a value, otherwise we will throw exception
        if (discount.getMinimumAmount() == null) {
            throw new RuntimeException("Discount minimum amount can not be null ");
        }

        // discount minimum amount and discount amount also needs to have a value bigger than ZERO, otherwise we will throw exception
        if (discount.getMinimumAmount().compareTo(BigDecimal.ZERO) <= 0 || discount.getDiscount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Discount amount needs be bigger than zero ");
        }

        // without having any item in cart, if customer try to add discount to cart we need to throw exception
        List<CartItem> cartItemList = cartItemRepository.findAllByCart(cart);
        if (cartItemList == null || cartItemList.size() == 0) {
            throw new RuntimeException("There is no item in the cart");
        }

        // we retrieve cart total amount to decide discount is applicable to the cart
        BigDecimal totalCartAmount = calculateTotalCartAmount(cartItemList);

        // if cart total amount less than discount minimum amount, no discount will be added to cart
        // and discount amount will be ZERO, we are returning the response without assigning any discount value to cart
        if (discount.getMinimumAmount().compareTo(totalCartAmount) > 0) {
            return BigDecimal.ZERO;
        }

        // if everything goes well we add discount to cart
        cart.setDiscount(discount);
        cartRepository.save(cart);

        // we are deciding which discount will be added to cart
        // depends on the discount, we are calculating the discount amount and return
        // if discount type amount based, we will return actual discount amount without calculation
        //
        if (discount.getDiscountType().equals(DiscountType.RATE_BASED)) {
            return totalCartAmount.multiply(discount.getDiscount()).divide(new BigDecimal(100), RoundingMode.FLOOR);
        } else {
            return discount.getDiscount();
        }
    }

    @Override
    public BigDecimal calculateTotalCartAmount(List<CartItem> cartItemList) {
        // this stream basically calculates the cart total amount depends on how many product is added to cart and theirs quantity
        Function<CartItem, BigDecimal> totalMapper = cartItem -> cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return cartItemList.stream()
                .map(totalMapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Cart createCartForCustomer(Customer customer) {
        // if customer doesn't have cart before adding a product to cart, no need to throw exception. We can create one.
        Cart cart = new Cart();
        cart.setCartState(CartState.CREATED);
        cart.setCustomer(customer);
        return cartRepository.save(cart);
    }

    private Cart checkCartCount(List<Cart> cartList, Customer customer) {
        // if customer doesn't have cart before adding a product to cart, no need to throw exception. We can create one.
        if (cartList == null || cartList.size() == 0) {
            cartList = new ArrayList<>();
            Cart cart = createCartForCustomer(customer);
            cartList.add(cart);
        }
        // if customer has multiple cart as CREATED status, means that there is a problem with our values in DB
        // we shouldn't allow a customer put any item into cart, we have to fix duplication first.
        if (cartList.size() > 1) {
            throw new RuntimeException("Duplicate cart count. Check values on database");
        }

        return cartList.get(0);
    }
}
