package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Habilita la capacidad de registrar mensajes de log
public class KafkaOrderProducerService {

    private static final String ORDER_TOPIC = "order_topic"; // Nombre del tema de Kafka para órdenes

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Envía una orden al tópico de Kafka.
     *
     * @param order Orden que se va a enviar.
     */
    public void sendOrder(Order order) {

        String orderJson = null;

        try {
            // Convertir la orden en JSON
            orderJson = objectMapper.writeValueAsString(order);
            log.debug("Orden convertida a JSON exitosamente: {}", orderJson);
        } catch (JsonProcessingException e) {
            log.error("Error al convertir la orden a JSON: {}", e.getMessage());
            throw new RuntimeException("No se pudo procesar la orden en formato JSON", e); // Mensaje traducido
        }

        // Enviar la orden al tópico de Kafka
        kafkaTemplate.send(ORDER_TOPIC, String.valueOf(order.getId()), orderJson);
        log.debug("Orden enviada a Kafka, ID de la orden: {}", order.getId());
    }
}
