package com.challenge.orders.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Datos de prueba reutilizables (Test Data Builder simplificado). */
public final class OrderFixtures {

    public static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");
    public static final Instant T1 = Instant.parse("2026-01-01T11:00:00Z");

    private OrderFixtures() {
    }

    public static List<OrderItem> defaultItems() {
        return List.of(
                new OrderItem("P001", 2, new BigDecimal("100000")),
                new OrderItem("P002", 3, new BigDecimal("15000")));
    }

    /** Orden con total esperado 245000 (2 x 100000 + 3 x 15000). */
    public static Order newOrder() {
        return Order.create(ORDER_ID, "Juan Perez", defaultItems(), T0);
    }

    /** Orden llevada a un estado dado siguiendo el camino feliz de la maquina de estados. */
    public static Order orderIn(OrderStatus status) {
        Order order = newOrder();
        switch (status) {
            case CREADA -> { }
            case PAGADA -> order.transitionTo(OrderStatus.PAGADA, T0);
            case ENVIADA -> {
                order.transitionTo(OrderStatus.PAGADA, T0);
                order.transitionTo(OrderStatus.ENVIADA, T0);
            }
            case ENTREGADA -> {
                order.transitionTo(OrderStatus.PAGADA, T0);
                order.transitionTo(OrderStatus.ENVIADA, T0);
                order.transitionTo(OrderStatus.ENTREGADA, T0);
            }
            case CANCELADA -> order.transitionTo(OrderStatus.CANCELADA, T0);
        }
        return order;
    }
}
