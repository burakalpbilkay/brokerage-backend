package com.brokaragefirm.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.brokaragefirm.domain.Side;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
  @NotNull
  private UUID customerId;
  @NotBlank
  private String assetName;
  @NotNull
  private Side side;
  @NotNull
  @DecimalMin("0.0001")
  private BigDecimal size;
  @NotNull
  @DecimalMin("0.01")
  private BigDecimal price;
}
