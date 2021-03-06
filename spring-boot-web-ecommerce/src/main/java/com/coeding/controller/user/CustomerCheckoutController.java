package com.coeding.controller.user;

import com.coeding.entity.*;
import com.coeding.service.CustomerOrderService;
import com.coeding.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * author Nhanle
 */
@Slf4j
@Controller
@RequestMapping("/customer/product")
public class CustomerCheckoutController {
    private Customer customer;
    private CustomerService customerService;
    private CustomerOrderService customerOrderService;
    private Cart cart;

    @Autowired
    public CustomerCheckoutController(CustomerService customerService, Cart cart, CustomerOrderService customerOrderService) {
        this.customerService = customerService;
        this.cart = cart;
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/checkout")
    public String customerHomePage(Authentication authentication, Model model) {
        String template = "template/user/customer/customer-info";
        log.info("set template customer info");
        if (authentication != null) {
            UserDetail userDetails = (UserDetail) authentication.getPrincipal();
            model.addAttribute("user", userDetails.getUser());

            Long countCustomer = customerService.countByUserId(userDetails.getUser().getId());

            if (countCustomer > 0) {
                log.info("customer already input info");
                customer = customerService.findByUserId(userDetails.getUser().getId());
                if (customer.getAddress() != null && customer.getFirstName() != null && customer.getLastName() != null && customer.getPhone() != null) {
                    log.info("customer already input all field of info");
                    model.addAttribute("customerOrder", new CustomerOrder(
                            customer.getFirstName() + " " + customer.getLastName(),
                            customer.getAddress(),
                            customer.getPhone(),
                            cart.calCartTotal(),
                            false,
                            customer,
                            cart.getCartItems()
                    ));
                    log.info("set template checkout");
                    template = "template/user/customer/product/checkout";
                }
            }
        }

        log.info("return teamplate");
        return template;
    }

    @PostMapping("/checkout")
    public String processCustomerOrder(CustomerOrder order, Model model) {
        log.info("save order with deliver info : " + order.getDeliverCustomerName() + "," + order.getDeliverCustomerPhone() + "," + order.getDeliverCustomerAddress());
        List<CartItem> listItem = new ArrayList<>(cart.getCartItems());
        CustomerOrder customerOrder = new CustomerOrder(
                order.getDeliverCustomerName(),
                order.getDeliverCustomerAddress(),
                order.getDeliverCustomerPhone(),
                cart.calCartTotal(),
                false,
                customer,
                listItem
        );


        customerOrderService.save(customerOrder);
        cart.clearCartItem();
        model.addAttribute("customerOrder",customerOrder);
        log.info("return to payment page");
        return "template/user/customer/payment/paypal/payment-paypal";
    }
}
