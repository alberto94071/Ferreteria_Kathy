package com.ferreteria.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

/**
 * Logotipo dibujado con Java2D (un cuadro de color con una llave),
 * así no se necesita ningún archivo de imagen.
 */
public class Logo extends JComponent {

    private final int tamano;

    public Logo(int tamano) {
        this.tamano = tamano;
        Dimension d = new Dimension(tamano, tamano);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Estilo.ACENTO);
        g2.fillRoundRect(0, 0, tamano, tamano, tamano / 4, tamano / 4);

        // Llave (mismo trazo que el ícono del mockup, en una cuadrícula de 24x24)
        double escala = tamano / 24.0 * 0.62;
        double desplazamiento = (tamano - 24 * escala) / 2;
        AffineTransform t = new AffineTransform();
        t.translate(desplazamiento, desplazamiento);
        t.scale(escala, escala);
        Path2D llave = new Path2D.Double();
        llave.moveTo(14.7, 6.3);
        llave.curveTo(12.6, 5.0, 9.6, 5.6, 8.4, 7.8);
        llave.curveTo(7.6, 9.0, 7.6, 10.6, 8.3, 11.7);
        llave.lineTo(3, 18);
        llave.lineTo(6, 21);
        llave.lineTo(12.3, 14.7);
        llave.curveTo(14.4, 16.0, 17.4, 15.4, 18.6, 13.2);
        llave.curveTo(19.4, 12.0, 19.4, 10.4, 18.7, 9.3);
        llave.lineTo(16.2, 11.8);
        llave.lineTo(13.8, 11.2);
        llave.lineTo(13.2, 8.8);
        llave.closePath();
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke((float) (2.0 * escala), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(t.createTransformedShape(llave));
        g2.dispose();
    }
}
