package com.challenge.orders.domain;

import com.challenge.orders.domain.exception.DeliveredOrderCancellationException;
import com.challenge.orders.domain.exception.InvalidOrderException;
import com.challenge.orders.domain.exception.InvalidStatusTransitionException;
import com.challenge.orders.domain.exception.OrderNotPaidException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.challenge.orders.domain.OrderFixtures.ORDER_ID;
import static com.challenge.orders.domain.OrderFixtures.T0;
import static com.challenge.orders.domain.OrderFixtures.T1;
import static com.challenge.orders.domain.OrderFixtures.defaultItems;
import static com.challenge.orders.domain.OrderFixtures.newOrder;
import static com.challenge.orders.domain.OrderFixtures.orderIn;
import static com.challenge.orders.domain.OrderStatus.CANCELADA;
import static com.challenge.orders.domain.OrderStatus.CREADA;
import static com.challenge.orders.domain.OrderStatus.ENTREGADA;
import static com.challenge.orders.domain.OrderStatus.ENVIADA;
import static com.challenge.orders.domain.OrderStatus.PAGADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order - agregado de orden de compra")
class OrderTest {

    @Nested
    @DisplayName("Creacion")
    class Creation {

        @Test
        @DisplayName("[RN-03] Crea la orden en estado CREADA y calcula el total automaticamente")
        void createsOrderWithCalculatedTotal() {
            Order order = newOrder();

            assertThat(order.getId()).isEqualTo(ORDER_ID);
            assertThat(order.getCliente()).isEqualTo("Juan Perez");
            assertThat(order.getEstado()).isEqualTo(CREADA);
            assertThat(order.getTotal()).isEqualByComparingTo("245000");
            assertThat(order.getProductos()).hasSize(2);
            assertThat(order.getFechaCreacion()).isEqualTo(T0);
            assertThat(order.getFechaActualizacion()).isEqualTo(T0);
        }

        @Test
        @DisplayName("[RN-03] El total es exacto con decimales (sin errores de punto flotante)")
        void totalIsExactWithDecimals() {
            List<OrderItem> items = List.of(
                    new OrderItem("A", 3, new BigDecimal("0.10")),
                    new OrderItem("B", 1, new BigDecimal("0.20")));

            Order order = Order.create(ORDER_ID, "Ana", items, T0);

            assertThat(order.getTotal()).isEqualByComparingTo("0.50");
        }

        @Test
        @DisplayName("[RN-03] Una orden de un producto tiene total igual a su subtotal")
        void singleItemTotal() {
            Order order = Order.create(ORDER_ID, "Ana",
                    List.of(new OrderItem("P001", 2, new BigDecimal("100000"))), T0);

            assertThat(order.getTotal()).isEqualByComparingTo("200000");
        }

        @Test
        @DisplayName("[RN-03] El total de una lista vacia es cero (caso base de la suma)")
        void totalOfEmptyListIsZero() {
            assertThat(Order.calculateTotal(List.of())).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("El nombre del cliente se normaliza (trim)")
        void trimsClientName() {
            Order order = Order.create(ORDER_ID, "  Juan Perez  ", defaultItems(), T0);

            assertThat(order.getCliente()).isEqualTo("Juan Perez");
        }

        @Test
        @DisplayName("La lista de productos es una copia defensiva inmutable")
        void productsAreDefensiveCopy() {
            List<OrderItem> items = new ArrayList<>(defaultItems());
            Order order = Order.create(ORDER_ID, "Juan", items, T0);

            items.clear();

            assertThat(order.getProductos()).hasSize(2);
            assertThatThrownBy(() -> order.getProductos().add(new OrderItem("X", 1, BigDecimal.ONE)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @ParameterizedTest(name = "[RN-01] Lista de productos {0} es rechazada")
        @NullAndEmptySource
        void orderMustHaveAtLeastOneProduct(List<OrderItem> productos) {
            assertThatThrownBy(() -> Order.create(ORDER_ID, "Juan", productos, T0))
                    .isInstanceOf(InvalidOrderException.class)
                    .hasMessage("La orden debe contener al menos un producto")
                    .extracting("ruleCode").isEqualTo("RN-01");
        }

        @ParameterizedTest(name = "Cliente ''{0}'' es rechazado")
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void clientIsMandatory(String cliente) {
            assertThatThrownBy(() -> Order.create(ORDER_ID, cliente, defaultItems(), T0))
                    .isInstanceOf(InvalidOrderException.class)
                    .hasMessage("El cliente es obligatorio")
                    .extracting("ruleCode").isEqualTo("VALIDATION_ERROR");
        }

        @Test
        @DisplayName("Id y fecha son obligatorios")
        void idAndDateAreMandatory() {
            assertThatThrownBy(() -> Order.create(null, "Juan", defaultItems(), T0))
                    .isInstanceOf(NullPointerException.class).hasMessage("id");
            assertThatThrownBy(() -> Order.create(ORDER_ID, "Juan", defaultItems(), null))
                    .isInstanceOf(NullPointerException.class).hasMessage("now");
        }
    }

    @Nested
    @DisplayName("Transiciones de estado")
    class Transitions {

        @Test
        @DisplayName("[RN-06] Camino feliz: CREADA -> PAGADA -> ENVIADA -> ENTREGADA")
        void happyPath() {
            Order order = newOrder();

            order.transitionTo(PAGADA, T1);
            assertThat(order.getEstado()).isEqualTo(PAGADA);
            order.transitionTo(ENVIADA, T1);
            assertThat(order.getEstado()).isEqualTo(ENVIADA);
            order.transitionTo(ENTREGADA, T1);
            assertThat(order.getEstado()).isEqualTo(ENTREGADA);
        }

        @Test
        @DisplayName("Una transicion exitosa actualiza la fecha de actualizacion pero no la de creacion")
        void successfulTransitionUpdatesTimestamp() {
            Order order = newOrder();

            order.transitionTo(PAGADA, T1);

            assertThat(order.getFechaActualizacion()).isEqualTo(T1);
            assertThat(order.getFechaCreacion()).isEqualTo(T0);
        }

        @ParameterizedTest(name = "[RN-04] Se puede cancelar una orden en estado {0}")
        @EnumSource(value = OrderStatus.class, names = {"CREADA", "PAGADA", "ENVIADA"})
        void canCancelNonDeliveredOrders(OrderStatus status) {
            Order order = orderIn(status);

            order.cancel(T1);

            assertThat(order.getEstado()).isEqualTo(CANCELADA);
            assertThat(order.getFechaActualizacion()).isEqualTo(T1);
        }

        @Test
        @DisplayName("[RN-04] Una orden entregada no puede ser cancelada")
        void deliveredOrderCannotBeCancelled() {
            Order order = orderIn(ENTREGADA);

            assertThatThrownBy(() -> order.cancel(T1))
                    .isInstanceOf(DeliveredOrderCancellationException.class)
                    .hasMessageContaining("ya fue entregada")
                    .extracting("ruleCode").isEqualTo("RN-04");
            assertThat(order.getEstado()).isEqualTo(ENTREGADA);
            assertThat(order.getFechaActualizacion()).isEqualTo(T0);
        }

        @Test
        @DisplayName("[RN-05] Una orden no pagada no puede ser enviada")
        void unpaidOrderCannotBeShipped() {
            Order order = newOrder();

            assertThatThrownBy(() -> order.transitionTo(ENVIADA, T1))
                    .isInstanceOf(OrderNotPaidException.class)
                    .hasMessageContaining("no ha sido pagada")
                    .extracting("ruleCode").isEqualTo("RN-05");
            assertThat(order.getEstado()).isEqualTo(CREADA);
            assertThat(order.getFechaActualizacion()).isEqualTo(T0);
        }

        static Stream<Arguments> invalidTransitions() {
            return Stream.of(
                    Arguments.of(CREADA, CREADA),
                    Arguments.of(CREADA, ENTREGADA),
                    Arguments.of(PAGADA, CREADA),
                    Arguments.of(PAGADA, PAGADA),
                    Arguments.of(PAGADA, ENTREGADA),
                    Arguments.of(ENVIADA, CREADA),
                    Arguments.of(ENVIADA, PAGADA),
                    Arguments.of(ENVIADA, ENVIADA),
                    Arguments.of(ENTREGADA, CREADA),
                    Arguments.of(ENTREGADA, PAGADA),
                    Arguments.of(ENTREGADA, ENVIADA),
                    Arguments.of(ENTREGADA, ENTREGADA),
                    Arguments.of(CANCELADA, CREADA),
                    Arguments.of(CANCELADA, PAGADA),
                    Arguments.of(CANCELADA, ENVIADA),
                    Arguments.of(CANCELADA, ENTREGADA),
                    Arguments.of(CANCELADA, CANCELADA));
        }

        @ParameterizedTest(name = "[RN-06] {0} -> {1} es rechazada")
        @MethodSource("invalidTransitions")
        void rejectsInvalidTransitions(OrderStatus from, OrderStatus to) {
            Order order = orderIn(from);

            assertThatThrownBy(() -> order.transitionTo(to, T1))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessageContaining(from + " -> " + to)
                    .satisfies(ex -> {
                        var e = (InvalidStatusTransitionException) ex;
                        assertThat(e.getRuleCode()).isEqualTo("RN-06");
                        assertThat(e.getFrom()).isEqualTo(from);
                        assertThat(e.getTo()).isEqualTo(to);
                    });
            assertThat(order.getEstado()).isEqualTo(from);
        }

        @Test
        @DisplayName("El estado destino es obligatorio")
        void targetIsMandatory() {
            Order order = newOrder();

            assertThatThrownBy(() -> order.transitionTo(null, T1))
                    .isInstanceOf(InvalidOrderException.class)
                    .hasMessage("El estado destino es obligatorio");
            assertThat(order.getEstado()).isEqualTo(CREADA);
        }
    }
}
