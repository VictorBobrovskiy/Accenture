package com.accenture.challenge.controller;

import com.accenture.challenge.util.OrderMapper;
import com.accenture.challenge.dto.OrderDto;
import com.accenture.challenge.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Processing", description = "Endpoints for processing and retrieving orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/processOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process a new order", description = "Asynchronously processes a new order and returns the processed order details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public CompletableFuture<ResponseEntity<OrderDto>> processOrder(@Valid @RequestBody OrderDto orderDto) {

        return orderService.processOrder(OrderMapper.toEntity(orderDto))
                .thenApply(processedOrder -> {
                    if (processedOrder == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(null);
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(OrderMapper.toDto(processedOrder));
                });
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process a new order", description = "Asynchronously processes a new order and returns the processed order details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {

        OrderDto order = OrderMapper.toDto(orderService.getOrderById(orderId));

        return new ResponseEntity<>(order, HttpStatus.OK);

    }
}