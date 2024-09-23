package com.accenture.challenge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "orderId", cascade = {CascadeType.DETACH}, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "order_amount", nullable = false)
    private BigDecimal orderAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

}