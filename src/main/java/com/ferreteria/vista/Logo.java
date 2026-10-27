package com.ferreteria.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Logotipo de Ferretería Kathy.
 *
 * - logoCompleto(ancho): la imagen del logo (src/main/resources/imagenes/logo_ferreteria.png),
 *   que va dentro del .jar, así no se pierde al mover el proyecto.
 * - El componente Logo dibuja el ícono (tuerca azul con martillo amarillo) con Java2D,
 *   nítido a cualquier tamaño. Se usa como ícono de las ventanas.
 */
public class Logo extends JComponent {

    private static final String RUTA_LOGO = "/imagenes/logo_ferreteria.png";
    private static BufferedImage logo;

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
        dibujarIcono((Graphics2D) g, tamano);
    }

    // ---------- Logo completo (imagen) ----------

    /** Logo escalado al ancho indicado, o null si no se encontró el archivo. */
    public static ImageIcon logoCompleto(int ancho) {
        BufferedImage original = cargar();
        if (original == null) {
            return null;
        }
        int alto = original.getHeight() * ancho / original.getWidth();
        return new ImageIcon(escalar(original, ancho, alto));
    }

    private static BufferedImage cargar() {
        if (logo == null) {
            try {
                URL url = Logo.class.getResource(RUTA_LOGO);
                if (url != null) {
                    logo = ImageIO.read(url);
                }
            } catch (Exception e) {
                System.out.println("No se pudo cargar el logo: " + e.getMessage());
            }
        }
        return logo;
    }

    // Reduce en varios pasos para que no se vea pixelado
    private static BufferedImage escalar(BufferedImage img, int ancho, int alto) {
        BufferedImage actual = img;
        int w = img.getWidth();
        int h = img.getHeight();
        do {
            w = Math.max(ancho, w / 2);
            h = Math.max(alto, h / 2);
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(actual, 0, 0, w, h, null);
            g.dispose();
            actual = tmp;
        } while (w != ancho || h != alto);
        return actual;
    }

    // ---------- Ícono (dibujado) ----------

    /** Íconos de varios tamaños para la barra de tareas de Windows. */
    public static List<Image> iconosVentana() {
        List<Image> lista = new ArrayList<>();
        for (int t : new int[]{16, 24, 32, 48, 64, 128}) {
            BufferedImage img = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            dibujarIcono(g, t);
            g.dispose();
            lista.add(img);
        }
        return lista;
    }

    /** Tuerca hexagonal azul, círculo blanco y martillo amarillo, como en el logotipo. */
    public static void dibujarIcono(Graphics2D g0, int t) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double c = t / 2.0;
        double r = t / 2.0;

        // Hexágono con puntas a los lados
        Path2D hex = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double ang = Math.toRadians(60 * i);
            double x = c + r * Math.cos(ang);
            double y = c + r * 0.92 * Math.sin(ang);
            if (i == 0) {
                hex.moveTo(x, y);
            } else {
                hex.lineTo(x, y);
            }
        }
        hex.closePath();
        g.setColor(Estilo.AZUL);
        g.fill(hex);

        double rc = r * 0.66;
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(c - rc, c - rc, rc * 2, rc * 2));

        // Martillo: diseñado en una cuadrícula de 100 y girado 45°
        AffineTransform at = new AffineTransform();
        at.translate(c, c);
        at.rotate(Math.toRadians(-45));
        at.scale(t * 0.8 / 100.0, t * 0.8 / 100.0);
        at.translate(-50, -51);
        g.setColor(Estilo.AMARILLO);
        g.fill(at.createTransformedShape(new RoundRectangle2D.Double(45, 40, 10, 38, 6, 6)));
        Path2D cabeza = new Path2D.Double();
        cabeza.moveTo(30, 24);
        cabeza.lineTo(64, 24);
        cabeza.lineTo(70, 30);
        cabeza.lineTo(64, 38);
        cabeza.lineTo(30, 38);
        cabeza.closePath();
        g.fill(at.createTransformedShape(cabeza));
        g.dispose();
    }
}