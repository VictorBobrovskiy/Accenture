//package com.accenture.challenge;
//
//import com.accenture.challenge.model.Order;
//import com.accenture.challenge.model.OrderItem;
//import com.accenture.challenge.model.OrderStatus;
//import com.accenture.challenge.repository.OrderRepository;
//import com.accenture.challenge.service.KafkaOrderConsumerService;
//import com.accenture.challenge.service.KafkaOrderProducerService;
//import com.accenture.challenge.service.OrderServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class OrderServiceTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private RedisTemplate<String, Order> redisTemplate;
//
//    @Mock
//    private KafkaOrderProducerService kafkaOrderProducerService;
//
//    @Mock
//    private KafkaOrderConsumerService kafkaOrderConsumerService;
//
//    @InjectMocks
//    private OrderServiceImpl orderService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testProcessOrder() throws Exception {
//        // Given
//        Order order = new Order();
//        order.setId(123L);
//        order.setStatus(OrderStatus.RECEIVED);
//
//        // Initialize order items
//        List<OrderItem> orderItems = new ArrayList<>();
//        OrderItem item = new OrderItem();
//        item.setProductId(123L);
//        item.setPrice(BigDecimal.valueOf(49.99));
//        item.setQuantity(1);
//        item.setOrder(order); // Set the order reference to prevent cyclic dependency issues
//        orderItems.add(item);
//
//        order.setOrderItems(orderItems); // Set the order items
//
//        // Mock dependencies
//        when(orderRepository.save(any(Order.class))).thenReturn(order);
//        when(kafkaOrderConsumerService.consumeProcessedOrder(anyString())).thenReturn(order);
//
//        // When
//        CompletableFuture<Order> futureOrder = orderService.processOrder(order);
//        Order processedOrder = futureOrder.get(); // Wait for completion
//
//        // Then
//        verify(orderRepository, times(1)).save(order);
//        verify(kafkaOrderProducerService, times(1)).sendOrder(order);
//        verify(redisTemplate, times(1)).opsForValue().set("order:" + order.getId(), order);
//    }
//
//
//
//    @Test
//    public void testGetOrderById() {
//        // Given
//        Order order = new Order();
//        order.setId(123L);
//
//        // Mock repository
//        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
//
//        // When
//        Order foundOrder = orderService.getOrderById(1L);
//
//        // Then
//        verify(orderRepository, times(1)).findById(1L);
//        assert foundOrder != null;
//        assert foundOrder.getId() == 123;
//    }
//}