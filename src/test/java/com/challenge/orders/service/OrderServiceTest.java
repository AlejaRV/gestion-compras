package com.challenge.orders.service;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderFixtures;
import com.challenge.orders.domain.OrderStatus;
import com.challenge.orders.domain.exception.DeliveredOrderCancellationException;
import com.challenge.orders.domain.exception.InvalidOrderException;
import com.challenge.orders.domain.exception.InvalidStatusTransitionException;
import com.challenge.orders.domain.exception.OrderNotFoundException;
import com.challenge.orders.domain.exception.OrderNotPaidException;
import com.challenge.orders.repository.InMemoryOrderRepository;
import com.challenge.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.challenge.orders.domain.OrderFixtures.ORDER_ID;
import static com.challenge.orders.domain.OrderFixtures.T0;
import static com.challenge.orders.domain.OrderFixtures.orderIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - casos de uso")
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    private OrderService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(T0, ZoneOffset.UTC);
        service = new OrderService(repository, fixedClock, () -> ORDER_ID);
    }

    @Test
    @DisplayName("[RN-03] Crear orden: genera id, fija fecha, calcula total y persiste")
    void createsAndPersistsOrder() {
        Order created = service.create("Juan Perez", OrderFixtures.defaultItems());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(created);
        assertThat(created.getId()).isEqualTo(ORDER_ID);
        assertThat(created.getFechaCreacion()).isEqualTo(T0);
        assertThat(created.getEstado()).isEqualTo(OrderStatus.CREADA);
        assertThat(created.getTotal()).isEqualByComparingTo("245000");
    }

    @Test
    @DisplayName("[RN-01] Crear orden sin productos no persiste nada")
    void createWithoutProductsDoesNotPersist() {
        assertThatThrownBy(() -> service.create("Juan", List.of()))
                .isInstanceOf(InvalidOrderException.class)
                .extracting("ruleCode").isEqualTo("RN-01");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Consultar orden existente")
    void findsExistingOrder() {
        Order order = OrderFixtures.newOrder();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThat(service.findById(ORDER_ID)).isSameAs(order);
    }

    @Test
    @DisplayName("Consultar orden inexistente lanza OrderNotFoundException")
    void findByIdNotFound() {
        UUID missing = UUID.randomUUID();
        when(repository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(missing))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("No existe una orden con id " + missing)
                .extracting("ruleCode").isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    @DisplayName("Listar sin filtro devuelve todas las ordenes")
    void listsAllWithoutFilter() {
        List<Order> all = List.of(orderIn(OrderStatus.CREADA), orderIn(OrderStatus.PAGADA));
        when(repository.findAll()).thenReturn(all);

        assertThat(service.findAll(null)).isEqualTo(all);
    }

    @Test
    @DisplayName("Listar con filtro devuelve solo las ordenes en ese estado")
    void listsFilteredByStatus() {
        Order created = orderIn(OrderStatus.CREADA);
        Order paid = orderIn(OrderStatus.PAGADA);
        when(repository.findAll()).thenReturn(List.of(created, paid));

        assertThat(service.findAll(OrderStatus.PAGADA)).containsExactly(paid);
        assertThat(service.findAll(OrderStatus.ENTREGADA)).isEmpty();
    }

    @Test
    @DisplayName("[RN-06] Actualizar estado valido persiste el cambio")
    void updatesStatus() {
        Order order = OrderFixtures.newOrder();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Order updated = service.updateStatus(ORDER_ID, OrderStatus.PAGADA);

        assertThat(updated.getEstado()).isEqualTo(OrderStatus.PAGADA);
        verify(repository).save(order);
    }

    @Test
    @DisplayName("[RN-05] Enviar una orden no pagada falla y no persiste")
    void shippingUnpaidOrderFails() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(OrderFixtures.newOrder()));

        assertThatThrownBy(() -> service.updateStatus(ORDER_ID, OrderStatus.ENVIADA))
                .isInstanceOf(OrderNotPaidException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("[RN-06] Transicion invalida falla y no persiste")
    void invalidTransitionFails() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderIn(OrderStatus.CANCELADA)));

        assertThatThrownBy(() -> service.updateStatus(ORDER_ID, OrderStatus.PAGADA))
                .isInstanceOf(InvalidStatusTransitionException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar estado de orden inexistente lanza 404")
    void updateStatusNotFound() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(ORDER_ID, OrderStatus.PAGADA))
                .isInstanceOf(OrderNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("[RN-04] Cancelar orden creada la deja en CANCELADA y persiste")
    void cancelsOrder() {
        Order order = OrderFixtures.newOrder();
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Order cancelled = service.cancel(ORDER_ID);

        assertThat(cancelled.getEstado()).isEqualTo(OrderStatus.CANCELADA);
        verify(repository).save(order);
    }

    @Test
    @DisplayName("[RN-04] Cancelar orden entregada falla y no persiste")
    void cancelDeliveredOrderFails() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.of(orderIn(OrderStatus.ENTREGADA)));

        assertThatThrownBy(() -> service.cancel(ORDER_ID))
                .isInstanceOf(DeliveredOrderCancellationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar orden inexistente lanza 404")
    void cancelNotFound() {
        when(repository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(ORDER_ID)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("El constructor de produccion genera ids unicos aleatorios")
    void productionConstructorGeneratesRandomIds() {
        OrderService realService = new OrderService(new InMemoryOrderRepository());

        Order a = realService.create("A", OrderFixtures.defaultItems());
        Order b = realService.create("B", OrderFixtures.defaultItems());

        assertThat(a.getId()).isNotNull().isNotEqualTo(b.getId());
        assertThat(realService.findAll(null)).hasSize(2);
    }
}
