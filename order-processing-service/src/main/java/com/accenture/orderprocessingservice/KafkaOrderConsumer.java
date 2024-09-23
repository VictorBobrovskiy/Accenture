package com.accenture.orderprocessingservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j // Enable logging for this class
@Service
@RequiredArgsConstructor
public class KafkaOrderConsumer {

    private static final String ORDER_TOPIC = "order_topic";

    private final OrderProcessingService orderProcessingService;
    private final ObjectMapper objectMapper;

    /**
     * Método que escucha los mensajes de Kafka en el tópico de órdenes y los consume.
     *
     * @param orderJson El mensaje de la orden en formato JSON
     * @throws JsonProcessingException Si ocurre un error durante la deserialización del JSON
     */
    @KafkaListener(topics = ORDER_TOPIC, groupId = "processing-group")
    public void consumeOrder(String orderJson) throws JsonProcessingException {
        log.debug("Recibiendo mensaje de Kafka: {}", orderJson);

        try {
            // Deserializar el JSON de la orden a un objeto Order
            Order order = objectMapper.readValue(orderJson, Order.class);
            log.debug("Orden deserializada correctamente con ID: {}", order.getId());

            // Procesar la orden (validar y recalcular)
            orderProcessingService.processOrder(order);
            log.debug("Orden procesada correctamente con ID: {}", order.getId());

        } catch (JsonProcessingException e) {
            // Loggear el error en caso de que ocurra un problema durante la deserialización
            log.error("Error al deserializar la orden desde JSON: {}", orderJson, e);
            throw e;
        }
    }
}
