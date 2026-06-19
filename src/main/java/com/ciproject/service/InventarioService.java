package com.ciproject.service;

import com.ciproject.model.Producto;
import com.ciproject.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio del inventario de productos de la tienda isimo.
 * Ademas del CRUD, controla la disponibilidad de stock.
 */
@Service
public class InventarioService {

    private final ProductoRepository productoRepository;

    public InventarioService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    public Optional<Producto> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(datos.getNombre());
            producto.setCategoria(datos.getCategoria());
            producto.setStock(datos.getStock());
            producto.setPrecio(datos.getPrecio());
            return productoRepository.save(producto);
        });
    }

    public boolean eliminar(Long id) {
        return productoRepository.findById(id).map(producto -> {
            productoRepository.delete(producto);
            return true;
        }).orElse(false);
    }

    /**
     * Descuenta unidades del stock cuando se despacha un pedido.
     * Devuelve vacio si el producto no existe o no hay stock suficiente.
     */
    public Optional<Producto> descontarStock(Long id, int cantidad) {
        return productoRepository.findById(id)
                .filter(producto -> cantidad > 0 && producto.getStock() >= cantidad)
                .map(producto -> {
                    producto.setStock(producto.getStock() - cantidad);
                    return productoRepository.save(producto);
                });
    }
}
