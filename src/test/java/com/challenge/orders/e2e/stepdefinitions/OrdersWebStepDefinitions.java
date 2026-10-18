package com.challenge.orders.e2e.stepdefinitions;

import com.challenge.orders.e2e.pages.OrdersPage;
import com.challenge.orders.e2e.tasks.CrearOrdenWeb;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.web.server.LocalServerPort;

public class OrdersWebStepDefinitions {

    @LocalServerPort
    private int port;

    private final Actor user = Actor.named("Analista de Compras");
    private WebDriver driver;

    @Before
    public void setup() {
        if (driver != null) {
            user.can(BrowseTheWeb.with(driver));
        }
    }

    @Dado("que el usuario navega a la página de gestión de órdenes")
    public void navegarAPagina() {
        String url = "http://localhost:" + (port > 0 ? port : 8080);
        user.attemptsTo(Open.url(url));
    }

    @Cuando("ingresa el cliente {string} y el producto {string} con cantidad {int} y valor {int}")
    public void ingresarDatos(String cliente, String prodId, int cant, int valor) {
        user.attemptsTo(CrearOrdenWeb.conDatos(cliente, prodId, cant, (double) valor));
    }

    @Cuando("confirma la creación de la orden")
    public void confirmarCreacion() {
        // Ejecutado en la tarea CrearOrdenWeb
    }

    @Entonces("el sistema muestra un mensaje de orden creada exitosamente")
    public void verificarAlerta() {
        user.attemptsTo(
                Ensure.that(OrdersPage.ALERT_BOX).isDisplayed()
        );
    }
}
