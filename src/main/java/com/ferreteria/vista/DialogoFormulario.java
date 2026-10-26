package com.ferreteria.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Ventana modal con un Formulario y botones Guardar / Cancelar.
 *
 * Uso:
 *   DialogoFormulario d = new DialogoFormulario(this, "Nuevo cliente");
 *   JTextField nombre = d.getFormulario().agregar("Nombre", new JTextField());
 *   if (d.mostrar(() -> clienteDAO.insertar(new Cliente(...)))) { refrescar(); }
 *
 * Si la acción de guardar lanza una excepción se muestra el mensaje
 * y la ventana sigue abierta para corregir.
 */
public class DialogoFormulario extends JDialog {

    private final Formulario formulario = new Formulario();
    private final JButton guardar = Estilo.botonPrimario("Guardar");
    private Dialogos.Accion alGuardar;
    private boolean guardado = false;

    public DialogoFormulario(Component padre, String titulo) {
        super(ventanaDe(padre), titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(Estilo.borde(20, 24, 16, 24));
        contenido.add(formulario, BorderLayout.CENTER);

        JButton cancelar = Estilo.botonSecundario("Cancelar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(guardar);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> {
            if (Dialogos.ejecutar(this, alGuardar)) {
                guardado = true;
                dispose();
            }
        });
        getRootPane().setDefaultButton(guardar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public Formulario getFormulario() {
        return formulario;
    }

    public void setTextoGuardar(String texto) {
        guardar.setText(texto);
    }

    // Muestra la ventana; devuelve true si se guardó
    public boolean mostrar(Dialogos.Accion alGuardar) {
        this.alGuardar = alGuardar;
        pack();
        Dimension d = getSize();
        setSize(Math.max(d.width, 460), d.height);
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return guardado;
    }

    public static Window ventanaDe(Component c) {
        if (c == null) {
            return null;
        }
        if (c instanceof Window) {
            return (Window) c;
        }
        return SwingUtilities.getWindowAncestor(c);
    }
}
