package com.challenge.orders.domain;

import com.challenge.orders.domain.exception.InvalidOrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem - validaciones de producto (RN-02)")
class OrderItemTest {

    @Test
    @DisplayName("[RN-03] El subtotal es cantidad x valorUnitario")
    void subtotalIsQuantityTimesUnitPrice() {
        OrderItem item = new OrderItem("P001", 3, new BigDecimal("15000.50"));

        assertThat(item.subtotal()).isEqualByComparingTo("45001.50");
    }

    @Test
    @DisplayName("[RN-02] Cantidad = 1 (limite inferior valido) es aceptada")
    void quantityOneIsValid() {
        OrderItem item = new OrderItem("P001", 1, BigDecimal.TEN);

        assertThat(item.cantidad()).isEqualTo(1);
        assertThat(item.subtotal()).isEqualByComparingTo("10");
    }

    @ParameterizedTest(name = "[RN-02] Cantidad {0} es rechazada")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void quantityMustBeGreaterThanZero(int cantidad) {
        assertThatThrownBy(() -> new OrderItem("P001", cantidad, BigDecimal.TEN))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("La cantidad del producto P001 debe ser mayor que cero")
                .extracting("ruleCode").isEqualTo("RN-02");
    }

    @Test
    @DisplayName("[RN-02] Valor unitario minimo positivo (0.01) es aceptado")
    void smallestPositiveUnitPriceIsValid() {
        OrderItem item = new OrderItem("P001", 2, new BigDecimal("0.01"));

        assertThat(item.subtotal()).isEqualByComparingTo("0.02");
    }

    @ParameterizedTest(name = "[RN-02] Valor unitario {0} es rechazado")
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
    void unitPriceMustBePositive(String valor) {
        assertThatThrownBy(() -> new OrderItem("P001", 1, new BigDecimal(valor)))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("El valor unitario del producto P001 debe ser mayor que cero")
                .extracting("ruleCode").isEqualTo("RN-02");
    }

    @Test
    @DisplayName("[RN-02] Valor unitario nulo es rechazado")
    void unitPriceCannotBeNull() {
        assertThatThrownBy(() -> new OrderItem("P001", 1, null))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("valor unitario");
    }

    @ParameterizedTest(name = "[RN-02] Id de producto ''{0}'' es rechazado")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void productIdIsMandatory(String id) {
        assertThatThrownBy(() -> new OrderItem(id, 1, BigDecimal.ONE))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("El id del producto es obligatorio")
                .extracting("ruleCode").isEqualTo("RN-02");
    }
}
