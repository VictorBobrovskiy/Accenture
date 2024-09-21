package com.accenture.challenge.dto;

import com.accenture.challenge.model.OrderItem;
import com.accenture.challenge.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO representing an Order")
public class OrderDto {

    @Schema(description = "Unique identifier of the order", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Customer's unique identifier", example = "12345")
    private Long customerId;

    @NotNull
    @Positive
    @Schema(description = "Total amount of the order", example = "99.99")
    private BigDecimal orderAmount;

    @NotNull
    @Schema(description = "List of items in the order", example = "[\"item1\", \"item2\"]")
    private List<OrderItem> orderItems;

    @NotNull
    @Schema(description = "Current status of the order", example = "PROCESSING")
    private OrderStatus status;

}
