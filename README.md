# 🏆 Reto Técnico: Ingeniero/a de Software QA de Calidad TI (Semi Senior)
## API REST para Gestión de Órdenes de Compra con Quality Gates y Pruebas Automatizadas

[![CI/CD Pipeline](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![JaCoCo Coverage](https://img.shields.io/badge/coverage-100%25-success)](target/site/jacoco/index.html)
[![Mutation Score](https://img.shields.io/badge/mutation%20score-98%25-success)](target/pit-reports/index.html)
[![Karate Tests](https://img.shields.io/badge/karate%20tests-25%2F25%20passed-blue)](target/karate-reports/karate-summary.html)

---

## 📌 1. Descripción de la Solución y Arquitectura

Esta solución implementa una **API REST de grado bancario** para la gestión del ciclo de vida de órdenes de compra, diseñada bajo los principios de **Clean Architecture**, **Domain-Driven Design (DDD)** y una rigurosa estrategia de aseguramiento de calidad con **Quality Gates automáticos**.

### 🏛️ Arquitectura en Capas
```
src/
├── main/
│   ├── java/com/challenge/orders/
│   │   ├── domain/          # Entidades (Order, OrderItem, OrderStatus con Máquina de Estados)
│   │   │   └── exception/   # Jerarquía de DomainException con códigos RN-01 a RN-06
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
| **RN-01** | La orden debe contener al menos un producto | `400 Bad Request` (`VALIDATION_ERROR` / `RN-01`) |
| **RN-02** | Cantidad y valor unitario deben ser > 0 | `400 Bad Request` (`VALIDATION_ERROR` / `RN-02`) |
| **RN-03** | Cálculo automático del total: $\sum(cantidad \times valorUnitario)$ | `201 Created` / `Order.calculateTotal()` |
| **RN-04** | Una orden entregada **no puede ser cancelada** | `409 Conflict` (`RN-04`) |
| **RN-05** | Una orden **no puede ser enviada si no ha sido pagada** | `400 Bad Request` (`RN-05`) |
| **RN-06** | Transiciones inválidas de estado son rechazadas | `409 Conflict` (`RN-06` con detalle exacto) |

### 🔄 Diagrama de la Máquina de Estados
```
[ CREADA ] ──► [ PAGADA ] ──► [ ENVIADA ] ──► [ ENTREGADA (Terminal) ]
    │              │               │
    └──────────────┴───────────────┴────────► [ CANCELADA (Terminal) ]
```

---

## 🛡️ 3. Estrategia de Calidad y Pirámide de Pruebas

Nuestra estrategia concibe la calidad como una responsabilidad transversal y medible:

```
          / \
         / E2E \       --> Playwright & Serenity BDD (Flujos completos Web UI)
        /───────\
       / API BDD \     --> Karate Framework (25 escenarios HTTP de contrato y aceptación)
      /───────────\
     / Mutation    \   --> PIT Mutation Testing (59 mutantes evaluados - 98% Score)
    /───────────────\
   / Unit & Slice    \ --> JUnit 5 + Mockito + JaCoCo (125 pruebas unitarias - 100% Cobertura)
  /───────────────────\
```

### 🎯 Resultados Obtenidos vs Mínimos Requeridos

| Métrica de Calidad | Mínimo Requerido | Resultado Obtenido | Estado |
| :--- | :---: | :---: | :---: |
| **Cobertura de Líneas (JaCoCo)** | $\ge 80\%$ | **100%** | ✅ Superado |
| **Cobertura de Ramas (JaCoCo)** | $\ge 80\%$ | **100%** | ✅ Superado |
| **Mutation Score (PITest)** | $\ge 70\%$ | **98% (58/59 matados)** | ✅ Superado |
| **Pruebas de Aceptación (Karate)** | 4 escenarios | **25 escenarios aprobados** | ✅ Superado |
| **Pruebas E2E de UI (Playwright)** | 4 flujos sugeridos | **4 flujos 100% aprobados** | ✅ Superado |
| **Quality Gates en CI/CD** | Obligatorio | **100% automatizado en GitHub Actions** | ✅ Superado |

---

## 🚀 4. Guía de Ejecución Local

### Prerrequisitos
- **Java 17 o superior**
- **Apache Maven 3.8+**
- **Docker & Docker Compose** (opcional)

### 1. Clonar y Construir
```bash
git clone <url-del-repositorio>
cd bancolombia
mvn clean install
```

### 2. Ejecutar la Aplicación Localmente
```bash
mvn spring-boot:run
```
- **Aplicación Web:** [http://localhost:8080](http://localhost:8080)
- **Documentación Swagger / OpenAPI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Check Actuator:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🧪 5. Ejecución de Pruebas y Reportes

### A. Pruebas Unitarias y Cobertura JaCoCo
```bash
mvn test -DskipITs
```
*Reporte HTML generado en:* `target/site/jacoco/index.html`

### B. Pruebas de Mutación con PITest
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

Verificar estado:
```bash
docker ps
curl http://localhost:8080/actuator/health
```

---

## 🔄 7. Pipeline de Integración Continua (CI/CD)

El archivo [`.github/workflows/ci.yml`](.github/workflows/ci.yml) implementa un pipeline completo que rompe el build si:
1. Cualquier prueba unitaria falla.
2. La cobertura de código es menor al 80% (`jacoco:check`).
3. El Mutation Score es menor al 70% (`pitest:mutationCoverage`).
4. Cualquier prueba de aceptación en Karate falla.
5. Falla la construcción de la imagen Docker.

---

## 🌟 8. Entregables Plus e Innovación
- **Frontend Interactivo:** Se incluye una interfaz gráfica moderna en `src/main/resources/static/index.html` con atributos `data-testid` para automatización E2E.
- **E2E Serenity BDD:** Estructura con Patrón Screenplay preparada en `src/test/java/com/challenge/orders/e2e/`.
- **Trazabilidad & Observabilidad:** Filtro `CorrelationIdFilter` que inyecta y propaga el encabezado `X-Correlation-Id` en logs y payloads de error RFC 7807.
