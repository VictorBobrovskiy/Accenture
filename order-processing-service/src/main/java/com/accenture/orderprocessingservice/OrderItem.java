package com.accenture.orderprocessingservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


import java.math.BigDecimal;


@Data
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long productId;

    private int quantity;

    private BigDecimal price;
}
