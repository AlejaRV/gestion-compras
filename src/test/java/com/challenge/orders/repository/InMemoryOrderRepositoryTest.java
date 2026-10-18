package com.challenge.orders.repository;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryOrderRepository")
class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    private static Order orderAt(String instant) {
        return Order.create(UUID.randomUUID(), "Cliente", OrderFixtures.defaultItems(), Instant.parse(instant));
    }

    @Test
    @DisplayName("Guarda y recupera una orden por id")
    void savesAndFindsById() {
        Order order = OrderFixtures.newOrder();

        Order saved = repository.save(order);

        assertThat(saved).isSameAs(order);
        assertThat(repository.findById(order.getId())).containsSame(order);
    }

    @Test
    @DisplayName("Devuelve vacio cuando la orden no existe")
    void returnsEmptyWhenNotFound() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Lista vacia cuando no hay ordenes")
    void emptyList() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Lista las ordenes de la mas reciente a la mas antigua")
    void listsNewestFirst() {
        Order oldest = orderAt("2026-01-01T00:00:00Z");
        Order newest = orderAt("2026-03-01T00:00:00Z");
        Order middle = orderAt("2026-02-01T00:00:00Z");
        repository.save(oldest);
        repository.save(newest);
        repository.save(middle);

        assertThat(repository.findAll()).containsExactly(newest, middle, oldest);
    }

    @Test
    @DisplayName("Guardar la misma orden dos veces no la duplica")
    void saveIsIdempotentById() {
        Order order = OrderFixtures.newOrder();
        repository.save(order);
        repository.save(order);

        assertThat(repository.findAll()).hasSize(1);
    }
}
