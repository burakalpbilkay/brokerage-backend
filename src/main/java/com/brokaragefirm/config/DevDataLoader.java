package com.brokaragefirm.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.brokaragefirm.domain.Asset;
import com.brokaragefirm.domain.Customer;
import com.brokaragefirm.repository.AssetRepository;
import com.brokaragefirm.repository.CustomerRepository;
import com.brokaragefirm.service.Money;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DevDataLoader implements CommandLineRunner {

  private final CustomerRepository customerRepo;
  private final AssetRepository assetRepo;
  private final PasswordEncoder encoder;

  @Override
  public void run(String... args) {
    var user1 = customerRepo.findByUsername("user1").orElseGet(() -> customerRepo.save(Customer.builder()
        .username("user1").password(encoder.encode("pass123"))
        .roles("ROLE_CUSTOMER").enabled(true).build()));

    var user2 = customerRepo.findByUsername("user2").orElseGet(() -> customerRepo.save(Customer.builder()
        .username("user2").password(encoder.encode("pass123"))
        .roles("ROLE_CUSTOMER").enabled(true).build()));

    seedTry(user1.getId(), new BigDecimal("100000.00"));
    seedTry(user2.getId(), new BigDecimal("100000.00"));
    seedAsset(user1.getId(), "INGA", new BigDecimal("10.0000"));
  }

  /**
   * Seeds the TRY asset for a customer with a specified amount.
   **/
  private void seedTry(java.util.UUID customerId, BigDecimal amount) {
    assetRepo.findByCustomerIdAndAssetName(customerId, "TRY").orElseGet(() -> assetRepo.save(Asset.builder()
        .customerId(customerId).assetName("TRY")
        .size(amount.setScale(Money.PRICE_SCALE, Money.RM))
        .usableSize(amount.setScale(Money.PRICE_SCALE, Money.RM))
        .build()));
  }

  // Seeds a specific asset for a customer with a given amount.
  private void seedAsset(java.util.UUID customerId, String name, BigDecimal shares) {
    assetRepo.findByCustomerIdAndAssetName(customerId, name).orElseGet(() -> assetRepo.save(Asset.builder()
        .customerId(customerId).assetName(name)
        .size(shares).usableSize(shares).build()));
  }
}
