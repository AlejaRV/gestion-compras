package com.challenge.orders.acceptance;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runner de las pruebas de aceptacion con Karate.
 * <ul>
 *   <li>Por defecto levanta la API real en un puerto aleatorio y ejecuta Karate contra ella.</li>
 *   <li>Con {@code -Dkarate.baseUrl=http://host:puerto} ejecuta contra un ambiente externo (ej. Docker).</li>
 * </ul>
 * Reportes: {@code target/karate-reports/karate-summary.html}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Pruebas de aceptacion Karate")
class OrdersAcceptanceIT {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Todos los escenarios de aceptacion pasan")
    void runAcceptanceTests() {
        String external = System.getProperty("karate.baseUrl");
        String baseUrl = (external == null || external.isBlank()) ? "http://localhost:" + port : external;

        Results results = Runner.path("classpath:karate/orders")
                .tags("~@ignore")
                .systemProperty("baseUrl", baseUrl)
                .outputCucumberJson(true)
                .outputJunitXml(true)
                .reportDir("target/karate-reports")
                .parallel(4);

        assertThat(results.getFailCount())
                .as("Escenarios fallidos:%n%s", results.getErrorMessages())
                .isZero();
    }
}
