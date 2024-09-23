package com.accenture.challenge.service;

import com.accenture.challenge.error.OrderNotFoundException;
import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.model.OrderStatus;
import com.accenture.challenge.repository.OrderItemRepository;
import com.accenture.challenge.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

@Slf4j // Habilita el registro de logs con Lombok
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KafkaOrderProducerService kafkaOrderProducerService;
    private final KafkaOrderConsumerService kafkaOrderConsumerService;
    private final RedisTemplate<String, Order> redisTemplate;
    private final ObjectMapper objectMapper;

    // Crear un pool de hilos personalizado con un número fijo de hilos
    private final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    /**
     * Procesa una orden, actualiza su estado y la envía a Kafka y Redis.
     *
     * @param order Orden a procesar.
     * @return CompletableFuture con la orden guardada.
     */
    @Override
    @Transactional
    public CompletableFuture<Order> processOrder(Order order) {

        log.debug("Iniciando procesamiento del pedido: {}", order);

        // Guardar la orden y actualizar el estado
        order.setStatus(OrderStatus.RECEIVED);
        Order savedOrder = orderRepository.save(order);

        log.debug("Pedido guardado en base de datos con ID: {} y estado: {}", savedOrder.getId(), savedOrder.getStatus());

        // Asignar el ID de la orden a cada OrderItem
        for (OrderItem item : order.getOrderItems()) {
            item.setOrderId(savedOrder.getId());
        }
        orderItemRepository.saveAll(order.getOrderItems());

        log.debug("Ítems del pedido guardados en base de datos para el pedido con ID: {}", savedOrder.getId());

        return CompletableFuture.supplyAsync(() -> {

            // Publicar en Kafka para procesamiento adicional
            kafkaOrderProducerService.sendOrder(savedOrder);  // Enviar JSON a Kafka
            log.debug("Pedido enviado a Kafka para procesamiento: {}", savedOrder.getId());

            // Publicar el pedido en la caché de Redis
            redisTemplate.opsForValue().set(savedOrder.getId().toString(), savedOrder);
            log.debug("Pedido publicado en la caché de Redis con ID: {}", savedOrder.getId());

            // Devolver la orden guardada, el procesamiento asincrónico se maneja en Kafka
            return savedOrder;
        }, forkJoinPool);
    }

    /**
     * Recupera una orden por su ID desde la base de datos o desde la caché.
     *
     * @param orderId ID del pedido.
     * @return La orden si existe.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId")
    public Order getOrderById(long orderId) {
        log.debug("Buscando el pedido con ID: {}", orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("No se encontró el pedido con ID: {}", orderId);  // Advertencia si no se encuentra
                    return new OrderNotFoundException("El pedido con id: " + orderId + " no existe");
                });
    }

    /**
     * Recupera los ítems de una orden por su ID.
     *
     * @param orderId ID del pedido.
     * @return Lista de ítems del pedido.
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(long orderId) {
        log.debug("Recuperando los ítems del pedido con ID: {}", orderId);
        return orderItemRepository.findAllByOrderId(orderId);
    }

    /**
     * Recupera todas las órdenes de la base de datos.
     *
     * @return Lista de todas las órdenes.
     */
    @Override
    public List<Order> getAllOrders() {
        log.debug("Recuperando todas las órdenes de la base de datos...");
        return orderRepository.findAll();  // Devolver todas las órdenes
    }
}
