package com.brokaragefirm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.brokaragefirm.api.dto.CreateOrderRequest;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.AssetRepository;

@SpringBootTest
class OrderLifecycleTest {
  @Autowired
  OrderService orders;
  @Autowired
  AdminMatchingService settlement;
  @Autowired
  AssetRepository assets;

  @Test
  void cannot_cancel_after_match() {
    // Seed db
    UUID cid = UUID.randomUUID();
    assets.save(Asset.builder()
        .customerId(cid).assetName("TRY")
        .size(new BigDecimal("1000.00")).usableSize(new BigDecimal("1000.00"))
        .build());
    assets.save(Asset.builder()
        .customerId(cid).assetName("INGA")
        .size(new BigDecimal("0.0000")).usableSize(new BigDecimal("0.0000"))
        .build());
    var o = orders
        .createOrder(new CreateOrderRequest(cid, "INGA", Side.BUY, new BigDecimal("1.0000"), new BigDecimal("10.00")));
    settlement.matchOrder(o.getId());
    assertThatThrownBy(() -> orders.cancelOrder(o.getId()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Only PENDING");

    // Verify balances didn’t change after failed cancel
    var tryAsset = assets.findByCustomerIdAndAssetName(cid, "TRY").orElseThrow();
    var INGA = assets.findByCustomerIdAndAssetName(cid, "INGA").orElseThrow();

    assertThat(tryAsset.getSize()).isEqualByComparingTo(new BigDecimal("990.00"));
    assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(new BigDecimal("990.00"));
    assertThat(INGA.getSize()).isEqualTo(new BigDecimal("1.0000"));
    assertThat(INGA.getUsableSize()).isEqualTo(new BigDecimal("1.0000"));
  }
}
