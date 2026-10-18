package com.challenge.orders.domain;

import com.challenge.orders.domain.exception.InvalidOrderException;

import java.math.BigDecimal;

/**
 * Producto dentro de una orden. Inmutable y auto-validado.
 * Se usa {@link BigDecimal} para valores monetarios (evita errores de redondeo de double).
 */
public record OrderItem(String id, int cantidad, BigDecimal valorUnitario) {

    public OrderItem {
        if (id == null || id.isBlank()) {
            throw new InvalidOrderException("RN-02", "El id del producto es obligatorio");
        }
        if (cantidad <= 0) {
            throw new InvalidOrderException("RN-02",
                    "La cantidad del producto " + id + " debe ser mayor que cero");
        }
        if (valorUnitario == null || valorUnitario.signum() <= 0) {
            throw new InvalidOrderException("RN-02",
                    "El valor unitario del producto " + id + " debe ser mayor que cero");
        }
    }

    /** Subtotal = cantidad x valorUnitario. */
    public BigDecimal subtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
