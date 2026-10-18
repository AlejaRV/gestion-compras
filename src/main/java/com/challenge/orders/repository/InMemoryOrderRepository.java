package com.challenge.orders.repository;

import com.challenge.orders.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacion en memoria, thread-safe. Elegida para que la solucion sea portable
 * (sin dependencias externas) y facil de ejecutar en Docker / CI.
 */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    /** Devuelve las ordenes ordenadas de la mas reciente a la mas antigua. */
    @Override
    public List<Order> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Order::getFechaCreacion).reversed())
                .toList();
    }
}
