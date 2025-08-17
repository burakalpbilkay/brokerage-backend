package com.brokaragefirm.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Side {
    BUY, SELL
} // Represents the side of an order, either BUY or SELL