package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderConsumerService {

    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = PROCESSED_ORDER_TOPIC, groupId = "order-group")
    public Order consumeProcessedOrder(String orderJson) {

        Order order;

        // Deserialize the order JSON to Order object
        try {
            order = objectMapper.readValue(orderJson, Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON from Kafka", e);
        }
       return order;
    }
}
