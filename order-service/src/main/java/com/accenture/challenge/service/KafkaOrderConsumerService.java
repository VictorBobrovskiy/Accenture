package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.accenture.challenge.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderConsumerService {

    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    private final NotificationService  notificationService;

    private final RedisTemplate<String, Order> redisTemplate;


    @KafkaListener(topics = PROCESSED_ORDER_TOPIC, groupId = "order-group")
    public void consumeProcessedOrder(String orderJson) {
        try {
            processedOrder = objectMapper.readValue(orderJson, Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON from Kafka", e);
        }
    }

    private void updateOrder(Order processedOrder) {
        // Logic to find and update the order in the repository
        Order existingOrder = orderRepository.findById(processedOrder.getId()).orElse(null);
        if (existingOrder != null) {
            existingOrder.setStatus(processedOrder.getStatus());
            // Update other fields as necessary
            orderRepository.save(existingOrder);
        }
    }
}
