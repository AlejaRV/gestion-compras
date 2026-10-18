package com.challenge.orders.repository;

import com.challenge.orders.domain.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia. Permite reemplazar la implementacion en memoria por una BD real
 * sin impactar la logica de negocio.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findAll();
}
