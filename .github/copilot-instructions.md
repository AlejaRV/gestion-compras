# 🤖 GitHub Copilot - Instrucciones de Calidad y Generación de Pruebas (QA Lead)

Este proyecto implementa el sistema de **Gestión de Órdenes de Compra** con Arquitectura Limpia en Spring Boot 3.3.5, pruebas de mutación (PITest), aceptación (Karate) y UI (Playwright).

Cuando interactúes con este repositorio para generar código, pruebas o documentación, sigue estrictamente las siguientes directrices:

---

## 1. Reglas de Negocio a Garantizar (RN-01 a RN-06)

Toda prueba generada debe validar o respetar estas reglas:
- **RN-01:** La orden debe tener al menos un ítem (`items` no vacío).
- **RN-02:** Los ítems no pueden tener cantidad $\le 0$ ni precio unitario $\le 0$.
- **RN-03:** El total de la orden se calcula automáticamente: $\sum(\text{cantidad} \times \text{precioUnitario})$.
- **RN-04:** La máquina de estados es estricta:
  - `PENDIENTE` $\to$ `EN_PROCESO`
  - `EN_PROCESO` $\to$ `COMPLETADA`
  - Cualquier estado no terminal $\to$ `CANCELADA`
  - `COMPLETADA` y `CANCELADA` son estados terminales (inmutables).
- **RN-05:** Órdenes completadas o canceladas no pueden modificarse.
- **RN-06:** Validaciones de entrada (`null`, formatos inválidos) retornan HTTP 400 Bad Request bajo estándar RFC 7807 (`ProblemDetail`).

---

## 2. Generación Automática de Casos de Prueba

### Pruebas Unitarias (JUnit 5 + Mockito + AssertJ):
- Ubicación: `src/test/java/com/challenge/orders/`
- Patrón: **Arrange-Act-Assert** (AAA) o **Given-When-Then**.
- Convención de nombres: `metodoProbado_condicion_resultadoEsperado()`.
- Usar `@DisplayName` descriptivo en español.
- Mantener cobertura de ramas y mutación resistente a PITest: validar valores exactos, excepciones específicas y mensajes de error.

### Pruebas de Aceptación (Karate BDD):
- Ubicación: `src/test/resources/karate/orders/orders.feature`
- Sintaxis Gherkin estricta con palabras clave en inglés (`Scenario:`, `Given`, `When`, `Then`) y descripciones en español.
- Usar `fuzzy matchers` (`'#regex ^[0-9a-f-]{36}$'`, `'#number'`, `'#string'`).

### Pruebas E2E de UI (Playwright Java):
- Ubicación: `src/test/java/com/challenge/orders/web/OrdersWebPlaywrightIT.java`
- Localizadores recomendados: IDs únicos (`#createOrderBtn`, `#searchOrderIdInput`), roles accesibles (`getByRole`) o texto visible.

---

## 3. Generación de Datos de Prueba Sintéticos
Al generar fixtures o datos de prueba:
- **UUIDs válidos:** Formato RFC 4122 v4.
- **Límites numéricos:** Cantidades enteras ($1, 2, 9999$), precios decimales con 2 decimales (`BigDecimal`).
- **Casos de borde / Seguridad:** Probar cadenas con scripts (`<script>alert(1)</script>`), comillas, emojis, caracteres unicode y strings de 255+ caracteres.

---

## 4. Matriz de Trazabilidad y Análisis de Riesgo
Cada escenario nuevo debe tener anotada la regla de negocio a la que apunta (e.g. `// Trazabilidad: RN-04`). Si se detecta un flujo sin prueba de borde o concurrencia, señalar la recomendación de riesgo correspondiente.
