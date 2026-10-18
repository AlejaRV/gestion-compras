package com.challenge.orders.domain.exception;

import java.util.UUID;

/**
 * RN-05: Una orden no puede ser enviada si no ha sido pagada. Se traduce a HTTP 400.
 */
public class OrderNotPaidException extends DomainException {

    public static final String RULE = "RN-05";

    public OrderNotPaidException(UUID orderId) {
        super(RULE, "La orden " + orderId + " no puede ser enviada porque no ha sido pagada");
    }
}
