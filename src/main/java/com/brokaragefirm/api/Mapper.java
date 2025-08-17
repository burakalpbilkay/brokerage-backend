package com.brokaragefirm.api;

import com.brokaragefirm.api.dto.OrderResponse;
import com.brokaragefirm.domain.Order;

public final class Mapper {
  private Mapper() {
  }

  public static OrderResponse toOrderResponse(Order o) {
    return OrderResponse.builder()
        .id(o.getId())
        .customerId(o.getCustomerId())
        .assetName(o.getAssetName())
        .side(o.getOrderSide())
        .size(o.getSize())
        .price(o.getPrice())
        .status(o.getStatus())
        .createDate(o.getCreateDate())
        .build();
  }
}
