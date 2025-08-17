package com.brokaragefirm.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponse {
  private UUID id;
  private UUID customerId;
  private String assetName;
  private BigDecimal size;
  private BigDecimal usableSize;
}
