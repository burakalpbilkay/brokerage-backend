// src/main/java/com/brokaragefirm/api/controller/OrderController.java
package com.brokaragefirm.api.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brokaragefirm.api.Mapper;
import com.brokaragefirm.api.Error.Errors.BadRequestException;
import com.brokaragefirm.api.Error.Errors.ForbiddenException;
import com.brokaragefirm.api.Error.Errors.NotFoundException;
import com.brokaragefirm.api.dto.CreateOrderRequest;
import com.brokaragefirm.api.dto.OrderFilterRequest;
import com.brokaragefirm.api.dto.OrderResponse;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.repository.OrderRepository;
import com.brokaragefirm.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final OrderRepository orderRepo;
  private final AuthContext auth;

  @PostMapping
  public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest req, Authentication who) {
    if (!auth.isAdmin(who)) {
      UUID me = auth.currentCustomerId(who);
      if (me == null || !me.equals(req.getCustomerId()))
        throw new ForbiddenException("Customers can only create orders for themselves.");
    }
    Order o = orderService.createOrder(req);
    return ResponseEntity.ok(Mapper.toOrderResponse(o));
  }

  @GetMapping
  public ResponseEntity<Page<OrderResponse>> list(
      @RequestParam("customerId") UUID customerId,
      @RequestParam(value = "from", required = false) Instant from,
      @RequestParam(value = "to", required = false) Instant to,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "assetName", required = false) String assetName,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      Authentication who) {
    if (!auth.isAdmin(who)) {
      UUID me = auth.currentCustomerId(who);
      if (me == null || !me.equals(customerId))
        throw new ForbiddenException("Customers can only list their own orders.");
    }
    OrderStatus st = null;
    if (status != null && !status.isBlank()) {
      try {
        st = OrderStatus.valueOf(status.trim().toUpperCase());
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("Invalid status: " + status);
      }
    }
    var filter = new OrderFilterRequest(customerId, from, to,
        st,
        assetName, page, size);
    Page<OrderResponse> resp = orderService.listOrders(filter).map(Mapper::toOrderResponse);
    return ResponseEntity.ok(resp);
  }

  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> cancel(@PathVariable("orderId") UUID orderId, Authentication who) {
    Order o = orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    if (!auth.isAdmin(who)) {
      UUID me = auth.currentCustomerId(who);
      if (me == null || !me.equals(o.getCustomerId()))
        throw new ForbiddenException("Customers can only cancel their own orders.");
    }
    orderService.cancelOrder(orderId);
    return ResponseEntity.noContent().build();
  }
}
