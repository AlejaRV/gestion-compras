package com.challenge.orders.domain.exception;

/**
 * Datos de la orden invalidos (RN-01, RN-02 y validaciones de integridad). Se traduce a HTTP 400.
 */
public class InvalidOrderException extends DomainException {

    public InvalidOrderException(String ruleCode, String message) {
        super(ruleCode, message);
    }
}
