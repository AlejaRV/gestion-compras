package com.challenge.orders.service;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderItem;
import com.challenge.orders.domain.OrderStatus;
import com.challenge.orders.domain.exception.OrderNotFoundException;
import com.challenge.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Casos de uso de ordenes de compra. Orquesta repositorio y dominio;
 * las reglas de negocio viven en {@link Order}.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final Clock clock;
    private final Supplier<UUID> idGenerator;

    @Autowired
    public OrderService(OrderRepository repository) {
        this(repository, Clock.systemUTC(), UUID::randomUUID);
    }

    /** Constructor para pruebas: permite controlar el reloj y los ids generados. */
    public OrderService(OrderRepository repository, Clock clock, Supplier<UUID> idGenerator) {
        this.repository = repository;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public Order create(String cliente, List<OrderItem> productos) {
        Order order = Order.create(idGenerator.get(), cliente, productos, clock.instant());
        repository.save(order);
        log.info("event=order_created orderId={} cliente=\"{}\" items={} total={}",
                order.getId(), order.getCliente(), order.getProductos().size(), order.getTotal());
        return order;
    }

    public Order findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    /** Lista todas las ordenes; si {@code estado} no es null, filtra por ese estado. */
    public List<Order> findAll(OrderStatus estado) {
        List<Order> all = repository.findAll();
        if (estado == null) {
            return all;
        }
        return all.stream().filter(o -> o.getEstado() == estado).toList();
    }

    public Order updateStatus(UUID id, OrderStatus target) {
        Order order = findById(id);
        OrderStatus previous = order.getEstado();
        order.transitionTo(target, clock.instant());
        repository.save(order);
        log.info("event=order_status_changed orderId={} from={} to={}", id, previous, target);
        return order;
    }

    public Order cancel(UUID id) {
        Order order = findById(id);
        OrderStatus previous = order.getEstado();
        order.cancel(clock.instant());
        repository.save(order);
        log.info("event=order_cancelled orderId={} previousStatus={}", id, previous);
        return order;
    }
}
