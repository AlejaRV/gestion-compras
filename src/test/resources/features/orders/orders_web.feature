# language: es
@e2e @serenity
Característica: Gestión de órdenes de compra en Frontend Web

  Como usuario de la aplicación web
  Quiero gestionar mis órdenes de compra visualmente
  Para consultar, crear y actualizar el ciclo de vida de las compras

  Escenario: Crear una orden desde el formulario web
    Dado que el usuario navega a la página de gestión de órdenes
    Cuando ingresa el cliente "Carlos Sanchez" y el producto "PROD-10" con cantidad 2 y valor 50000
    Y confirma la creación de la orden
    Entonces el sistema muestra un mensaje de orden creada exitosamente
