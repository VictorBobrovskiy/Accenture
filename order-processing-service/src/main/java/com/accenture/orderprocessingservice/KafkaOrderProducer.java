package com.accenture.orderprocessingservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {
    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void sendProcessedOrder(Order order) throws JsonProcessingException {

        // Convert the order to JSON string
        String orderJson = objectMapper.writeValueAsString(order);

        // Send the processed order JSON to Kafka
        kafkaTemplate.send(PROCESSED_ORDER_TOPIC, orderJson);
    }

}
