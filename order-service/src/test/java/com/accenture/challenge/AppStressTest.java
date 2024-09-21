//package com.accenture.challenge;
//
//import com.accenture.challenge.dto.OrderDto;
//import com.accenture.challenge.service.OrderService;
//import com.accenture.challenge.util.OrderMapper;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.math.BigDecimal;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.LongStream;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@RequiredArgsConstructor
//public class AppStressTest {
//
//    private final MockMvc mockMvc;
//
//    private final OrderService orderService;
//
//    private final ObjectMapper objectMapper;
//
//
//    // Test the processOrder endpoint with 1000 concurrent requests.
//    @Test
//    public void testProcessOrder_1000ConcurrentRequests() throws Exception {
//        // Create a thread pool with 1000 threads
//        ExecutorService executorService = Executors.newFixedThreadPool(1000);
//
//        // Mock the service layer to return a processed order
//        when(orderService.processOrder(any())).thenAnswer(invocation -> {
//            OrderDto orderDto = new OrderDto();
//            orderDto.setId( System.currentTimeMillis());
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
//}
//
