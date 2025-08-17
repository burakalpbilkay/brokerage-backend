package com.brokaragefirm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.brokaragefirm.service.Money;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assets", uniqueConstraints = @UniqueConstraint(name = "uk_asset_customer_name", columnNames = {
    "customer_id", "asset_name" }), indexes = {
        @Index(name = "idx_asset_customer", columnList = "customer_id"),
        @Index(name = "idx_asset_name", columnList = "asset_name")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @NotBlank
  @Column(name = "asset_name", nullable = false)
  private String assetName; // e.g., "INGA", "TRY"

  @NotNull
  @DecimalMin("0")
  @Column(precision = 38, scale = 4, nullable = false)
  private BigDecimal size; // total shares owned

  @NotNull
  @DecimalMin("0")
  @Column(name = "usable_size", precision = 38, scale = 4, nullable = false)
  private BigDecimal usableSize; // shares available for trading

  @Version
  private long version; // optimistic locking version

  @Column(updatable = false)
  private Instant createdAt;

  private Instant updatedAt;

  // Automatically set timestamps and defaults before persisting
  @PrePersist
  void prePersist() {
    createdAt = Instant.now();
    updatedAt = createdAt;
    if (size == null)
      size = BigDecimal.ZERO.setScale(Money.SIZE_SCALE, Money.RM);
    ;
    if (usableSize == null)
      usableSize = BigDecimal.ZERO.setScale(Money.SIZE_SCALE, Money.RM);
    ;
  }

  // Update timestamp before updating
  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
