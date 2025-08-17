package com.brokaragefirm.service;

import static org.assertj.core.api.Assertions.assertThat;

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
@Import({ OrderService.class, AdminMatchingService.class })
class AdminMatchingServiceTest {
  @Autowired
  OrderService orderService;
  @Autowired
  AdminMatchingService matching;
  @Autowired
  AssetRepository assetRepo;
  @Autowired
  OrderRepository orderRepo;

  UUID customerId = UUID.randomUUID();

  @BeforeEach
  void seed() {
    assetRepo.save(Asset.builder().customerId(customerId).assetName("TRY")
        .size(new BigDecimal("1000.00")).usableSize(new BigDecimal("1000.00")).build());
    assetRepo.save(Asset.builder().customerId(customerId).assetName("INGA")
        .size(new BigDecimal("0.0000")).usableSize(new BigDecimal("0.0000")).build());
  }

  @Test
  void match_buy_adds_shares() {
    var o = orderService.createOrder(
        new CreateOrderRequest(customerId, "INGA", Side.BUY, new BigDecimal("2.0000"), new BigDecimal("10.00")));
    var matched = matching.matchOrder(o.getId());
    assertThat(matched.getStatus()).isEqualTo(OrderStatus.MATCHED);
    var INGA = assetRepo.findByCustomerIdAndAssetName(customerId, "INGA").orElseThrow();
    assertThat(INGA.getSize()).isEqualTo(new BigDecimal("2.0000"));
    assertThat(INGA.getUsableSize()).isEqualTo(new BigDecimal("2.0000"));
    var lira = assetRepo.findByCustomerIdAndAssetName(customerId, "TRY").orElseThrow();
    assertThat(lira.getUsableSize()).isEqualTo(new BigDecimal("980.00"));
    assertThat(lira.getSize()).isEqualTo(new BigDecimal("980.00"));

  }

  @Test
  void match_sell_adds_try() {
    var INGA = assetRepo.findByCustomerIdAndAssetName(customerId, "INGA").orElseThrow();
    INGA.setSize(new BigDecimal("5.0000"));
    INGA.setUsableSize(new BigDecimal("5.0000"));
    assetRepo.save(INGA);

    var o = orderService.createOrder(
        new CreateOrderRequest(customerId, "INGA", Side.SELL, new BigDecimal("2.0000"), new BigDecimal("10.00")));
    var matched = matching.matchOrder(o.getId());
    assertThat(matched.getStatus()).isEqualTo(OrderStatus.MATCHED);
    var lira = assetRepo.findByCustomerIdAndAssetName(customerId, "TRY").orElseThrow();
    assertThat(lira.getSize()).isEqualTo(new BigDecimal("1020.00"));
    assertThat(lira.getUsableSize()).isEqualTo(new BigDecimal("1020.00"));
    var INGAAfter = assetRepo.findByCustomerIdAndAssetName(customerId, "INGA").orElseThrow();
    assertThat(INGAAfter.getUsableSize()).isEqualTo(new BigDecimal("3.0000"));
    assertThat(INGAAfter.getSize()).isEqualTo(new BigDecimal("3.0000"));
  }
}
