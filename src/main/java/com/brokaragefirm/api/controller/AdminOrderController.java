package com.brokaragefirm.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brokaragefirm.api.Mapper;
import com.brokaragefirm.api.Error.Errors.ForbiddenException;
import com.brokaragefirm.api.dto.OrderResponse;
import com.brokaragefirm.config.AuthContext;
import com.brokaragefirm.domain.Order;
import com.brokaragefirm.service.AdminMatchingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

  private final AdminMatchingService admin;
  private final AuthContext auth;

  @PostMapping("/{id}/match")
  public ResponseEntity<OrderResponse> match(@PathVariable("id") UUID id, Authentication who) {
    if (!auth.isAdmin(who))
      throw new ForbiddenException("Only admin can match orders");
    Order o = admin.matchOrder(id);
    return ResponseEntity.ok(Mapper.toOrderResponse(o));
  }
}
