package com.accenture.orderprocessingservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderConsumer {

    private static final String ORDER_TOPIC = "order_topic";

    private final OrderProcessingService orderProcessingService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ORDER_TOPIC, groupId = "processing-group")
    public void consumeOrder(String orderJson) throws JsonProcessingException {

        // Deserialize the order JSON to Order object
        Order order = objectMapper.readValue(orderJson, Order.class);

        // Process the order (validate and recalculate)
        orderProcessingService.processOrder(order);
    }
}
