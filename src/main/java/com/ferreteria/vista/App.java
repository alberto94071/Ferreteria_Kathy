package com.ferreteria.vista;

import com.ferreteria.api.ServidorApi;
import javax.swing.SwingUtilities;


public class App {

    public static void main(String[] args) {
        Estilo.instalar();
        ServidorApi.iniciar();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}