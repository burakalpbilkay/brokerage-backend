package com.brokaragefirm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.brokaragefirm.api.dto.CreateOrderRequest;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.AssetRepository;
import com.brokaragefirm.repository.OrderRepository;

@DataJpaTest
@Import(OrderService.class)
class OrderServiceTest {

  @Autowired
  OrderService orderService;
  @Autowired
  AssetRepository assetRepo;
  @Autowired
  OrderRepository orderRepo;

  UUID customerId = UUID.randomUUID();

  @BeforeEach
  void setup() {
    assetRepo.save(Asset.builder()
        .customerId(customerId).assetName("TRY")
        .size(new BigDecimal("1000.00")).usableSize(new BigDecimal("1000.00"))
        .build());
    assetRepo.save(Asset.builder()
        .customerId(customerId).assetName("INGA")
        .size(new BigDecimal("5.0000")).usableSize(new BigDecimal("5.0000"))
        .build());
  }

  @Test
  void buy_reserves_try_and_creates_pending() {
    var req = new CreateOrderRequest(customerId, "INGA", Side.BUY, new BigDecimal("2.0000"), new BigDecimal("10.00"));
    var o = orderService.createOrder(req);

    assertThat(o.getStatus()).isEqualTo(OrderStatus.PENDING);
    var tryAsset = assetRepo.findByCustomerIdAndAssetName(customerId, "TRY").orElseThrow();
    assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal("980.00"));
  }

  @Test
  void sell_reserves_shares() {
    var req = new CreateOrderRequest(customerId, "INGA", Side.SELL, new BigDecimal("1.5000"), new BigDecimal("10.00"));
    var o = orderService.createOrder(req);
    var INGA = assetRepo.findByCustomerIdAndAssetName(customerId, "INGA").orElseThrow();
    assertThat(INGA.getUsableSize()).isEqualTo(new BigDecimal("3.5000"));
    assertThat(o.getStatus()).isEqualTo(OrderStatus.PENDING);
  }

  @Test
  void cancel_buy_restores_try() {
    var o = orderService.createOrder(
        new CreateOrderRequest(customerId, "INGA", Side.BUY, new BigDecimal("1.0000"), new BigDecimal("100.00")));
    orderService.cancelOrder(o.getId());
    var tryAsset = assetRepo.findByCustomerIdAndAssetName(customerId, "TRY").orElseThrow();
    assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal("1000.00"));
    assertThat(orderRepo.findById(o.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELED);
  }

  @Test
  void cancel_sell_restores_shares() {
    var o = orderService.createOrder(
        new CreateOrderRequest(customerId, "INGA", Side.SELL, new BigDecimal("2.0000"), new BigDecimal("10.00")));
    orderService.cancelOrder(o.getId());
    var INGA = assetRepo.findByCustomerIdAndAssetName(customerId, "INGA").orElseThrow();
    assertThat(INGA.getUsableSize()).isEqualTo(new BigDecimal("5.0000"));
    assertThat(orderRepo.findById(o.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELED);
  }

  @Test
  void insufficient_throws() {
    var req = new CreateOrderRequest(customerId, "INGA", Side.BUY, new BigDecimal("1000.0000"),
        new BigDecimal("1000.00"));
    assertThatThrownBy(() -> orderService.createOrder(req))
        .isInstanceOf(com.brokaragefirm.api.Error.Errors.InsufficientBalanceException.class)
        .hasMessageContaining("Insufficient");
  }

  @Test
  void cannot_trade_try_against_try() {
    var req = new CreateOrderRequest(customerId, "TRY", Side.BUY,
        new BigDecimal("1.0000"), new BigDecimal("10.00"));
    assertThatThrownBy(() -> orderService.createOrder(req))
        .isInstanceOf(com.brokaragefirm.api.Error.Errors.BadRequestException.class)
        .hasMessageContaining("TRY");
  }
}
