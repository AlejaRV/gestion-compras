package com.challenge.orders.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados de una orden y su maquina de estados (RN-06).
 *
 * <pre>
 * CREADA ──► PAGADA ──► ENVIADA ──► ENTREGADA
 *    │          │          │
 *    └──────────┴──────────┴──► CANCELADA
 * </pre>
 *
 * ENTREGADA y CANCELADA son estados terminales.
 */
public enum OrderStatus {
    CREADA,
    PAGADA,
    ENVIADA,
    ENTREGADA,
    CANCELADA;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CREADA, Collections.unmodifiableSet(EnumSet.of(PAGADA, CANCELADA)));
        ALLOWED_TRANSITIONS.put(PAGADA, Collections.unmodifiableSet(EnumSet.of(ENVIADA, CANCELADA)));
        ALLOWED_TRANSITIONS.put(ENVIADA, Collections.unmodifiableSet(EnumSet.of(ENTREGADA, CANCELADA)));
        ALLOWED_TRANSITIONS.put(ENTREGADA, Collections.unmodifiableSet(EnumSet.noneOf(OrderStatus.class)));
        ALLOWED_TRANSITIONS.put(CANCELADA, Collections.unmodifiableSet(EnumSet.noneOf(OrderStatus.class)));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return target != null && ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    public Set<OrderStatus> allowedTransitions() {
        return ALLOWED_TRANSITIONS.get(this);
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.get(this).isEmpty();
    }
}
