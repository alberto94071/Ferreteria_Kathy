package com.ferreteria.vista;

import com.ferreteria.api.ServidorApi;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del programa con interfaz gráfica.
 * También enciende la API para la app del celular.
 */
public class App {

    public static void main(String[] args) {
        Estilo.instalar();
        ServidorApi.iniciar();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}