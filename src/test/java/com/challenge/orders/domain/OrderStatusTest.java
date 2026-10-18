package com.challenge.orders.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.challenge.orders.domain.OrderStatus.CANCELADA;
import static com.challenge.orders.domain.OrderStatus.CREADA;
import static com.challenge.orders.domain.OrderStatus.ENTREGADA;
import static com.challenge.orders.domain.OrderStatus.ENVIADA;
import static com.challenge.orders.domain.OrderStatus.PAGADA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderStatus - maquina de estados (RN-06)")
class OrderStatusTest {

    /** Matriz completa 5x5 de transiciones: fuente unica de verdad de RN-06. */
    static Stream<Arguments> transitionMatrix() {
        var expected = new java.util.EnumMap<OrderStatus, Set<OrderStatus>>(OrderStatus.class);
        expected.put(CREADA, EnumSet.of(PAGADA, CANCELADA));
        expected.put(PAGADA, EnumSet.of(ENVIADA, CANCELADA));
        expected.put(ENVIADA, EnumSet.of(ENTREGADA, CANCELADA));
        expected.put(ENTREGADA, EnumSet.noneOf(OrderStatus.class));
        expected.put(CANCELADA, EnumSet.noneOf(OrderStatus.class));

        return Stream.of(OrderStatus.values()).flatMap(from ->
                Stream.of(OrderStatus.values()).map(to ->
                        Arguments.of(from, to, expected.get(from).contains(to))));
    }

    @ParameterizedTest(name = "[RN-06] {0} -> {1} permitido={2}")
    @MethodSource("transitionMatrix")
    void shouldEvaluateEveryTransition(OrderStatus from, OrderStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "[RN-06] {0} no permite transicion a null")
    @EnumSource(OrderStatus.class)
    void shouldRejectNullTarget(OrderStatus from) {
        assertThat(from.canTransitionTo(null)).isFalse();
    }

    @Test
    @DisplayName("[RN-06] ENTREGADA y CANCELADA son estados terminales")
    void terminalStates() {
        assertThat(ENTREGADA.isTerminal()).isTrue();
        assertThat(CANCELADA.isTerminal()).isTrue();
        assertThat(CREADA.isTerminal()).isFalse();
        assertThat(PAGADA.isTerminal()).isFalse();
        assertThat(ENVIADA.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("[RN-06] Las transiciones permitidas expuestas son inmutables")
    void allowedTransitionsAreImmutable() {
        assertThat(CREADA.allowedTransitions()).containsExactlyInAnyOrder(PAGADA, CANCELADA);
        assertThatThrownBy(() -> CREADA.allowedTransitions().add(ENTREGADA))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
