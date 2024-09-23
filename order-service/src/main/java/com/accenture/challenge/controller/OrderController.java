package com.accenture.challenge.controller;

import com.accenture.challenge.dto.OrderDto;
import com.accenture.challenge.model.Order;
import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.service.OrderService;
import com.accenture.challenge.util.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j  // Habilita el registro de logs con Lombok
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Procesamiento de Pedidos", description = "Endpoints para procesar y recuperar pedidos")
public class OrderController {

    private final OrderService orderService;

    /**
     * Procesa una nueva orden de manera asíncrona y devuelve los detalles del pedido procesado.
     *
     * @param orderDto objeto DTO que representa la orden a procesar
     * @return ResponseEntity con el pedido procesado o un error
     */
    @PostMapping(value = "/processOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Procesar una nueva orden", description = "Procesa una nueva orden de manera asíncrona y devuelve los detalles del pedido procesado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Entrada inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public CompletableFuture<ResponseEntity<OrderDto>> processOrder(@Valid @RequestBody OrderDto orderDto) {

        log.debug("Recibido el pedido para procesar: {}", orderDto);  // Log indicando que se ha recibido el pedido

        return orderService.processOrder(OrderMapper.toEntity(orderDto))
                .thenApply(processedOrder -> {
                    if (processedOrder == null) {
                        log.error("Error al procesar el pedido, el resultado es nulo.");  // Log en caso de error
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(null);
                    }
                    log.info("Pedido procesado exitosamente: {}", processedOrder);  // Log indicando éxito
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(OrderMapper.toDto(processedOrder));
                });
    }

    /**
     * Recupera los detalles de una orden específica por su ID.
     *
     * @param orderId ID del pedido a recuperar
     * @return ResponseEntity con los detalles de la orden o un error
     */
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Recuperar un pedido", description = "Recupera los detalles de una orden específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido recuperado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Entrada inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {

        log.debug("Recuperando el pedido con ID: {}", orderId);  // Log indicando que se está intentando recuperar el pedido

        Order order = orderService.getOrderById(orderId);

        order.setOrderItems(orderService.getOrderItemsByOrderId(orderId));  // Asignar los artículos del pedido

        OrderDto orderDto = OrderMapper.toDto(order);

        log.info("Pedido con ID {} recuperado exitosamente.", orderId);  // Log indicando que se recuperó el pedido

        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }

    /**
     * Recupera una lista de todas las órdenes existentes.
     *
     * @return ResponseEntity con una lista de todas las órdenes
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obtener todos los pedidos", description = "Devuelve una lista de todos los pedidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos recuperados exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.debug("Recuperando todos los pedidos...");  // Log indicando el inicio de la recuperación de todos los pedidos

        List<Order> orders = orderService.getAllOrders();

        if (orders.isEmpty()) {
            log.warn("No se encontraron pedidos.");  // Log en caso de no encontrar pedidos
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // Asignar los OrderItems a cada pedido
        orders.forEach(order -> {
            List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(order.getId());
            order.setOrderItems(orderItems);
        });

        List<OrderDto> orderDtos = orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Pedidos recuperados exitosamente.");  // Log indicando el éxito en la recuperación de los pedidos
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }
}
