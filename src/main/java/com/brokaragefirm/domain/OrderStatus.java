package com.brokaragefirm.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum OrderStatus {
    PENDING, MATCHED, CANCELED
} // Represents the status of an order, can be PENDING, MATCHED, or CANCELED
