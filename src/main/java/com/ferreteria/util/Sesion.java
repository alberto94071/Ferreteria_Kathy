package com.ferreteria.util;

import com.ferreteria.modelo.Usuario;

public class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se puede iniciar sesión sin un usuario");
        }
        if (!usuario.isActivo()) {
            throw new IllegalStateException("El usuario está desactivado");
        }
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        if (usuarioActual == null) {
            throw new IllegalStateException("No hay ninguna sesión iniciada");
        }
        return usuarioActual;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    public static boolean esAdministrador() {
        return haySesion() && usuarioActual.esAdministrador();
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
