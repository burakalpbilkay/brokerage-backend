package com.brokaragefirm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.brokaragefirm.api.dto.CreateOrderRequest;
import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.Side;
import com.brokaragefirm.repository.AssetRepository;

@SpringBootTest
class ConcurrentReservationTest {
  @Autowired
  OrderService orders;
  @Autowired
  AssetRepository assets;

  private UUID cid;

  @BeforeEach
  void setUp() {
    cid = UUID.randomUUID(); // brand-new test customer

    assets.save(Asset.builder()
        .customerId(cid).assetName("TRY")
        .size(new BigDecimal("1000.00"))
        .usableSize(new BigDecimal("1000.00"))
        .build());

    assets.save(Asset.builder()
        .customerId(cid).assetName("INGA")
        .size(new BigDecimal("0.0000"))
        .usableSize(new BigDecimal("0.0000"))
        .build());
  }

  @Test
  void two_buys_only_one_fits_prevent_over_withdraw() throws Exception {

    var start = new java.util.concurrent.CountDownLatch(1);
    var pool = Executors.newFixedThreadPool(2);

    Callable<Boolean> buy = () -> {
      start.await();
      try {
        orders.createOrder(new CreateOrderRequest(
            cid, "INGA", Side.BUY, new BigDecimal("600.0000"), new BigDecimal("1.00")));
        return true;
      } catch (Exception e) {
        return false;
      }
    };

    var f1 = pool.submit(buy);
    var f2 = pool.submit(buy);
    start.countDown();

    int success = (f1.get() ? 1 : 0) + (f2.get() ? 1 : 0);
    assertThat(success).isEqualTo(1);

    var tryAsset = assets.findByCustomerIdAndAssetName(cid, "TRY").orElseThrow();
    assertThat(tryAsset.getUsableSize()).isEqualTo(new BigDecimal("400.0000"));

    pool.shutdown();
  }
}
