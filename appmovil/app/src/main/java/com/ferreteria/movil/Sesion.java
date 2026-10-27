package com.ferreteria.movil;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Datos que el celular recuerda entre usos: la dirección de la computadora
 * de la ferretería y la sesión del usuario.
 */
public final class Sesion {

    private static final String ARCHIVO = "ferreteria";

    private Sesion() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static String servidor(Context c) {
        return prefs(c).getString("servidor", "");
    }

    public static String token(Context c) {
        return prefs(c).getString("token", null);
    }

    public static String nombre(Context c) {
        return prefs(c).getString("nombre", "");
    }

    public static String negocio(Context c) {
        return prefs(c).getString("negocio", "Ferretería");
    }

    public static boolean esAdministrador(Context c) {
        return "ADMINISTRADOR".equals(prefs(c).getString("rol", ""));
    }

    public static boolean activa(Context c) {
        return token(c) != null && !servidor(c).isEmpty();
    }

    public static void guardarServidor(Context c, String servidor) {
        prefs(c).edit().putString("servidor", servidor).apply();
    }

    public static void iniciar(Context c, String token, String nombre, String rol, String negocio) {
        prefs(c).edit()
                .putString("token", token)
                .putString("nombre", nombre)
                .putString("rol", rol)
                .putString("negocio", negocio)
                .apply();
    }

    // Borra la sesión pero recuerda la dirección del servidor
    public static void cerrar(Context c) {
        prefs(c).edit().remove("token").remove("nombre").remove("rol").apply();
    }
}
