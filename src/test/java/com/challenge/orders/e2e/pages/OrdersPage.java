package com.challenge.orders.e2e.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class OrdersPage extends PageObject {

    public static final Target CLIENTE_INPUT = Target.the("campo nombre del cliente")
            .located(By.cssSelector("[data-testid='cliente-input']"));

    public static final Target PROD_ID_0 = Target.the("primer producto id")
            .located(By.cssSelector("[data-testid='prod-id-0']"));

    public static final Target PROD_CANT_0 = Target.the("primer producto cantidad")
            .located(By.cssSelector("[data-testid='prod-cant-0']"));

    public static final Target PROD_VALOR_0 = Target.the("primer producto valor unitario")
            .located(By.cssSelector("[data-testid='prod-valor-0']"));

    public static final Target BTN_CREAR_ORDEN = Target.the("botón crear orden")
            .located(By.cssSelector("[data-testid='btn-crear-orden']"));

    public static final Target CONSULTAR_ID_INPUT = Target.the("campo buscar por id")
            .located(By.cssSelector("[data-testid='consultar-id-input']"));

    public static final Target BTN_CONSULTAR = Target.the("botón buscar orden")
            .located(By.cssSelector("[data-testid='btn-consultar']"));

    public static final Target ORDER_DETAIL_CONTAINER = Target.the("contenedor detalle de orden")
            .located(By.cssSelector("[data-testid='order-detail']"));

    public static final Target DETAIL_ID = Target.the("id en detalle")
            .located(By.cssSelector("[data-testid='detail-id']"));

    public static final Target DETAIL_ESTADO = Target.the("estado en detalle")
            .located(By.cssSelector("[data-testid='detail-estado']"));

    public static final Target ALERT_BOX = Target.the("caja de alerta y notificaciones")
            .located(By.id("alert-box"));

    public static Target statusBadgeOf(String orderId) {
        return Target.the("badge de estado de la orden " + orderId)
                .located(By.cssSelector("[data-testid='status-badge-" + orderId + "']"));
    }
}
