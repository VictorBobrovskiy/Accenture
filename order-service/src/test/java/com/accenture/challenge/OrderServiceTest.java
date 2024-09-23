package com.accenture.challenge;

import com.accenture.challenge.error.OrderNotFoundException;
import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.model.OrderStatus;
import com.accenture.challenge.repository.OrderItemRepository;
import com.accenture.challenge.repository.OrderRepository;
import com.accenture.challenge.service.KafkaOrderConsumerService;
import com.accenture.challenge.service.KafkaOrderProducerService;
import com.accenture.challenge.service.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private KafkaOrderProducerService kafkaOrderProducerService;

    @Mock
    private KafkaOrderConsumerService kafkaOrderConsumerService;

    @Mock
    private RedisTemplate<String, Order> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create a sample Order
        order = new Order();
        order.setId(1L);
        order.setCustomerId(12345L);
        order.setOrderAmount(BigDecimal.valueOf(100.0));
        order.setStatus(OrderStatus.RECEIVED);

        // Create sample OrderItems
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(50.0));

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductId(2L);
        item2.setQuantity(1);
        item2.setPrice(BigDecimal.valueOf(50.0));

        orderItems = Arrays.asList(item1, item2);
        order.setOrderItems(orderItems);
    }

    @Test
    void processOrder_success() {
        // Mock ValueOperations for RedisTemplate
        ValueOperations<String, Order> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock saving the order
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Mock saving order items
        when(orderItemRepository.saveAll(any())).thenReturn(orderItems);

        // Execute the method
        CompletableFuture<Order> result = orderService.processOrder(order);

        // Verify interactions and result
        assertNotNull(result);
        Order processedOrder = result.join();
        assertEquals(OrderStatus.RECEIVED, processedOrder.getStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(any());
        verify(kafkaOrderProducerService, times(1)).sendOrder(any(Order.class));
    }

    @Test
    void getOrderById_orderExists() {
        // Mock finding the order by ID
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Execute the method
        Order result = orderService.getOrderById(1L);

        // Verify interactions and result
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(12345, result.getCustomerId());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_orderDoesNotExist() {
        // Mock finding the order by ID to return empty
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Execute and expect an exception
        Exception exception = assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(1L);
        });

        // Verify the exception message
        String expectedMessage = "El pedido con id: 1 no existe";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderItemsByOrderId() {
        // Mock finding the order items by order ID
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(orderItems);

        // Execute the method
        List<OrderItem> result = orderService.getOrderItemsByOrderId(1L);

        // Verify interactions and result
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderItemRepository, times(1)).findAllByOrderId(1L);
    }

    @Test
    void getAllOrders() {
        // Mock finding all orders
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));

        // Execute the method
        List<Order> result = orderService.getAllOrders();

        // Verify interactions and result
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }
}