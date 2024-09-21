package com.accenture.orderprocessingservice;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaOrderConsumerService {

    private static final String ORDER_TOPIC = "order_topic";

    private OrderProcessingService orderProcessingService;

    private ObjectMapper objectMapper;

    @KafkaListener(topics = ORDER_TOPIC, groupId = "processing-group")
    public void consumeOrder(String orderJson) throws JsonProcessingException {

        // Deserialize the order JSON to Order object
        Order order = objectMapper.readValue(orderJson, Order.class);

        // Process the order (validate and recalculate)
        orderProcessingService.processOrder(order);
    }
}
