package com.challenge.orders.domain;

import com.challenge.orders.domain.exception.DeliveredOrderCancellationException;
import com.challenge.orders.domain.exception.InvalidOrderException;
import com.challenge.orders.domain.exception.InvalidStatusTransitionException;
import com.challenge.orders.domain.exception.OrderNotPaidException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz: orden de compra.
 * <p>
 * Encapsula todas las reglas de negocio (RN-01..RN-06) para que no puedan ser evadidas
 * desde capas superiores: una orden solo se puede construir valida y solo puede cambiar
 * de estado a traves de {@link #transitionTo(OrderStatus, Instant)}.
 */
public final class Order {

    private final UUID id;
    private final String cliente;
    private final List<OrderItem> productos;
    private final BigDecimal total;
    private final Instant fechaCreacion;
    private OrderStatus estado;
    private Instant fechaActualizacion;

    private Order(UUID id, String cliente, List<OrderItem> productos, Instant now) {
        this.id = id;
        this.cliente = cliente;
        this.productos = List.copyOf(productos);
        this.total = calculateTotal(this.productos);
        this.estado = OrderStatus.CREADA;
        this.fechaCreacion = now;
        this.fechaActualizacion = now;
    }

    /**
     * Crea una orden nueva en estado CREADA validando RN-01 y calculando el total (RN-03).
     */
    public static Order create(UUID id, String cliente, List<OrderItem> productos, Instant now) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(now, "now");
        if (cliente == null || cliente.isBlank()) {
            throw new InvalidOrderException("VALIDATION_ERROR", "El cliente es obligatorio");
        }
        if (productos == null || productos.isEmpty()) {
            throw new InvalidOrderException("RN-01", "La orden debe contener al menos un producto");
        }
        return new Order(id, cliente.trim(), productos, now);
    }

    /** RN-03: total = suma de (cantidad x valorUnitario). */
    static BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cambia el estado de la orden aplicando RN-04, RN-05 y RN-06.
     * Las reglas especificas se evaluan antes que la regla general para devolver
     * el error de negocio mas preciso posible.
     */
    public synchronized void transitionTo(OrderStatus target, Instant now) {
        if (target == null) {
            throw new InvalidOrderException("VALIDATION_ERROR", "El estado destino es obligatorio");
        }
        if (estado == OrderStatus.ENTREGADA && target == OrderStatus.CANCELADA) {
            throw new DeliveredOrderCancellationException(id);
        }
        if (estado == OrderStatus.CREADA && target == OrderStatus.ENVIADA) {
            throw new OrderNotPaidException(id);
        }
        if (!estado.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(id, estado, target);
        }
        this.estado = target;
        this.fechaActualizacion = now;
    }

    /** Cancela la orden (equivale a transicionar a CANCELADA). */
    public void cancel(Instant now) {
        transitionTo(OrderStatus.CANCELADA, now);
    }

    public UUID getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public List<OrderItem> getProductos() {
        return productos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public synchronized OrderStatus getEstado() {
        return estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public synchronized Instant getFechaActualizacion() {
        return fechaActualizacion;
    }
}
