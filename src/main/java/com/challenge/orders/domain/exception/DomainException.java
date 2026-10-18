package com.challenge.orders.domain.exception;

/**
 * Excepcion base del dominio. Cada excepcion transporta el codigo de la regla de negocio
 * violada (ej. RN-04) para trazabilidad end-to-end: dominio -> respuesta HTTP -> pruebas.
 */
public abstract class DomainException extends RuntimeException {

    private final String ruleCode;

    protected DomainException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
