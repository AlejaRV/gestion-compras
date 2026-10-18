@ignore
Feature: Helper reutilizable - crea una orden y la lleva a un estado por el camino feliz

  # Uso: * def result = call read('classpath:karate/common/order-in-status.feature') { targetStatus: 'ENTREGADA' }
  # Retorna: result.orderId

  Scenario:
    * def paths =
      """
      {
        CREADA: [],
        PAGADA: ['PAGADA'],
        ENVIADA: ['PAGADA', 'ENVIADA'],
        ENTREGADA: ['PAGADA', 'ENVIADA', 'ENTREGADA'],
        CANCELADA: ['CANCELADA']
      }
      """
    Given url baseUrl
    And path 'orders'
    And request read('classpath:karate/data/valid-order.json')
    When method post
    Then status 201
    * def orderId = response.id

    * def steps = karate.map(paths[targetStatus], function(s){ return { orderId: karate.get('orderId'), estado: s } })
    * call read('classpath:karate/common/patch-status.feature') steps

    Given url baseUrl
    And path 'orders', orderId
    When method get
    Then status 200
    And match response.estado == targetStatus
