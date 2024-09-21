package com.accenture.orderprocessingservice;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaOrderProducerService {
    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    public void sendProcessedOrder(Order order) throws JsonProcessingException {

        // Convert the order to JSON string
        String orderJson = objectMapper.writeValueAsString(order);

        // Send the processed order JSON to Kafka
        kafkaTemplate.send(PROCESSED_ORDER_TOPIC, orderJson);
    }

}
