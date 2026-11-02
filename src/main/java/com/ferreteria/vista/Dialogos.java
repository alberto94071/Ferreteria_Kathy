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

    public interface GeneradorArchivo {
        void generar(java.io.File archivo) throws Exception;
    }

    /**
     * Pregunta dónde guardar un PDF (propone la carpeta Documentos y el nombre indicado),
     * lo genera y lo abre con el programa de PDF de la computadora.
     */
    public static void guardarPDF(Component padre, String nombreSugerido, GeneradorArchivo generador) {
        java.io.File carpeta = new java.io.File(System.getProperty("user.home"), "Documents");
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser(carpeta.isDirectory() ? carpeta : null);
        selector.setDialogTitle("Guardar PDF");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documento PDF", "pdf"));
        selector.setSelectedFile(new java.io.File(selector.getCurrentDirectory(), nombreSugerido));
        if (selector.showSaveDialog(padre) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new java.io.File(archivo.getParentFile(), archivo.getName() + ".pdf");
        }
        if (archivo.exists() && !confirmar(padre, "El archivo " + archivo.getName() + " ya existe.\n¿Desea reemplazarlo?")) {
            return;
        }
        final java.io.File destino = archivo;
        if (ejecutar(padre, () -> generador.generar(destino))) {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(destino);
                    return;
                }
            } catch (Exception e) {
                // Si no hay programa para abrir PDF, solo se avisa dónde quedó
            }
            info(padre, "PDF guardado en:\n" + destino.getAbsolutePath());
        }
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int r = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
