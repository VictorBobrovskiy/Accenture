//package com.accenture.challenge;
//
//import com.accenture.challenge.dto.OrderDto;
//import com.accenture.challenge.error.OrderNotFoundException;
//import com.accenture.challenge.service.OrderService;
//import com.accenture.challenge.util.OrderMapper;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.LongStream;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@RequiredArgsConstructor
//public class OrderControllerIntegrationTest {
//
//    private final MockMvc mockMvc;
//
//    private final OrderService orderService;
//
//    private final ObjectMapper objectMapper;
//
//
//    // Test the processOrder endpoint with 1000 concurrent requests.
//
//    @Test
//    public void testProcessOrder_1000ConcurrentRequests() throws Exception {
//        // Create a thread pool with 1000 threads
//        ExecutorService executorService = Executors.newFixedThreadPool(1000);
//
//        // Mock the service layer to return a processed order
//        when(orderService.processOrder(any())).thenAnswer(invocation -> {
//            OrderDto orderDto = new OrderDto();
//            orderDto.setId(System.currentTimeMillis());
//            orderDto.setCustomerId(System.currentTimeMillis());
//            orderDto.setOrderAmount(BigDecimal.valueOf(100));
//            return CompletableFuture.completedFuture(OrderMapper.toEntity(orderDto));
//        });
//
//        // Submit 1000 concurrent requests
//        CompletableFuture<?>[] futures = LongStream.range(0, 1000)
//                .mapToObj(i -> CompletableFuture.runAsync(() -> {
//                    try {
//                        // Create an order DTO with unique orderId and customerId
//                        OrderDto orderDto = new OrderDto();
//                        orderDto.setId(i);
//                        orderDto.setCustomerId(i);
//                        orderDto.setOrderAmount(BigDecimal.valueOf(100));
//
//                        // Perform the POST request to process the order
//                        mockMvc.perform(post("/api/orders/processOrder")
//                                        .contentType(MediaType.APPLICATION_JSON)
//                                        .content(objectMapper.writeValueAsString(orderDto)))
//                                .andExpect(status().isOk());
//                    } catch (Exception e) {
//                        e.printStackTrace(); // Handle exceptions (e.g., log them)
//                    }
//                }, executorService))
//                .toArray(CompletableFuture[]::new);
//
//        // Wait for all requests to complete
//        CompletableFuture.allOf(futures).join();
//
//        // Shutdown the executor service
//        executorService.shutdown();
//    }
//
//
//    @Test
//    public void testProcessOrder_Success() throws Exception {
//        // Create a mock OrderDto
//        OrderDto orderDto = new OrderDto();
//        orderDto.setId(1L);
//        orderDto.setCustomerId(12345L);
//        orderDto.setOrderAmount(BigDecimal.valueOf(100));
//
//        // Mock the service layer to return a processed order
//        when(orderService.processOrder(any())).thenReturn(CompletableFuture.completedFuture(OrderMapper.toEntity(orderDto)));
//
//        // Perform the POST request to process the order
//        mockMvc.perform(post("/api/orders/processOrder")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(orderDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.orderId").value("order-123"))
//                .andExpect(jsonPath("$.customerId").value("customer-123"))
//                .andExpect(jsonPath("$.orderAmount").value(100));
//    }
//
//    /**
//     * Test the /{orderId} endpoint for retrieving an order by ID.
//     */
//    @Test
//    public void testGetOrder_Success() throws Exception {
//        // Create a mock OrderDto
//        OrderDto orderDto = new OrderDto();
//        orderDto.setId(1L);
//        orderDto.setCustomerId(12345L);
//        orderDto.setOrderAmount(BigDecimal.valueOf(100));
//
//        // Mock the service layer to return the order
//        when(orderService.getOrderById(1L)).thenReturn(OrderMapper.toEntity(orderDto));
//
//        // Perform the GET request to retrieve the order
//        mockMvc.perform(get("/api/orders/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.orderId").value("order-123"))
//                .andExpect(jsonPath("$.customerId").value("customer-123"))
//                .andExpect(jsonPath("$.orderAmount").value(100));
//    }
//
//
//    // Test the /{orderId} endpoint for retrieving an order by ID when the order is not found.
//    @Test
//    public void testGetOrder_NotFound() throws Exception {
//        // Mock the service layer to throw an OrderNotFoundException
//        when(orderService.getOrderById(1L)).thenThrow(new OrderNotFoundException("Order not found"));
//
//        // Perform the GET request to retrieve the order
//        mockMvc.perform(get("/api/orders/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//}