package com.accenture.challenge.service;


import com.accenture.challenge.error.OrderNotFoundException;
import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.model.OrderStatus;
import com.accenture.challenge.repository.OrderItemRepository;
import com.accenture.challenge.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final KafkaOrderProducerService kafkaOrderProducerService;

    private final KafkaOrderConsumerService kafkaOrderConsumerService;

    private final RedisTemplate<String, Order> redisTemplate;

    private final ObjectMapper objectMapper;


    // Create a custom thread pool with a fixed number of threads
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Override
    @Transactional
    public CompletableFuture<Order> processOrder(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            // Save order and update status
            order.setStatus(OrderStatus.RECEIVED);
            Order savedOrder = orderRepository.save(order);

            // Set order ID for each OrderItem
            for (OrderItem item : order.getOrderItems()) {
                item.setOrderId(savedOrder.getId());
            }
            orderItemRepository.saveAll(order.getOrderItems());

            // Publish to Kafka for further processing
            kafkaOrderProducerService.sendOrder(savedOrder); // Send JSON to Kafka

            // Return the saved order for now; processed order will be handled asynchronously
            return savedOrder;
        }, executorService);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId")
    public Order getOrderById(long orderId) {

            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("El orden con id: " + orderId + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(long orderId) {
        return orderItemRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll(); // Return all orders
    }

}