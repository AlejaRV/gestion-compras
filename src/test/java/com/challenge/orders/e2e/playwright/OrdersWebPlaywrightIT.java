package com.challenge.orders.e2e.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Pruebas E2E de interfaz de usuario con Playwright.
 * Cubre los 4 flujos sugeridos en el reto:
 * 1. Crear orden
 * 2. Consultar orden
 * 3. Actualizar estado
 * 4. Cancelar orden
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas E2E de UI con Playwright")
public class OrdersWebPlaywrightIT {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;
    private String baseUrl;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // Ejecución headless para compatibilidad en entornos locales y pipelines CI/CD
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        baseUrl = "http://localhost:" + port;
        page.navigate(baseUrl);
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("Flujo 1: Crear una nueva orden desde el formulario web")
    void shouldCreateOrderFromWebUI() {
        // Diligenciar cliente
        page.locator("[data-testid='cliente-input']").fill("Laura Gomez");

        // Diligenciar producto
        page.locator("[data-testid='prod-id-0']").fill("PLAY-001");
        page.locator("[data-testid='prod-cant-0']").fill("3");
        page.locator("[data-testid='prod-valor-0']").fill("45000");

        // Enviar formulario
        page.locator("[data-testid='btn-crear-orden']").click();

        // Verificar mensaje de éxito
        assertThat(page.locator("#alert-box")).isVisible();
        assertThat(page.locator("#alert-box")).containsText("Orden creada con éxito");

        // Verificar que aparezca en el listado general
        assertThat(page.locator("#orders-tbody")).containsText("Laura Gomez");
        assertThat(page.locator("#orders-tbody")).containsText("PLAY-001");
    }

    @Test
    @DisplayName("Flujo 2: Consultar una orden existente por ID")
    void shouldSearchOrderById() {
        // Crear orden primero
        page.locator("[data-testid='cliente-input']").fill("Andres Mejia");
        page.locator("[data-testid='prod-id-0']").fill("PLAY-002");
        page.locator("[data-testid='prod-cant-0']").fill("1");
        page.locator("[data-testid='prod-valor-0']").fill("80000");
        page.locator("[data-testid='btn-crear-orden']").click();

        // Tomar el UUID que queda automáticamente precargado en el buscador
        String orderId = page.locator("[data-testid='consultar-id-input']").inputValue();

        // Hacer clic en buscar
        page.locator("[data-testid='btn-consultar']").click();

        // Validar que el detalle sea visible y contenga la información correcta
        assertThat(page.locator("[data-testid='order-detail']")).isVisible();
        assertThat(page.locator("[data-testid='detail-cliente']")).hasText("Andres Mejia");
        assertThat(page.locator("[data-testid='detail-estado']")).hasText("CREADA");
        assertThat(page.locator("[data-testid='detail-total']")).containsText("80.000");
    }

    @Test
    @DisplayName("Flujo 3: Actualizar el estado de la orden secuencialmente")
    void shouldUpdateOrderStatus() {
        // Crear orden
        page.locator("[data-testid='cliente-input']").fill("Sofia Castro");
        page.locator("[data-testid='prod-id-0']").fill("PLAY-003");
        page.locator("[data-testid='prod-cant-0']").fill("2");
        page.locator("[data-testid='prod-valor-0']").fill("30000");
        page.locator("[data-testid='btn-crear-orden']").click();

        // Localizar la fila creada y presionar botón 'Pagar'
        page.waitForSelector("#orders-tbody tr:has-text('Sofia Castro')");
        Page.WaitForResponseOptions waitForResponse = new Page.WaitForResponseOptions();
        
        page.locator("#orders-tbody tr:has-text('Sofia Castro') button:has-text('Pagar')").click();

        // Validar que el estado cambie a PAGADA
        assertThat(page.locator("#orders-tbody tr:has-text('Sofia Castro')")).containsText("PAGADA");
    }

    @Test
    @DisplayName("Flujo 4: Cancelar una orden creada")
    void shouldCancelOrder() {
        // Crear orden
        page.locator("[data-testid='cliente-input']").fill("Camilo Restrepo");
        page.locator("[data-testid='prod-id-0']").fill("PLAY-004");
        page.locator("[data-testid='prod-cant-0']").fill("1");
        page.locator("[data-testid='prod-valor-0']").fill("20000");
        page.locator("[data-testid='btn-crear-orden']").click();

        // Configurar diálogo de confirmación (accept confirm)
        page.onceDialog(dialog -> dialog.accept());

        // Presionar botón Cancelar
        page.locator("#orders-tbody tr:has-text('Camilo Restrepo') button:has-text('Cancelar')").click();

        // Validar que el estado cambie a CANCELADA
        assertThat(page.locator("#orders-tbody tr:has-text('Camilo Restrepo')")).containsText("CANCELADA");
    }
}
