package com.ferreteria.util;

import com.ferreteria.modelo.Usuario;

public class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        throw new UnsupportedOperationException("Pendiente");
    }

    public static Usuario getUsuarioActual() {
        throw new UnsupportedOperationException("Pendiente");
    }

    public static boolean haySesion() {
        throw new UnsupportedOperationException("Pendiente");
    }

    public static boolean esAdministrador() {
        throw new UnsupportedOperationException("Pendiente");
    }

    public static void cerrar() {
        throw new UnsupportedOperationException("Pendiente");
    }
}
