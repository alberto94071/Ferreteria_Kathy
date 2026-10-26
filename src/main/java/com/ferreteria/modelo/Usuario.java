package com.ferreteria.modelo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Usuario extends Persona {

    private String nombreUsuario;
    private String contrasena;
    private Rol rol;

    public Usuario() {
        this.rol = Rol.VENDEDOR;
    }

    public Usuario(String nombre, String direccion, String celular,
                   String nombreUsuario, String contrasenaPlana, Rol rol) {
        super(nombre, direccion, celular);
        setNombreUsuario(nombreUsuario);
        cambiarContrasena(contrasenaPlana);
        setRol(rol);
    }

    @Override
    public String getTipo() {
        return "Usuario";
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean verificarContrasena(String contrasenaPlana) {
        if (contrasenaPlana == null || contrasena == null) {
            return false;
        }
        return contrasena.equals(encriptar(contrasenaPlana));
    }

    public void cambiarContrasena(String contrasenaPlana) {
        if (contrasenaPlana == null || contrasenaPlana.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        this.contrasena = encriptar(contrasenaPlana);
    }

    public static String encriptar(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo encriptar la contraseña", e);
        }
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (nombreUsuario.trim().length() > 40) {
            throw new IllegalArgumentException("El nombre de usuario no puede tener más de 40 caracteres");
        }
        if (nombreUsuario.trim().contains(" ")) {
            throw new IllegalArgumentException("El nombre de usuario no puede tener espacios");
        }
        this.nombreUsuario = nombreUsuario.trim().toLowerCase();
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasenaEncriptada) {
        this.contrasena = contrasenaEncriptada;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio");
        }
        this.rol = rol;
    }
}
