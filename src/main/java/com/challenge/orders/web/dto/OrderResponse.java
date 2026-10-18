package com.challenge.orders.web.dto;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderItem;
import com.challenge.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String cliente,
        List<Item> productos,
        BigDecimal total,
        OrderStatus estado,
        Instant fechaCreacion,
        Instant fechaActualizacion
) {

    public record Item(String id, int cantidad, BigDecimal valorUnitario, BigDecimal subtotal) {

        static Item from(OrderItem item) {
            return new Item(item.id(), item.cantidad(), item.valorUnitario(), item.subtotal());
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCliente(),
                order.getProductos().stream().map(Item::from).toList(),
                order.getTotal(),
                order.getEstado(),
                order.getFechaCreacion(),
                order.getFechaActualizacion());
    }
}
