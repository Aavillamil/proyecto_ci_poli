package com.ciproject;

import com.ciproject.model.Pedido;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba unitaria del dominio de negocio. Verifica que un pedido nuevo
 * se inicializa en estado PENDIENTE y conserva los datos del cliente.
 * Sirve para que las etapas Test de Jenkins, Travis CI y Codeship
 * ejecuten una validacion real del codigo.
 */
class PedidoTest {

    @Test
    void pedidoNuevoArrancaEnEstadoPendiente() {
        Pedido pedido = new Pedido("Carlos Gomez", "Arroz 500g", 12, 30000.0);

        assertEquals("PENDIENTE", pedido.getEstado());
        assertEquals("Carlos Gomez", pedido.getCliente());
        assertEquals(12, pedido.getCantidad());
        assertEquals(30000.0, pedido.getTotal());
    }

    @Test
    void elEstadoDelPedidoSePuedeActualizar() {
        Pedido pedido = new Pedido("Ana Ruiz", "Aceite girasol 1L", 3, 21000.0);
        pedido.setEstado("ENVIADO");

        assertEquals("ENVIADO", pedido.getEstado());
    }
}
