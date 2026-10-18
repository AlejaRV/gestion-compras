package com.challenge.orders.web;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderItem;
import com.challenge.orders.domain.OrderStatus;
import com.challenge.orders.service.OrderService;
import com.challenge.orders.web.dto.CreateOrderRequest;
import com.challenge.orders.web.dto.OrderResponse;
import com.challenge.orders.web.dto.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Ordenes", description = "Gestion de ordenes de compra")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear orden", description = "Crea una orden en estado CREADA y calcula el total (RN-01, RN-02, RN-03)")
    @ApiResponse(responseCode = "201", description = "Orden creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos (RN-01 / RN-02)")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderItem> items = request.productos().stream()
                .map(p -> new OrderItem(p.id(), p.cantidad(), p.valorUnitario()))
                .toList();
        Order order = service.create(request.cliente(), items);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar orden por id")
    @ApiResponse(responseCode = "200", description = "Orden encontrada")
    @ApiResponse(responseCode = "404", description = "Orden no existe")
    public OrderResponse findById(@PathVariable UUID id) {
        return OrderResponse.from(service.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar ordenes", description = "Lista todas las ordenes, opcionalmente filtradas por estado")
    public List<OrderResponse> findAll(
            @Parameter(description = "Filtro opcional por estado")
            @RequestParam(required = false) OrderStatus estado) {
        return service.findAll(estado).stream().map(OrderResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado", description = "Aplica la maquina de estados (RN-04, RN-05, RN-06)")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Orden no pagada no puede enviarse (RN-05) o payload invalido")
    @ApiResponse(responseCode = "404", description = "Orden no existe")
    @ApiResponse(responseCode = "409", description = "Transicion invalida (RN-04 / RN-06)")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return OrderResponse.from(service.updateStatus(id, request.estado()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar orden", description = "Cancelacion logica: la orden pasa a CANCELADA (RN-04)")
    @ApiResponse(responseCode = "200", description = "Orden cancelada")
    @ApiResponse(responseCode = "404", description = "Orden no existe")
    @ApiResponse(responseCode = "409", description = "Orden entregada o ya cancelada")
    public OrderResponse cancel(@PathVariable UUID id) {
        return OrderResponse.from(service.cancel(id));
    }
}
