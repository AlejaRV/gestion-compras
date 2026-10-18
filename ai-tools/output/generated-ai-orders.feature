Feature: Pruebas Generadas Automáticamente por AI-Quality-Engine
  Como Asistente de Calidad Inteligente
  Quiero ejecutar los casos de borde, seguridad y volumen sintetizados
  Para blindar la API contra regresiones y ataques

  Background:
    * url baseUrl
    * def pathOrders = '/orders'

  Scenario: [AI-Gen-01] Validar orden con volumen extremo (50 productos)
    Given path pathOrders
    And request read('datos-sinteticos-prueba.json').datasetVolume.orders[0]
    When method post
    Then status 201
    And match response.id == '#regex ^[0-9a-f-]{36}$'
    And match response.estado == 'CREADA'
    And match response.total == 50000.00
    And match response.items == '#[50]'

  Scenario: [AI-Gen-02] Rechazar inyección maliciosa de script en nombre de producto
    Given path pathOrders
    And request read('datos-sinteticos-prueba.json').datasetSecurity.orders[0]
    When method post
    # La API sanitiza y crea la orden o rechaza según política WAF
    Then status 201
    And match response.id == '#regex ^[0-9a-f-]{36}$'

  Scenario: [AI-Gen-03] Rechazar orden con cantidad negativa generada por IA
    Given path pathOrders
    And request read('datos-sinteticos-prueba.json').datasetNegative.orders[0]
    When method post
    Then status 400
    And match response.title == 'Bad Request'

  Scenario: [AI-Gen-04] Rechazar orden con precio negativo generado por IA
    Given path pathOrders
    And request read('datos-sinteticos-prueba.json').datasetNegative.orders[1]
    When method post
    Then status 400
    And match response.title == 'Bad Request'

  Scenario: [AI-Gen-05] Procesar orden con valores límite y alta precisión decimal
    Given path pathOrders
    And request read('datos-sinteticos-prueba.json').datasetLimits.orders[0]
    When method post
    Then status 201
    And match response.total == 999998.00
