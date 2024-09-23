package com.accenture.orderprocessingservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j // Enable logging
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final Random random = new Random();

    private final Map<Long, Order> orderStorage = new ConcurrentHashMap<>();

    private final KafkaOrderProducer kafkaOrderProducer;

    /**
     * Procesar la orden validando y recalculando el precio.
     *
     * @param order La orden a procesar
     */
    public void processOrder(Order order) {
        log.debug("Iniciando el procesamiento de la orden con ID: {}", order.getId());

        // Establecer el estado como PROCESSING
        order.setStatus(OrderStatus.PROCESSING);

        try {
            // Simular lógica de validación (verificar stock, crédito del cliente, etc.)
            validateOrder(order);
            log.debug("Validación exitosa de la orden con ID: {}", order.getId());

            // Simular el recálculo del precio de la orden
            recalculateOrderPrice(order);
            log.debug("Recalculo del precio completado para la orden con ID: {}", order.getId());

            // Si todo es exitoso, marcar la orden como COMPLETED
            order.setStatus(OrderStatus.COMPLETED);
            orderStorage.putIfAbsent(order.getId(), order);
            log.debug("Orden con ID: {} marcada como COMPLETED", order.getId());

        } catch (Exception e) {
            // En caso de cualquier fallo, marcar la orden como FAILED
            order.setStatus(OrderStatus.FAILED);
            log.error("Error durante el procesamiento de la orden con ID: {}. Estado marcado como FAILED", order.getId(), e);
        }

        // Simular retraso en el procesamiento
        try {
            Thread.sleep(random.nextInt(400) + 100);  // Retraso aleatorio entre 100-500ms
            log.debug("Retraso simulado completado para la orden con ID: {}", order.getId());
        } catch (InterruptedException e) {
            log.error("Error al realizar el retraso simulado para la orden con ID: {}", order.getId(), e);
            Thread.currentThread().interrupt();
        }

        // Enviar la orden procesada de vuelta a Kafka
        try {
            kafkaOrderProducer.sendProcessedOrder(order);
            log.debug("Orden procesada con ID: {} enviada a Kafka exitosamente", order.getId());
        } catch (Exception e) {
            log.error("Fallo al enviar la orden procesada con ID: {} a Kafka", order.getId(), e);
            throw new RuntimeException("Error al enviar la orden procesada a Kafka", e);
        }
    }

    /**
     * Validar la orden.
     *
     * @param order La orden a validar
     */
    private void validateOrder(Order order) {
        log.debug("Validando la orden con ID: {}", order.getId());

        // Verificar que la orden tenga al menos un ítem
        if (order.getOrderItems().isEmpty()) {
            log.error("La orden con ID: {} no tiene ítems. Validación fallida.", order.getId());
            throw new ValidateException("La orden debe contener al menos un ítem.");
        }

        log.debug("La validación de la orden con ID: {} fue exitosa.", order.getId());
    }

    /**
     * Recalcular el precio de la orden aplicando un descuento aleatorio.
     *
     * @param order La orden a recalcular
     */
    private void recalculateOrderPrice(Order order) {
        log.debug("Recalculando el precio de la orden con ID: {}", order.getId());

        BigDecimal totalAmount = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simular la aplicación de un descuento aleatorio
        BigDecimal discount = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 0.1));
        totalAmount = totalAmount.subtract(totalAmount.multiply(discount));

        order.setOrderAmount(totalAmount);
        log.debug("Precio recalculado para la orden con ID: {} es: {}", order.getId(), totalAmount);
    }
}
