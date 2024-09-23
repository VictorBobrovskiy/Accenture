package com.accenture.orderprocessingservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j // Habilita el uso de log para la clase
@Service
@RequiredArgsConstructor
public class KafkaOrderProducer {
    // Nombre del tópico en Kafka donde se enviarán los pedidos procesados
    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    // Inyección de dependencias de KafkaTemplate y ObjectMapper
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Este método convierte un pedido en formato JSON y lo envía a un tópico en Kafka.
     *
     * @param order El pedido que será procesado.
     * @throws JsonProcessingException Si ocurre un error al convertir el pedido a JSON.
     */
    public void sendProcessedOrder(Order order) throws JsonProcessingException {

        // Convertir el pedido a una cadena JSON
        String orderJson = objectMapper.writeValueAsString(order);
        log.debug("Pedido convertido a JSON: {}", orderJson);

        // Enviar el JSON del pedido procesado al tópico de Kafka
        kafkaTemplate.send(PROCESSED_ORDER_TOPIC, orderJson);
        log.debug("Pedido procesado enviado al tópico de Kafka: {}", PROCESSED_ORDER_TOPIC);

        // Log adicional indicando que el pedido ha sido enviado correctamente
        log.debug("El pedido ha sido enviado correctamente a Kafka");
    }
}
