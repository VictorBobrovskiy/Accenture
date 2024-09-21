package com.accenture.challenge.util;

import com.accenture.challenge.dto.OrderDto;
import com.accenture.challenge.model.Order;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class OrderMapper {

    // Convert Order entity to OrderDto
    public static OrderDto toDto(Order order) {

        if (order == null) {
            return null;
        }

        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setCustomerId(order.getCustomerId());
        orderDto.setOrderAmount(order.getOrderAmount());
        orderDto.setOrderItems(order.getOrderItems());
        orderDto.setStatus(order.getStatus());

        return orderDto;
    }

    // Convert OrderDto to Order entity
    public static Order toEntity(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        Order order = new Order();
        order.setId(orderDto.getId());
        order.setCustomerId(orderDto.getCustomerId());
        order.setOrderAmount(orderDto.getOrderAmount());
        order.setOrderItems(orderDto.getOrderItems());
        order.setStatus(orderDto.getStatus());

        return order;
    }

    // Convert list of Order entities to list of OrderDto
    public List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    // Convert list of OrderDto to list of Order entities
    public List<Order> toEntityList(List<OrderDto> orderDtos) {
        return orderDtos.stream()
                .map(OrderMapper::toEntity)
                .collect(Collectors.toList());
    }

}
