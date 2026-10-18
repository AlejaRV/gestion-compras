package com.challenge.orders.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Datos para crear una orden. El total se calcula automaticamente (RN-03)")
public record CreateOrderRequest(
        @Schema(example = "Juan Perez")
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 120, message = "El cliente no puede superar 120 caracteres")
        String cliente,

        @NotEmpty(message = "RN-01: La orden debe contener al menos un producto")
        List<@Valid OrderItemRequest> productos
) {
}
