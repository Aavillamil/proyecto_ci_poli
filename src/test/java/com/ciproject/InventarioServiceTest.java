package com.ciproject;

import com.ciproject.model.Producto;
import com.ciproject.repository.ProductoRepository;
import com.ciproject.service.InventarioService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Prueba de la logica de negocio del inventario. Verifica que descontar stock
 * funciona cuando hay disponibilidad y se rechaza cuando no la hay.
 */
class InventarioServiceTest {

    @Test
    void descuentaStockCuandoHayDisponibilidad() {
        ProductoRepository repo = mock(ProductoRepository.class);
        Producto arroz = new Producto("Arroz 500g", "Viveres", 10, 2500.0);
        when(repo.findById(1L)).thenReturn(Optional.of(arroz));
        when(repo.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        InventarioService servicio = new InventarioService(repo);
        Optional<Producto> resultado = servicio.descontarStock(1L, 4);

        assertTrue(resultado.isPresent());
        assertEquals(6, resultado.get().getStock());
    }

    @Test
    void rechazaElDescuentoSiNoHayStockSuficiente() {
        ProductoRepository repo = mock(ProductoRepository.class);
        Producto aceite = new Producto("Aceite girasol 1L", "Viveres", 2, 7000.0);
        when(repo.findById(1L)).thenReturn(Optional.of(aceite));

        InventarioService servicio = new InventarioService(repo);
        Optional<Producto> resultado = servicio.descontarStock(1L, 5);

        assertTrue(resultado.isEmpty());
    }
}
