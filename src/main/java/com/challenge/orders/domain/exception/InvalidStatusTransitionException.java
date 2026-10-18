package com.challenge.orders.domain.exception;

import com.challenge.orders.domain.OrderStatus;

import java.util.UUID;

/**
 * RN-06: No se permiten transiciones invalidas de estado. Se traduce a HTTP 409.
 */
public class InvalidStatusTransitionException extends DomainException {

    public static final String RULE = "RN-06";

    private final OrderStatus from;
    private final OrderStatus to;

    public InvalidStatusTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super(RULE, "Transicion de estado invalida para la orden " + orderId + ": " + from + " -> " + to
                + ". Transiciones permitidas desde " + from + ": " + from.allowedTransitions());
        this.from = from;
        this.to = to;
    }

    public OrderStatus getFrom() {
        return from;
    }

    public OrderStatus getTo() {
        return to;
    }
}
