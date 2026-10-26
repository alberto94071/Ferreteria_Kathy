package com.ferreteria.vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel para formularios: cada llamada a agregar() pone una fila
 * con la etiqueta a la izquierda y el campo a la derecha.
 */
public class Formulario extends JPanel {

    private int fila = 0;

    public Formulario() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public <C extends JComponent> C agregar(String etiqueta, C campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(5, 0, 5, 12);
        add(Estilo.etiqueta(etiqueta), g);

        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = fila;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        add(campo, g);
        fila++;
        return campo;
    }

    // Componente que ocupa las dos columnas (por ejemplo una nota o una tabla)
    public <C extends JComponent> C agregarCompleto(C componente, boolean crecer) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.weightx = 1;
        g.weighty = crecer ? 1 : 0;
        g.fill = crecer ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        add(componente, g);
        fila++;
        return componente;
    }
}
