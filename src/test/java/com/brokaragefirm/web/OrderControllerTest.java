package com.brokaragefirm.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.brokaragefirm.api.Error.GlobalExceptionHandler;
import com.brokaragefirm.api.controller.OrderController;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.OrderRepository;
import com.brokaragefirm.service.OrderService;

@WebMvcTest(controllers = OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthContext auth;
    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderRepository orderRepository;

    @Test
    void create_order_for_self_ok() throws Exception {
        UUID caller = UUID.randomUUID();

        // Mock auth context to simulate a non-admin user
        when(auth.isAdmin(any())).thenReturn(false);
        when(auth.currentCustomerId(any())).thenReturn(caller);

        // Mock the order creation service
        when(orderService.createOrder(any())).thenReturn(new Order());
        var created = new Order();
        created.setId(UUID.randomUUID());
        created.setCustomerId(caller);
        created.setAssetName("INGA");
        created.setOrderSide(Side.BUY);
        created.setSize(new BigDecimal("1.0000"));
        created.setPrice(new BigDecimal("10.00"));
        created.setStatus(OrderStatus.PENDING);
        created.setCreateDate(Instant.parse("2024-01-01T00:00:00Z"));
        when(orderService.createOrder(any())).thenReturn(created);

        String body = """
                {"customerId":"%s","assetName":"INGA","side":"BUY","size":1.0000,"price":10.00}
                """.formatted(caller);

        mvc.perform(post("/api/orders")
                .with(user("user1").roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(caller.toString()))
                .andExpect(jsonPath("$.assetName").value("INGA"))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_order_for_other_customer_forbidden() throws Exception {
        UUID caller = UUID.randomUUID();

        when(auth.isAdmin(any())).thenReturn(false);
        when(auth.currentCustomerId(any())).thenReturn(caller);

        String body = """
                {"customerId":"%s","assetName":"INGA","side":"BUY","size":1.0000,"price":10.00}
                """.formatted(UUID.randomUUID()); // not caller

        mvc.perform(post("/api/orders")
                .with(user("user1").roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_orders_returns_page() throws Exception {
        UUID caller = UUID.randomUUID();

        when(auth.isAdmin(any())).thenReturn(false);
        when(auth.currentCustomerId(any())).thenReturn(caller);

        var o = new Order();
        o.setId(UUID.randomUUID());
        o.setCustomerId(caller);
        o.setAssetName("INGA");
        o.setOrderSide(Side.SELL);
        o.setSize(new BigDecimal("2.5000"));
        o.setPrice(new BigDecimal("15.00"));
        o.setStatus(OrderStatus.PENDING);
        o.setCreateDate(Instant.parse("2024-01-02T00:00:00Z"));

        Page<Order> page = new PageImpl<>(List.of(o), PageRequest.of(0, 20), 1);
        when(orderService.listOrders(any())).thenReturn(page);

        mvc.perform(get("/api/orders")
                .with(user("user1").roles("CUSTOMER"))
                .param("customerId", caller.toString())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assetName").value("INGA"))
                .andExpect(jsonPath("$.content[0].side").value("SELL"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void cancel_own_order_returns_204() throws Exception {
        UUID caller = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(auth.isAdmin(any())).thenReturn(false);
        when(auth.currentCustomerId(any())).thenReturn(caller);

        var existing = new Order();
        existing.setId(orderId);
        existing.setCustomerId(caller);
        existing.setAssetName("INGA");
        existing.setOrderSide(Side.BUY);
        existing.setSize(new BigDecimal("1.0000"));
        existing.setPrice(new BigDecimal("10.00"));
        existing.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(eq(orderId))).thenReturn(Optional.of(existing));
        doNothing().when(orderService).cancelOrder(eq(orderId));

        mvc.perform(delete("/api/orders/{id}", orderId)
                .with(user("user1").roles("CUSTOMER"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
    
}
