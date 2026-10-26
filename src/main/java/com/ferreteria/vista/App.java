package com.ferreteria.vista;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada del programa con interfaz gráfica.
 */
public class App {

    public static void main(String[] args) {
        Estilo.instalar();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
