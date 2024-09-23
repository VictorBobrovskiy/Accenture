package com.accenture.orderprocessingservice;


import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
public class Order {

    private Long id;

    private Long customerId;

    private BigDecimal orderAmount;

    private List<OrderItem> orderItems;

    private OrderStatus status;

    private Timestamp createdAt;
}