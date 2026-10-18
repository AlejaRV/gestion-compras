package com.challenge.orders.web.dto;

import com.challenge.orders.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de la orden")
public record UpdateStatusRequest(
        @Schema(example = "PAGADA")
        @NotNull(message = "El estado es obligatorio")
        OrderStatus estado
) {
}
