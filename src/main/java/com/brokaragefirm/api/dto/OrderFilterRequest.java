package com.brokaragefirm.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.brokaragefirm.domain.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFilterRequest {
  private UUID customerId;
  private Instant from;
  private Instant to;
  private OrderStatus status;
  private String assetName;
  private Integer page;
  private Integer size;
}
