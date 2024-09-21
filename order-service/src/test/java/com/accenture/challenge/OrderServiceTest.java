//package com.accenture.challenge;
//
//import com.accenture.challenge.model.Order;
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
//    public void testProcessOrder() {
//        // Given
//        Order order = new Order();
//        order.setId(123L);
//        order.setStatus(OrderStatus.RECEIVED);
//
//        // Mock dependencies
//        when(orderRepository.save(any(Order.class))).thenReturn(order);
//        when(kafkaOrderConsumerService.consumeProcessedOrder(anyString())).thenReturn(order);
//
//        // When
//        CompletableFuture<Order> futureOrder = orderService.processOrder(order);
//
//        // Then
//        verify(orderRepository, times(1)).save(order);
//        verify(kafkaOrderProducerService, times(1)).sendOrder(order);
//        verify(redisTemplate, times(1)).opsForValue().set("order:" + order.getId(), order);
//    }
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
//        assert foundOrder.getId().equals(123L);
//    }
//}