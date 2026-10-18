# 🏆 Reto Técnico: Ingeniero/a de Software QA de Calidad TI (Semi Senior)
## API REST para Gestión de Órdenes de Compra con Quality Gates y Pruebas Automatizadas

[![CI/CD Pipeline](https://github.com/AlejaRV/gestion-compras/actions/workflows/ci.yml/badge.svg)](https://github.com/AlejaRV/gestion-compras/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![JaCoCo Coverage](https://img.shields.io/badge/coverage-100%25-success)](docs/evidence/jacoco-summary.html)
[![Mutation Score](https://img.shields.io/badge/mutation%20score-98%25-success)](docs/evidence/pitest-summary.html)
[![Karate Tests](https://img.shields.io/badge/karate%20tests-25%2F25%20passed-blue)](docs/evidence/karate-summary.html)
[![Playwright Tests](https://img.shields.io/badge/playwright%20ui-4%2F4%20passed-blueviolet)](src/test/java/com/challenge/orders/e2e/playwright/OrdersWebPlaywrightIT.java)

---

## 📌 1. Descripción de la Solución y Decisiones Técnicas

Esta solución implementa una **API REST de grado transaccional** para la gestión del ciclo de vida de órdenes de compra, diseñada bajo los principios de **Clean Architecture**, **Domain-Driven Design (DDD)** y una rigurosa estrategia de calidad con **Quality Gates automáticos**.

### 💡 Decisiones Técnicas Clave:
1. **Precisión Financiera con `BigDecimal`:** Se prohibió el uso de tipos primitivos de punto flotante (`float`, `double`) en el cálculo de subtotales y totales para evitar errores acumulativos de redondeo binario (`RN-03`).
2. **Máquina de Estados en el Dominio (`RN-06`):** Implementada dentro del enum `OrderStatus` como una tabla inmutable de transiciones permitidas. Los estados terminales (`ENTREGADA`, `CANCELADA`) no admiten ninguna transición posterior.
3. **Borrado Lógico y Auditoría (`RN-04`):** El endpoint `DELETE /orders/{id}` no elimina físicamente registros; cambia el estado a `CANCELADA` para preservar trazabilidad histórica y rechaza con `409 Conflict` si la orden ya fue entregada.
4. **Respuestas de Error Homogéneas (RFC 7807):** Todas las excepciones de negocio y de validación retornan `application/problem+json` con campos estructurados: `type`, `title`, `status`, `detail`, `code` (código de regla violada) y `correlationId`.
5. **Observabilidad Distribuida:** Filtro `CorrelationIdFilter` que propaga y registra el encabezado `X-Correlation-Id` en los logs del servidor (MDC).

---

## 🏛️ 2. Arquitectura de la Solución

```
src/
├── main/
│   ├── java/com/challenge/orders/
│   │   ├── domain/          # Entidades (Order, OrderItem, OrderStatus con Máquina de Estados)
│   │   │   └── exception/   # Jerarquía de DomainException con códigos de reglas de negocio
│   │   ├── repository/      # Puertos e Implementación InMemory concurrente y portable
│   │   ├── service/         # Casos de uso de negocio y orquestación
│   │   └── web/             # Adaptador REST (Controllers, DTOs y Problem Details RFC 7807)
│   └── resources/
│       ├── static/          # Frontend Web moderno para consumo E2E y pruebas manuales
│       └── application.yml  # Configuración, Swagger y Actuator Probes
```

---

## ⚙️ 3. Reglas de Negocio Implementadas

| Regla | Descripción | Manejo / Código HTTP |
| :--- | :--- | :--- |
| **RN-01** | La orden debe contener al menos un producto | `400 Bad Request` (`VALIDATION_ERROR`) |
| **RN-02** | Cantidad y valor unitario deben ser > 0 | `400 Bad Request` (`VALIDATION_ERROR`) |
| **RN-03** | Cálculo automático del total: $\sum(cantidad \times valorUnitario)$ | `201 Created` / `Order.calculateTotal()` |
| **RN-04** | Una orden entregada **no puede ser cancelada** | `409 Conflict` (`RN-04`) |
| **RN-05** | Una orden **no puede ser enviada si no ha sido pagada** | `400 Bad Request` (`RN-05`) |
| **RN-06** | Transiciones inválidas de estado son rechazadas | `409 Conflict` (`RN-06` con detalle exacto) |

### 🔄 Máquina de Estados Finitos
```
[ CREADA ] ──► [ PAGADA ] ──► [ ENVIADA ] ──► [ ENTREGADA (Terminal) ]
    │              │               │
    └──────────────┴───────────────┴────────► [ CANCELADA (Terminal) ]
```

---

## 🛡️ 4. Estrategia de Calidad y Pirámide de Pruebas

Nuestra estrategia concibe la calidad como una **responsabilidad transversal, medible y preventiva**, aplicando una pirámide de pruebas balanceada donde cada nivel mitiga un riesgo específico:

```
                  / \
                 /   \
                / E2E \         --> Playwright & Serenity BDD (UI Web completa)
               /───────\
              / Aceptac.\       --> Karate Framework (25 escenarios BDD / Contratos HTTP)
             /───────────\
            /  Mutación   \     --> PIT Mutation Testing (59 mutantes evaluados - 98% Score)
           /───────────────\
          /   Unitarias     \   --> JUnit 5 + Mockito + JaCoCo (125 pruebas - 100% Cobertura)
         /───────────────────\
```

### 🧠 Casos de Automatización de Nivel Senior Diseñados e Implementados

Como Ingeniero de Automatización Senior, más allá de los flujos elementales, se diseñaron pruebas para mitigar **riesgos de borde, integridad transaccional y resiliencia**:

| Categoría de Riesgo | Nivel de Prueba | Caso de Automatización Diseñado | Justificación de Riesgo |
| :--- | :--- | :--- | :--- |
| **Integridad Transaccional** | Karate (API) | Ciclo de vida completo encadenado (`CREADA ➔ PAGADA ➔ ENVIADA ➔ ENTREGADA`) | Valida que el total monetario no mute durante las transiciones de estado. |
| **Matriz Completa de Estados** | Unitarias | Prueba parametrizada de matriz 5x5 de transiciones posibles | Mitiga el riesgo de regresión o bypass accidental de estados terminales. |
| **Ataque de Payloads Nulos/Vacíos** | Karate & Unitarias | Inyección de listas vacías, campos `null`, espacios en blanco y cantidades en `0` o `-1` | Garantiza que ninguna entrada corrupta toque la capa de persistencia. |
| **Inyección de Tipos Inválidos** | Karate (API) | Peticiones con UUIDs no conformes (`/orders/no-es-uuid`) y estados inventados (`PERDIDA`) | Valida que el deserializador responda `400` estructurado en vez de fallar con `500`. |
| **Concurrencia en la UI** | Playwright (UI) | Diálogos de confirmación nativos del navegador (`window.confirm`) | Automatiza la interacción con ventanas emergentes asíncronas en cancelación. |
| **Trazabilidad Distribuida** | Karate & Controller | Propagación y verificación del header `X-Correlation-Id` | Permite rastreo extremo a extremo en logs ante auditorías. |

---

## 🎯 5. Evidencias de Ejecución y Métricas Obtenidas

| Métrica de Calidad | Mínimo Requerido | Resultado Obtenido | Estado |
| :--- | :---: | :---: | :---: |
| **Cobertura de Líneas (JaCoCo)** | $\ge 80\%$ | **100% (114/114 líneas mutadas)** | ✅ Superado |
| **Cobertura de Ramas (JaCoCo)** | $\ge 80\%$ | **100% (44/44 ramas evaluadas)** | ✅ Superado |
| **Mutation Score (PITest)** | $\ge 70\%$ | **98% (58 de 59 mutantes matados)** | ✅ Superado |
| **Pruebas de Aceptación (Karate)** | 4 escenarios | **25 escenarios aprobados (0 fallos)** | ✅ Superado |
| **Pruebas E2E de UI (Playwright)** | 4 flujos sugeridos | **4 flujos 100% aprobados** | ✅ Superado |
| **Quality Gates en CI/CD** | Obligatorio | **Automatizado y en verde en GitHub Actions** | ✅ Superado |

### 📸 Evidencias Disponibles en el Repositorio:
- **Reporte JaCoCo (Cobertura):** [docs/evidence/jacoco-summary.html](docs/evidence/jacoco-summary.html)
- **Reporte PITest (Mutation Testing):** [docs/evidence/pitest-summary.html](docs/evidence/pitest-summary.html)
- **Reporte Karate (Aceptación de API):** [docs/evidence/karate-summary.html](docs/evidence/karate-summary.html)
- **Logs del Pipeline CI/CD:** Visualizables en la pestaña [GitHub Actions del Repositorio](https://github.com/AlejaRV/gestion-compras/actions).

---

## 🚀 6. Guía de Ejecución Local

### Prerrequisitos
- **Java 17 o superior** (OpenJDK / Eclipse Temurin)
- **Apache Maven 3.8+**
- **Docker & Docker Compose** (opcional)

### 1. Clonar el Repositorio
```bash
git clone https://github.com/AlejaRV/gestion-compras.git
cd gestion-compras
```

### 2. Ejecutar la Aplicación
```bash
mvn spring-boot:run
```
- **Aplicación Web:** [http://localhost:8080](http://localhost:8080)
- **Swagger / OpenAPI UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Check Actuator:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🧪 7. Comandos de Ejecución de Pruebas

```bash
# A. Pruebas Unitarias + Quality Gate Cobertura JaCoCo (>= 80%)
mvn test -DskipITs

# B. Quality Gate Pruebas de Mutación con PITest (>= 70%)
mvn pitest:mutationCoverage -DskipITs

# C. Pruebas de Aceptación con Karate Framework (25 escenarios)
mvn test -Dtest=OrdersAcceptanceIT

# D. Pruebas E2E de UI con Playwright (4 flujos completos)
mvn test -Dtest=OrdersWebPlaywrightIT
```

---

## 🐳 8. Containerización con Docker

### Construir y Ejecutar el Contenedor
```bash
docker build -t challenge-api .
docker run -p 8080:8080 challenge-api
```

O utilizando **Docker Compose**:
```bash
docker compose up --build -d
```

---

## 🔄 9. Pipeline de CI/CD (GitHub Actions)

El archivo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) ejecuta el pipeline completo en cada `push` o `pull request` hacia `main`:
1. **Build & Compile:** Compilación en Java 17 Temurin con cache de dependencias.
2. **Quality Gate Cobertura:** Ejecuta JaCoCo y falla el build si `LINE` o `BRANCH` son menores al 80%.
3. **Quality Gate Mutación:** Ejecuta PITest con umbral de fallo estricto al 70%.
4. **Pruebas Karate:** Ejecuta la suite de aceptación levantando la app en puerto aleatorio.
5. **Pruebas Playwright:** Instala dependencias del navegador Chromium y ejecuta las pruebas de UI headless.
6. **Docker Build:** Valida la construcción de la imagen de producción.
7. **Artefactos:** Publica los reportes HTML de JaCoCo, PITest y Karate para descarga y auditoría.
