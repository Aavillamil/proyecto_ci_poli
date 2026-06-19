package com.ciproject;

import com.ciproject.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba unitaria del dominio Usuario. Verifica los valores por defecto
 * (rol OPERARIO y usuario activo) y la asignacion de datos.
 */
class UsuarioTest {

    @Test
    void usuarioNuevoQuedaActivoPorDefecto() {
        Usuario usuario = new Usuario();

        assertTrue(usuario.isActivo());
        assertEquals("OPERARIO", usuario.getRol());
    }

    @Test
    void seConservaElRolAsignado() {
        Usuario usuario = new Usuario("Laura Diaz", "laura@isimo.com", "ADMIN");

        assertEquals("ADMIN", usuario.getRol());
        assertEquals("laura@isimo.com", usuario.getCorreo());
    }
}
