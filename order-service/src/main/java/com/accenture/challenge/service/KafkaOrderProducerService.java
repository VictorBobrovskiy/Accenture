package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderProducerService {

    private static final String ORDER_TOPIC = "order_topic";

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;


    public void sendOrder(Order order)  {

        String orderJson = null;

        try {
            orderJson = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        kafkaTemplate.send(ORDER_TOPIC, String.valueOf(order.getId()), orderJson);
    }
}