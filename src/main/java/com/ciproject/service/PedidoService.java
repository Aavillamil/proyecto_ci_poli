package com.ciproject.service;

import com.ciproject.model.Pedido;
import com.ciproject.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de los pedidos de la tienda isimo.
 * Separa el acceso a datos del controlador REST.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido crear(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    public Optional<Pedido> actualizar(Long id, Pedido datos) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setCliente(datos.getCliente());
            pedido.setProducto(datos.getProducto());
            pedido.setCantidad(datos.getCantidad());
            pedido.setTotal(datos.getTotal());
            pedido.setEstado(datos.getEstado());
            return pedidoRepository.save(pedido);
        });
    }

    public boolean eliminar(Long id) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedidoRepository.delete(pedido);
            return true;
        }).orElse(false);
    }
}
