package com.accenture.challenge.service;

import com.accenture.challenge.model.Order;
import com.accenture.challenge.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j  // Habilita el registro de logs con Lombok
@Service
@RequiredArgsConstructor
public class KafkaOrderConsumerService {

    private static final String PROCESSED_ORDER_TOPIC = "processed_order_topic";

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final RedisTemplate<String, Order> redisTemplate;

    /**
     * Escucha los mensajes del tópico de Kafka y procesa el pedido recibido.
     *
     * @param orderJson El pedido en formato JSON recibido desde Kafka.
     */
    @KafkaListener(topics = PROCESSED_ORDER_TOPIC, groupId = "order-group")
    public void consumeProcessedOrder(String orderJson) {
        log.debug("Pedido recibido desde Kafka: {}", orderJson);  // Log indicando que se recibió un mensaje desde Kafka

        Order processedOrder;
        try {
            // Convertir el mensaje JSON a un objeto Order
            processedOrder = objectMapper.readValue(orderJson, Order.class);
            log.debug("Pedido convertido desde JSON a objeto Order: {}", processedOrder);
        } catch (JsonProcessingException e) {
            log.error("Error al procesar el JSON del pedido desde Kafka: {}", e.getMessage());  // Error al procesar JSON
            throw new RuntimeException("Error al procesar el JSON desde Kafka", e);
        }

        // Lógica para actualizar el pedido en la base de datos
        updateOrder(processedOrder);

        // Enviar notificación al cliente
        notificationService.notify(processedOrder.getCustomerId());
        log.debug("Notificación enviada al cliente con ID: {}", processedOrder.getCustomerId());

        // Publicar el pedido en la caché de Redis
        redisTemplate.opsForValue().set(processedOrder.getId().toString(), processedOrder);
        log.debug("Pedido publicado en la caché de Redis con ID: {}", processedOrder.getId());
    }

    /**
     * Actualiza el pedido en la base de datos con la información procesada.
     *
     * @param processedOrder El pedido procesado.
     */
    private void updateOrder(Order processedOrder) {
        // Lógica para buscar y actualizar el pedido en el repositorio
        Order existingOrder = orderRepository.findById(processedOrder.getId()).orElse(null);
        if (existingOrder != null) {
            log.debug("Pedido encontrado en la base de datos, actualizando: {}", existingOrder.getId());
            existingOrder.setStatus(processedOrder.getStatus());
            // Actualizar otros campos si es necesario
            orderRepository.save(existingOrder);
            log.debug("Pedido con ID {} actualizado exitosamente en la base de datos.", existingOrder.getId());
        } else {
            log.warn("No se encontró un pedido con ID: {}", processedOrder.getId());  // Pedido no encontrado
        }
    }
}
