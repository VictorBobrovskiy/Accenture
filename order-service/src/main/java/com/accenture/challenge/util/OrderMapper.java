package com.accenture.challenge.util;

import com.accenture.challenge.dto.OrderDto;
import com.accenture.challenge.model.Order;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j // Habilita la capacidad de registrar mensajes de log
public class OrderMapper {

    /**
     * Convierte una entidad Order a un DTO OrderDto.
     *
     * @param order La entidad Order que se va a convertir.
     * @return Un objeto OrderDto.
     */
    public static OrderDto toDto(Order order) {
        if (order == null) {
            log.debug("La orden recibida es nula, no se puede convertir a DTO.");
            return null;
        }

        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setCustomerId(order.getCustomerId());
        orderDto.setOrderAmount(order.getOrderAmount());
        orderDto.setOrderItems(order.getOrderItems());
        orderDto.setStatus(order.getStatus());

        log.debug("Orden convertida a DTO exitosamente, ID de la orden: {}", order.getId());
        return orderDto;
    }

    /**
     * Convierte un DTO OrderDto a una entidad Order.
     *
     * @param orderDto El DTO OrderDto que se va a convertir.
     * @return Un objeto Order.
     */
    public static Order toEntity(OrderDto orderDto) {
        if (orderDto == null) {
            log.debug("El DTO de la orden es nulo, no se puede convertir a entidad.");
            return null;
        }

        Order order = new Order();
        order.setCustomerId(orderDto.getCustomerId());
        order.setOrderAmount(orderDto.getOrderAmount());
        order.setOrderItems(orderDto.getOrderItems());
        order.setStatus(orderDto.getStatus());

        log.debug("DTO de la orden convertido a entidad exitosamente, ID del cliente: {}", orderDto.getCustomerId());
        return order;
    }

    /**
     * Convierte una lista de entidades Order a una lista de DTOs OrderDto.
     *
     * @param orders Lista de entidades Order.
     * @return Lista de objetos OrderDto.
     */
    public List<OrderDto> toDtoList(List<Order> orders) {
        log.debug("Convirtiendo lista de {} órdenes a DTOs.", orders.size());
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de DTOs OrderDto a una lista de entidades Order.
     *
     * @param orderDtos Lista de DTOs OrderDto.
     * @return Lista de objetos Order.
     */
    public List<Order> toEntityList(List<OrderDto> orderDtos) {
        log.debug("Convirtiendo lista de {} DTOs de órdenes a entidades.", orderDtos.size());
        return orderDtos.stream()
                .map(OrderMapper::toEntity)
                .collect(Collectors.toList());
    }
}
