package com.ciproject.controller;

import com.ciproject.model.Producto;
import com.ciproject.service.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<Producto> listar() {
        return inventarioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return inventarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return inventarioService.actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return inventarioService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** Descuenta unidades del stock de un producto al despachar un pedido. */
    @PatchMapping("/{id}/descontar")
    public ResponseEntity<Producto> descontar(@PathVariable Long id, @RequestParam int cantidad) {
        return inventarioService.descontarStock(id, cantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
