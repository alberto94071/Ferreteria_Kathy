package com.ferreteria.vista;

import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Mensajes al usuario y manejo uniforme de errores.
 *
 * Dialogos.ejecutar(...) corre una acción y, si falla, muestra el mensaje:
 *  - errores de validación del modelo (IllegalArgumentException / IllegalStateException) como aviso
 *  - errores de base de datos (SQLException) como error
 */
public final class Dialogos {

    public interface Accion {
        void ejecutar() throws Exception;
    }

    private Dialogos() {
    }

    // Devuelve true si la acción terminó sin errores
    public static boolean ejecutar(Component padre, Accion accion) {
        try {
            accion.ejecutar();
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            aviso(padre, e.getMessage());
        } catch (SQLException e) {
            error(padre, "Ocurrió un error con la base de datos:\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            error(padre, "Ocurrió un error inesperado:\n" + e);
        }
        return false;
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
