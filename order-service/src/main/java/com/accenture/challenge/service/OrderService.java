package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrderService {

    CompletableFuture<Order> processOrder(Order order);

    Order getOrderById(long orderId);

    List<OrderItem> getOrderItemsByOrderId(long orderId);

    List<Order> getAllOrders();

}
