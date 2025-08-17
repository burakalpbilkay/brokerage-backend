package com.brokaragefirm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void clamp_and_cost_scale_are_correct() {
        var price = new BigDecimal("10.005"); // -> 10.01
        var size = new BigDecimal("1.23456"); // -> 1.2346
        assertThat(Money.cost(price, size)).isEqualByComparingTo("12.36"); // 10.01 * 1.2346 = 12.356... -> 12.36
    }
}
