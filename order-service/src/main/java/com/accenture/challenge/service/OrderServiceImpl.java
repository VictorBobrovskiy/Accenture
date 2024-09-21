package com.accenture.challenge.service;


import com.accenture.challenge.error.OrderNotFoundException;
import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.model.OrderStatus;
import com.accenture.challenge.repository.OrderItemRepository;
import com.accenture.challenge.repository.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final KafkaOrderProducerService kafkaOrderProducerService;

    private final KafkaOrderConsumerService kafkaOrderConsumerService;

    private final RedisTemplate<String, Order> redisTemplate;

    private final Random random;


    // Create a custom thread pool with a fixed number of threads
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Override
    @Transactional
    public CompletableFuture<Order> processOrder(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            // Save order and update status
            order.setStatus(OrderStatus.RECEIVED);
            Order savedOrder = orderRepository.save(order);

            // Publish to Kafka for further processing
            kafkaOrderProducerService.sendOrder(savedOrder);

            // Simulate processing delay and wait for Kafka response
            Order processedOrder = kafkaOrderConsumerService.consumeProcessedOrder(savedOrder.getId().toString());

            if (processedOrder != null) {
                Order savedProcessedOrder = orderRepository.save(processedOrder);

                // Cache the processed order
                redisTemplate.opsForValue().set("order:" + savedProcessedOrder.getId(), savedProcessedOrder);

                return savedProcessedOrder;
            }
            return null;
        }, executorService);
}

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId")
    public Order getOrderById(Long orderId) {

            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("El orden con id: " + orderId + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        return orderItemRepository.findAllByOrderId(orderId);
    }

}