package com.ferreteria.vista;

/**
 * Toda pantalla del menú implementa esto. La ventana principal llama
 * refrescar() cada vez que se muestra, para cargar datos actualizados.
 */
public interface Pantalla {

    void refrescar();
}
