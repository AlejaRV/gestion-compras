package com.challenge.orders.web;

import com.challenge.orders.domain.Order;
import com.challenge.orders.domain.OrderFixtures;
import com.challenge.orders.domain.OrderStatus;
import com.challenge.orders.domain.exception.DeliveredOrderCancellationException;
import com.challenge.orders.domain.exception.InvalidStatusTransitionException;
import com.challenge.orders.domain.exception.OrderNotFoundException;
import com.challenge.orders.domain.exception.OrderNotPaidException;
import com.challenge.orders.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.challenge.orders.domain.OrderFixtures.ORDER_ID;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController - contrato HTTP")
class OrderControllerTest {

    private static final String VALID_ORDER = """
            {
              "cliente": "Juan Perez",
              "productos": [
                { "id": "P001", "cantidad": 2, "valorUnitario": 100000 },
                { "id": "P002", "cantidad": 3, "valorUnitario": 15000 }
              ]
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService service;

    @Nested
    @DisplayName("POST /orders")
    class Create {

        @Test
        @DisplayName("[RN-03] 201 con Location y total calculado")
        void createsOrder() throws Exception {
            when(service.create(eq("Juan Perez"), anyList())).thenReturn(OrderFixtures.newOrder());

            mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_ORDER))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/orders/" + ORDER_ID)))
                    .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.cliente").value("Juan Perez"))
                    .andExpect(jsonPath("$.estado").value("CREADA"))
                    .andExpect(jsonPath("$.total").value(245000))
                    .andExpect(jsonPath("$.productos", hasSize(2)))
                    .andExpect(jsonPath("$.productos[0].subtotal").value(200000))
                    .andExpect(jsonPath("$.fechaCreacion").value("2026-01-01T10:00:00Z"));
        }

        @Test
        @DisplayName("[RN-01] 400 cuando no hay productos")
        void rejectsEmptyProducts() throws Exception {
            mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"cliente\":\"Juan\",\"productos\":[]}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("productos"))
                    .andExpect(jsonPath("$.errors[0].message")
                            .value("RN-01: La orden debe contener al menos un producto"))
                    .andExpect(jsonPath("$.instance").value("/orders"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
            verify(service, never()).create(any(), any());
        }

        @Test
        @DisplayName("[RN-02] 400 cuando la cantidad es cero")
        void rejectsZeroQuantity() throws Exception {
            mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("""
                            {"cliente":"Juan","productos":[{"id":"P001","cantidad":0,"valorUnitario":10}]}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("productos[0].cantidad"))
                    .andExpect(jsonPath("$.errors[0].message").value("RN-02: La cantidad debe ser mayor que cero"));
        }

        @Test
        @DisplayName("400 cuando el JSON esta mal formado")
        void rejectsMalformedJson() throws Exception {
            mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        }

        @Test
        @DisplayName("415 cuando el content-type no es JSON")
        void rejectsUnsupportedMediaType() throws Exception {
            mvc.perform(post("/orders").contentType(MediaType.TEXT_PLAIN).content("hola"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.code").value("HTTP_415"));
        }
    }

    @Nested
    @DisplayName("GET /orders")
    class Read {

        @Test
        @DisplayName("200 al consultar una orden existente")
        void getsOrder() throws Exception {
            when(service.findById(ORDER_ID)).thenReturn(OrderFixtures.newOrder());

            mvc.perform(get("/orders/{id}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.total").value(245000));
        }

        @Test
        @DisplayName("404 al consultar una orden inexistente")
        void getsNotFound() throws Exception {
            when(service.findById(ORDER_ID)).thenThrow(new OrderNotFoundException(ORDER_ID));

            mvc.perform(get("/orders/{id}", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                    .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
        }

        @Test
        @DisplayName("400 cuando el id no es un UUID")
        void rejectsInvalidUuid() throws Exception {
            mvc.perform(get("/orders/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                    .andExpect(jsonPath("$.detail").value("El parametro 'id' tiene un valor invalido: abc"));
        }

        @Test
        @DisplayName("200 al listar todas las ordenes")
        void listsOrders() throws Exception {
            when(service.findAll(null)).thenReturn(List.of(OrderFixtures.newOrder()));

            mvc.perform(get("/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("200 al listar filtrando por estado")
        void listsOrdersByStatus() throws Exception {
            when(service.findAll(OrderStatus.PAGADA)).thenReturn(List.of());

            mvc.perform(get("/orders").param("estado", "PAGADA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
            verify(service).findAll(OrderStatus.PAGADA);
        }

        @Test
        @DisplayName("400 al filtrar por un estado inexistente")
        void rejectsUnknownStatusFilter() throws Exception {
            mvc.perform(get("/orders").param("estado", "PERDIDA"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("200 al aplicar una transicion valida")
        void updatesStatus() throws Exception {
            Order paid = OrderFixtures.orderIn(OrderStatus.PAGADA);
            when(service.updateStatus(ORDER_ID, OrderStatus.PAGADA)).thenReturn(paid);

            mvc.perform(patch("/orders/{id}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"PAGADA\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("PAGADA"));
        }

        @Test
        @DisplayName("[RN-05] 400 al enviar una orden no pagada")
        void rejectsShippingUnpaid() throws Exception {
            when(service.updateStatus(ORDER_ID, OrderStatus.ENVIADA)).thenThrow(new OrderNotPaidException(ORDER_ID));

            mvc.perform(patch("/orders/{id}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENVIADA\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("RN-05"))
                    .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
        }

        @Test
        @DisplayName("[RN-06] 409 ante una transicion invalida")
        void rejectsInvalidTransition() throws Exception {
            when(service.updateStatus(ORDER_ID, OrderStatus.CREADA)).thenThrow(
                    new InvalidStatusTransitionException(ORDER_ID, OrderStatus.PAGADA, OrderStatus.CREADA));

            mvc.perform(patch("/orders/{id}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CREADA\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("RN-06"))
                    .andExpect(jsonPath("$.title").value("Conflicto de estado"));
        }

        @Test
        @DisplayName("400 cuando el estado no existe")
        void rejectsUnknownStatus() throws Exception {
            mvc.perform(patch("/orders/{id}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"PERDIDA\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        }

        @Test
        @DisplayName("400 cuando el estado es nulo")
        void rejectsMissingStatus() throws Exception {
            mvc.perform(patch("/orders/{id}/status", ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("estado"));
        }
    }

    @Nested
    @DisplayName("DELETE /orders/{id}")
    class Cancel {

        @Test
        @DisplayName("[RN-04] 200 al cancelar una orden no entregada")
        void cancelsOrder() throws Exception {
            when(service.cancel(ORDER_ID)).thenReturn(OrderFixtures.orderIn(OrderStatus.CANCELADA));

            mvc.perform(delete("/orders/{id}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADA"));
        }

        @Test
        @DisplayName("[RN-04] 409 al cancelar una orden entregada")
        void rejectsCancellingDelivered() throws Exception {
            when(service.cancel(ORDER_ID)).thenThrow(new DeliveredOrderCancellationException(ORDER_ID));

            mvc.perform(delete("/orders/{id}", ORDER_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("RN-04"));
        }
    }

    @Nested
    @DisplayName("Transversal: errores y observabilidad")
    class CrossCutting {

        @Test
        @DisplayName("500 con mensaje generico ante errores inesperados")
        void unexpectedError() throws Exception {
            when(service.findById(any(UUID.class))).thenThrow(new IllegalStateException("boom"));

            mvc.perform(get("/orders/{id}", ORDER_ID))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.detail").value(
                            "Ocurrio un error inesperado. Use el correlationId para rastrearlo."));
        }

        @Test
        @DisplayName("405 cuando el metodo no esta soportado")
        void methodNotAllowed() throws Exception {
            mvc.perform(put("/orders"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.code").value("HTTP_405"));
        }

        @Test
        @DisplayName("404 para rutas inexistentes (no se convierte en 500)")
        void unknownRoute() throws Exception {
            mvc.perform(get("/no-existe"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("HTTP_404"));
        }

        @Test
        @DisplayName("Propaga el X-Correlation-Id recibido en header y en el cuerpo de error")
        void propagatesCorrelationId() throws Exception {
            when(service.findById(ORDER_ID)).thenThrow(new OrderNotFoundException(ORDER_ID));

            mvc.perform(get("/orders/{id}", ORDER_ID).header(CorrelationIdFilter.HEADER, "test-cid-123"))
                    .andExpect(header().string(CorrelationIdFilter.HEADER, "test-cid-123"))
                    .andExpect(jsonPath("$.correlationId").value("test-cid-123"));
        }

        @Test
        @DisplayName("Genera un X-Correlation-Id cuando no se envia o esta vacio")
        void generatesCorrelationId() throws Exception {
            when(service.findAll(null)).thenReturn(List.of());

            mvc.perform(get("/orders"))
                    .andExpect(header().string(CorrelationIdFilter.HEADER, notNullValue()));
            mvc.perform(get("/orders").header(CorrelationIdFilter.HEADER, " "))
                    .andExpect(header().string(CorrelationIdFilter.HEADER,
                            org.hamcrest.Matchers.matchesPattern("[0-9a-f\\-]{36}")));
        }
    }
}
