package com.ciproject.service;

import com.ciproject.model.Usuario;
import com.ciproject.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de los usuarios del sistema (operarios y administradores
 * de la tienda isimo que gestionan pedidos e inventario).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crear(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> actualizar(Long id, Usuario datos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datos.getNombre());
            usuario.setCorreo(datos.getCorreo());
            usuario.setRol(datos.getRol());
            usuario.setActivo(datos.isActivo());
            return usuarioRepository.save(usuario);
        });
    }

    public boolean eliminar(Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuarioRepository.delete(usuario);
            return true;
        }).orElse(false);
    }
}
