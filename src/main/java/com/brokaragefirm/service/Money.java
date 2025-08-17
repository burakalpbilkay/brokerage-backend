package com.brokaragefirm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
  private Money() {
  }

  public static final int PRICE_SCALE = 2;
  public static final int SIZE_SCALE = 4;
  public static final RoundingMode RM = RoundingMode.HALF_UP;

  public static BigDecimal cost(BigDecimal price, BigDecimal size) {
    return price.setScale(PRICE_SCALE, RM)
        .multiply(size.setScale(SIZE_SCALE, RM))
        .setScale(PRICE_SCALE, RM);
  }
}
