package com.challenge.orders.domain.exception;

import java.util.UUID;

/**
 * La orden solicitada no existe. Se traduce a HTTP 404.
 */
public class OrderNotFoundException extends DomainException {

    public static final String CODE = "ORDER_NOT_FOUND";

    public OrderNotFoundException(UUID orderId) {
        super(CODE, "No existe una orden con id " + orderId);
    }
}
