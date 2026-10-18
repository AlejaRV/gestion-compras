package com.challenge.orders.e2e.tasks;

import com.challenge.orders.e2e.pages.OrdersPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class CrearOrdenWeb implements Task {

    private final String cliente;
    private final String prodId;
    private final int cantidad;
    private final double valorUnitario;

    public CrearOrdenWeb(String cliente, String prodId, int cantidad, double valorUnitario) {
        this.cliente = cliente;
        this.prodId = prodId;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
    }

    public static CrearOrdenWeb conDatos(String cliente, String prodId, int cantidad, double valorUnitario) {
        return instrumented(CrearOrdenWeb.class, cliente, prodId, cantidad, valorUnitario);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Enter.theValue(cliente).into(OrdersPage.CLIENTE_INPUT),
                Enter.theValue(prodId).into(OrdersPage.PROD_ID_0),
                Enter.theValue(String.valueOf(cantidad)).into(OrdersPage.PROD_CANT_0),
                Enter.theValue(String.valueOf(valorUnitario)).into(OrdersPage.PROD_VALOR_0),
                Click.on(OrdersPage.BTN_CREAR_ORDEN)
        );
    }
}
