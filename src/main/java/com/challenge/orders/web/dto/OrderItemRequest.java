package com.challenge.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Producto incluido en la orden")
public record OrderItemRequest(
        @Schema(example = "P001")
        @NotBlank(message = "RN-02: El id del producto es obligatorio")
        String id,

        @Schema(example = "2")
        @NotNull(message = "RN-02: La cantidad es obligatoria")
        @Positive(message = "RN-02: La cantidad debe ser mayor que cero")
        Integer cantidad,

        @Schema(example = "100000")
        @NotNull(message = "RN-02: El valor unitario es obligatorio")
        @Positive(message = "RN-02: El valor unitario debe ser mayor que cero")
        BigDecimal valorUnitario
) {
}
