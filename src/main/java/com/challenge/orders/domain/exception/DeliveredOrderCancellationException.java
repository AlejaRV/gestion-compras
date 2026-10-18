package com.challenge.orders.domain.exception;

import java.util.UUID;

/**
 * RN-04: Una orden entregada no puede ser cancelada. Se traduce a HTTP 409.
 */
public class DeliveredOrderCancellationException extends DomainException {

    public static final String RULE = "RN-04";

    public DeliveredOrderCancellationException(UUID orderId) {
        super(RULE, "La orden " + orderId + " ya fue entregada y no puede ser cancelada");
    }
}
