---
name: qa-quality-assistant
description: Asistente autónomo de Calidad y Pruebas para Gestión de Órdenes. Permite generar automáticamente casos de prueba, matrices de trazabilidad, datos sintéticos y análisis de riesgo con IA.
---

# QA Quality Assistant Skill

Este skill provee flujos asistidos por Inteligencia Artificial para el ciclo de vida de Calidad de Software (QA):

## Capacidades

1. **Generación Automática de Casos de Prueba:**
   - Diseña casos de prueba unitarios (JUnit 5 / Mockito), de aceptación (Karate BDD) y E2E (Playwright) basados en las reglas `RN-01` a `RN-06`.
2. **Generación de Matrices de Trazabilidad:**
   - Mapea requerimientos de negocio con casos de prueba, mutantes de PITest y suites automatizadas.
3. **Generación de Datos de Prueba Sintéticos:**
   - Produce datasets de borde, estrés, volumen y sanitización de entradas maliciosas.
4. **Análisis de Brechas y Recomendaciones Basadas en Riesgo:**
   - Evalúa escenarios faltantes (concurrencia, idempotencia, resiliencia) y asigna niveles de severidad e impacto.

## Ejecución del Motor

Para ejecutar el motor de calidad asistido por IA:

```bash
# Ejecutar todas las tareas de QA
node ai-tools/ai-quality-engine.js

# O generar específicamente la matriz de trazabilidad:
node ai-tools/ai-quality-engine.js --action traceability

# Generar datos sintéticos:
node ai-tools/ai-quality-engine.js --action data

# Análisis de riesgos y brechas:
node ai-tools/ai-quality-engine.js --action risk
```

## Salidas Generadas

Los reportes y artefactos generados se guardan en:
- `ai-tools/output/matriz-trazabilidad.json`
- `ai-tools/output/datos-sinteticos-prueba.json`
- `ai-tools/output/recomendaciones-riesgo-ia.json`
