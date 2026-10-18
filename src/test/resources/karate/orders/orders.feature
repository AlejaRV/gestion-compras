@aceptacion
Feature: Gestión de órdenes de compra - Pruebas de aceptación de la API

  Como cliente de la API de órdenes de compra
  Quiero crear, consultar y gestionar el ciclo de vida de las órdenes
  Para garantizar extremo a extremo (HTTP) el cumplimiento de las reglas RN-01..RN-06

  Background:
    * url baseUrl
    * def validOrder = read('classpath:karate/data/valid-order.json')
    * def orderSchema = read('classpath:karate/schemas/order-schema.json')
    * def itemSchema = read('classpath:karate/schemas/item-schema.json')
    * def errorSchema = read('classpath:karate/schemas/error-schema.json')
    * def orderInStatus = read('classpath:karate/common/order-in-status.feature')
    * def calcTotal =
      """
      function(items) {
        var total = 0;
        for (var i = 0; i < items.length; i++) { total += items[i].cantidad * items[i].valorUnitario; }
        return total;
      }
      """

  # =====================================================================
  # Escenarios OBLIGATORIOS del reto
  # =====================================================================

  @obligatorio @smoke @RN-03
  Scenario: Crear una orden válida responde 201
    Given path 'orders'
    And request validOrder
    When method post
    Then status 201
    And match response == orderSchema
    And match each response.productos == itemSchema
    And match response.estado == 'CREADA'
    And match response.cliente == validOrder.cliente
    # RN-03: el total lo calcula la API = sum(cantidad x valorUnitario) = 245000
    * def expectedTotal = calcTotal(validOrder.productos)
    And match response.total == expectedTotal
    And match response.total == 245000
    * def location = karate.response.header('Location')
    And match location contains '/orders/' + response.id

  @obligatorio @smoke
  Scenario: Consultar una orden existente responde 200
    * def created = call orderInStatus { targetStatus: 'CREADA' }
    Given path 'orders', created.orderId
    When method get
    Then status 200
    And match response == orderSchema
    And match response.id == created.orderId
    And match response.total == 245000

  @obligatorio @RN-05
  Scenario: Intentar enviar una orden no pagada responde 400
    * def created = call orderInStatus { targetStatus: 'CREADA' }
    Given path 'orders', created.orderId, 'status'
    And request { estado: 'ENVIADA' }
    When method patch
    Then status 400
    And match response == errorSchema
    And match response.code == 'RN-05'
    And match response.detail contains 'no ha sido pagada'
    # La orden no debe haber cambiado de estado
    Given path 'orders', created.orderId
    When method get
    Then match response.estado == 'CREADA'

  @obligatorio @RN-04
  Scenario: Intentar cancelar una orden entregada responde 409
    * def delivered = call orderInStatus { targetStatus: 'ENTREGADA' }
    Given path 'orders', delivered.orderId
    When method delete
    Then status 409
    And match response == errorSchema
    And match response.code == 'RN-04'
    And match response.detail contains 'ya fue entregada'
    # La orden debe seguir entregada
    Given path 'orders', delivered.orderId
    When method get
    Then match response.estado == 'ENTREGADA'

  # =====================================================================
  # Escenarios ADICIONALES (justificación basada en riesgo en el README)
  # =====================================================================

  # Riesgo: flujo principal de negocio roto entre transiciones encadenadas.
  @adicional @RN-06 @smoke
  Scenario: Ciclo de vida completo CREADA -> PAGADA -> ENVIADA -> ENTREGADA
    * def created = call orderInStatus { targetStatus: 'CREADA' }
    * def id = created.orderId
    Given path 'orders', id, 'status'
    And request { estado: 'PAGADA' }
    When method patch
    Then status 200
    And match response.estado == 'PAGADA'
    Given path 'orders', id, 'status'
    And request { estado: 'ENVIADA' }
    When method patch
    Then status 200
    And match response.estado == 'ENVIADA'
    Given path 'orders', id, 'status'
    And request { estado: 'ENTREGADA' }
    When method patch
    Then status 200
    And match response.estado == 'ENTREGADA'
    # Integridad financiera: el total no se altera durante el ciclo de vida
    And match response.total == 245000

  # Riesgo: datos inválidos persistidos -> órdenes inconsistentes / totales erróneos.
  @adicional @RN-01 @RN-02
  Scenario Outline: Rechazar orden inválida (<caso>) con 400
    Given path 'orders'
    And request <payload>
    When method post
    Then status 400
    And match response == errorSchema
    And match response.code == 'VALIDATION_ERROR'
    And match response.errors[0].message contains '<mensaje>'

    Examples:
      | caso                | payload                                                                        | mensaje                   |
      | sin productos       | { cliente: 'Ana', productos: [] }                                              | RN-01                     |
      | productos nulo      | { cliente: 'Ana' }                                                             | RN-01                     |
      | cantidad cero       | { cliente: 'Ana', productos: [{ id: 'P1', cantidad: 0, valorUnitario: 10 }] }  | RN-02                     |
      | cantidad negativa   | { cliente: 'Ana', productos: [{ id: 'P1', cantidad: -5, valorUnitario: 10 }] } | RN-02                     |
      | valor unitario cero | { cliente: 'Ana', productos: [{ id: 'P1', cantidad: 1, valorUnitario: 0 }] }   | RN-02                     |
      | cliente vacío       | { cliente: '', productos: [{ id: 'P1', cantidad: 1, valorUnitario: 10 }] }     | El cliente es obligatorio |

  # Riesgo: estados incoherentes (p. ej. reactivar una orden cancelada o retroceder estados).
  @adicional @RN-06
  Scenario Outline: Rechazar transición inválida <desde> -> <hacia> con 409
    * def order = call orderInStatus { targetStatus: '<desde>' }
    Given path 'orders', order.orderId, 'status'
    And request { estado: '<hacia>' }
    When method patch
    Then status 409
    And match response.code == 'RN-06'
    And match response.detail contains '<desde> -> <hacia>'

    Examples:
      | desde     | hacia     |
      | CREADA    | ENTREGADA |
      | PAGADA    | CREADA    |
      | ENVIADA   | PAGADA    |
      | ENTREGADA | ENVIADA   |
      | CANCELADA | PAGADA    |
      | CANCELADA | CANCELADA |

  # Riesgo: impedir cancelaciones legítimas (impacto directo en el cliente).
  @adicional @RN-04
  Scenario Outline: Cancelar una orden en estado <estado> responde 200
    * def order = call orderInStatus { targetStatus: '<estado>' }
    Given path 'orders', order.orderId
    When method delete
    Then status 200
    And match response.estado == 'CANCELADA'

    Examples:
      | estado  |
      | CREADA  |
      | PAGADA  |
      | ENVIADA |

  @adicional
  Scenario: Consultar una orden inexistente responde 404
    Given path 'orders', '00000000-0000-0000-0000-000000000000'
    When method get
    Then status 404
    And match response == errorSchema
    And match response.code == 'ORDER_NOT_FOUND'

  @adicional
  Scenario: Consultar con un id que no es UUID responde 400
    Given path 'orders', 'no-es-un-uuid'
    When method get
    Then status 400
    And match response.code == 'INVALID_PARAMETER'

  @adicional
  Scenario: Actualizar a un estado inexistente responde 400
    * def created = call orderInStatus { targetStatus: 'CREADA' }
    Given path 'orders', created.orderId, 'status'
    And request { estado: 'PERDIDA' }
    When method patch
    Then status 400
    And match response.code == 'MALFORMED_REQUEST'

  @adicional
  Scenario: Listar órdenes y filtrar por estado
    * def paid = call orderInStatus { targetStatus: 'PAGADA' }
    Given path 'orders'
    When method get
    Then status 200
    And match response == '#[_ > 0]'
    And match response[*].id contains paid.orderId
    Given path 'orders'
    And param estado = 'PAGADA'
    When method get
    Then status 200
    And match each response contains { estado: 'PAGADA' }
    And match response[*].id contains paid.orderId

  # Observabilidad: cada respuesta debe ser trazable de punta a punta.
  @adicional @observabilidad
  Scenario: La API propaga el X-Correlation-Id
    Given path 'orders'
    And header X-Correlation-Id = 'karate-trace-001'
    When method get
    Then status 200
    * def cid = karate.response.header('X-Correlation-Id')
    And match cid == 'karate-trace-001'
