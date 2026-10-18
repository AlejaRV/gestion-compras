# 🏆 Reto Técnico: Ingeniero/a de Software QA de Calidad TI (Semi Senior)
## API REST para Gestión de Órdenes de Compra con Quality Gates y Pruebas Automatizadas

[![CI/CD Pipeline](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![JaCoCo Coverage](https://img.shields.io/badge/coverage-100%25-success)](target/site/jacoco/index.html)
[![Mutation Score](https://img.shields.io/badge/mutation%20score-98%25-success)](target/pit-reports/index.html)
[![Karate Tests](https://img.shields.io/badge/karate%20tests-25%2F25%20passed-blue)](target/karate-reports/karate-summary.html)
[![Playwright Tests](https://img.shields.io/badge/playwright%20ui-4%2F4%20passed-blueviolet)](src/test/java/com/challenge/orders/e2e/playwright/OrdersWebPlaywrightIT.java)

---

## 📌 1. Descripción de la Solución y Arquitectura de Software

Esta solución implementa una **API REST de grado bancario** para la gestión del ciclo de vida de órdenes de compra, diseñada bajo los principios de **Clean Architecture**, **Domain-Driven Design (DDD)** y una rigurosa estrategia de calidad con **Quality Gates automáticos**.

### 🏛️ Arquitectura del Backend
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

## ⚙️ 2. Reglas de Negocio Implementadas

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

## 🛡️ 3. Arquitectura de Calidad y Pirámide de Pruebas

Nuestra estrategia concibe la calidad como una **responsabilidad transversal y continua**, aplicando una pirámide de pruebas balanceada donde cada nivel mitiga un riesgo específico:

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

### 🧩 Detalle por Nivel de Prueba:

1. **Pruebas Unitarias y de Integración Slice (Base de la Pirámide):**
   - **Framework:** JUnit 5, Mockito, AssertJ, `@WebMvcTest`.
   - **Propósito:** Validar el cálculo de totales (`BigDecimal`), validación de entradas y la máquina de estados 5x5 exhaustiva.
   - **Métrica:** 125 pruebas ejecutadas, **100% de cobertura en líneas y 100% en ramas**.

2. **Mutation Testing (Validación de la Eficacia de las Pruebas):**
   - **Herramienta:** PITest (`pitest-maven`).
   - **Propósito:** Introducir mutantes (cambios de operadores matemáticos, inversión de condicionales y retornos nulos) para verificar que las aserciones de los tests realmente fallen cuando hay un defecto.
   - **Métrica:** **98% Mutation Score** (58 de 59 mutantes eliminados).

3. **Pruebas de Aceptación de API (Capa Media):**
   - **Herramienta:** Karate Framework (`karate-junit5`).
   - **Propósito:** Pruebas de integración de caja negra que validan contratos HTTP, esquemas JSON (`#uuid`, `#number`), códigos de estado (201, 200, 400, 409) y trazabilidad con Correlation IDs.
   - **Métrica:** **25 escenarios Gherkin ejecutados y aprobados**.

4. **Pruebas End-to-End de UI (Cúspide de la Pirámide):**
   - **Herramientas:** 
     - **Playwright (Java):** Automatización headless moderna y rápida de los 4 flujos principales (Crear orden, Consultar por ID, Actualizar estado y Cancelar).
     - **Serenity BDD:** Implementación con el patrón Screenplay + Cucumber.
   - **Métrica:** **4 flujos de usuario 100% aprobados**.

---

### 🎯 Matriz de Resultados Obtenidos vs Mínimos Requeridos

| Métrica de Calidad | Mínimo Requerido | Resultado Obtenido | Estado |
| :--- | :---: | :---: | :---: |
| **Cobertura de Líneas (JaCoCo)** | $\ge 80\%$ | **100%** | ✅ Superado |
| **Cobertura de Ramas (JaCoCo)** | $\ge 80\%$ | **100%** | ✅ Superado |
| **Mutation Score (PITest)** | $\ge 70\%$ | **98% (58/59 matados)** | ✅ Superado |
| **Pruebas de Aceptación (Karate)** | 4 escenarios | **25 escenarios aprobados** | ✅ Superado |
| **Pruebas E2E de UI (Playwright)** | 4 flujos sugeridos | **4 flujos aprobados** | ✅ Superado |
| **Quality Gates en CI/CD** | Obligatorio | **100% automatizado en GitHub Actions** | ✅ Superado |

---

## 🚀 4. Guía de Ejecución Local

### Prerrequisitos
- **Java 17 o superior** (OpenJDK / Eclipse Temurin)
- **Apache Maven 3.8+**
- **Docker & Docker Compose** (opcional)

### 1. Clonar y Compilar
```bash
git clone https://github.com/AlejaRV/gestion-compras.git
cd gestion-compras
mvn clean compile
```

### 2. Ejecutar la Aplicación Localmente
```bash
mvn spring-boot:run
```
- **Aplicación Web:** [http://localhost:8080](http://localhost:8080)
- **Documentación Swagger / OpenAPI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Check Actuator:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🧪 5. Comandos de Ejecución de Pruebas

### A. Pruebas Unitarias y Cobertura JaCoCo (Quality Gate >= 80%)
```bash
mvn test -DskipITs
```
*Reporte HTML generado en:* `target/site/jacoco/index.html`

### B. Pruebas de Mutación con PITest (Quality Gate >= 70%)
```bash
mvn pitest:mutationCoverage -DskipITs
```
*Reporte HTML generado en:* `target/pit-reports/index.html`

### C. Pruebas de Aceptación con Karate Framework
```bash
mvn test -Dtest=OrdersAcceptanceIT
```
*Reporte HTML generado en:* `target/karate-reports/karate-summary.html`

### D. Pruebas E2E de UI con Playwright
```bash
mvn test -Dtest=OrdersWebPlaywrightIT
```

---

## 🐳 6. Ejecución con Docker

### Construir y Ejecutar el Contenedor
```bash
docker build -t challenge-api .
docker run -p 8080:8080 challenge-api
```

O usando **Docker Compose**:
```bash
docker compose up --build -d
```

---

## 🔄 7. Pipeline de Integración Continua (CI/CD)

El archivo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) implementa un pipeline automatizado con Quality Gates estrictos:
1. **Build & Compile:** Compilación limpia del proyecto en Java 17.
2. **Unit Tests & JaCoCo Check:** Falla si la cobertura en líneas o ramas es `< 80%`.
3. **PITest Mutation Coverage:** Falla si el Mutation Score es `< 70%`.
4. **Karate Acceptance Tests:** Ejecuta los 25 escenarios de integración de API.
5. **Playwright UI Tests:** Instala dependencias de Chromium y corre las pruebas E2E de interfaz.
6. **Docker Build:** Valida la construcción exitosa de la imagen del contenedor.
7. **Artifact Upload:** Publica los reportes HTML de JaCoCo, PITest y Karate para auditoría.
