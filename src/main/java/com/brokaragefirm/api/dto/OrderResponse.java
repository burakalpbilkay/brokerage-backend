package com.brokaragefirm.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.brokaragefirm.domain.OrderStatus;
import com.brokaragefirm.domain.Side;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
  private UUID id;
  private UUID customerId;
  private String assetName;
  private Side side;
  private BigDecimal size;
  private BigDecimal price;
  private OrderStatus status;
  private Instant createDate;
}
