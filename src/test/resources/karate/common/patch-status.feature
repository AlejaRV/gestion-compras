@ignore
Feature: Helper reutilizable - aplica una transicion de estado valida

  Scenario:
    Given url baseUrl
    And path 'orders', orderId, 'status'
    And request { estado: '#(estado)' }
    When method patch
    Then status 200
    And match response.estado == estado
