package com.brokaragefirm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_created", columnList = "create_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "asset_name", nullable = false)
  private String assetName; // non-TRY stock symbol

  @Enumerated(EnumType.STRING)
  @Column(name = "order_side", nullable = false)
  private Side orderSide;

  @Column(precision = 38, scale = 4, nullable = false)
  private BigDecimal size; // shares

  @Column(precision = 38, scale = 2, nullable = false)
  private BigDecimal price; // per share (TRY)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status; // PENDING, MATCHED, CANCELED

  @Column(name = "create_date", nullable = false)
  private Instant createDate;

  @Version
  private long version; // optimistic locking version

  @PrePersist
  void prePersist() {
    if (status == null)
      status = OrderStatus.PENDING;
    if (createDate == null)
      createDate = Instant.now();
  }
}
