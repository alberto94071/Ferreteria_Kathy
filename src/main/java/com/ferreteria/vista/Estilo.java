package com.ferreteria.vista;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.border.Border;


public final class Estilo {

    
    public static final Color AZUL = new Color(0x0B4983);
    public static final Color AMARILLO = new Color(0xF6B238);
    public static final Color AZUL_MARINO = new Color(0x072F57);

    
    public static final Color ACENTO = AZUL;
    public static final Color MENU_FONDO = new Color(AZUL_MARINO);
    public static final Color MENU_TEXTO = new Color(0xD3E2F4);
    public static final Color MENU_BORDE = new Color(0x15497D);
    public static final Color MENU_ACTIVO = AMARILLO;
    public static final Color FONDO = new Color(0xF8FAFC);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(0xE2E8F0);
    public static final Color TEXTO = new Color(0x111827);
    public static final Color TEXTO_SUAVE = new Color(0x4B5563);
    public static final Color PELIGRO = new Color(0xB91C1C);
    public static final Color PELIGRO_FONDO = new Color(0xFEF2F2);
    public static final Color ALERTA = new Color(0x9A3412);
    public static final Color ALERTA_FONDO = new Color(0xFFF7ED);
    public static final Color EXITO = new Color(0x047857);
    public static final Color EXITO_FONDO = new Color(0xECFDF5);
    public static final Color AVISO_FONDO = new Color(0xFFFBEB);
    public static final Color INFO = new Color(0x1D4ED8);
    public static final Color INFO_FONDO = new Color(0xEFF6FF);
    public static final Color SELECCION = new Color(0xFDEBC4);
    public static final Color FONDO_LOGIN = new Color(0xF1F5FB);

    private Estilo() {
    }

    public static void instalar() {
        FlatLightLaf.setup();
        Font base = UIManager.getFont("defaultFont");
        if (base != null) {
            UIManager.put("defaultFont", base.deriveFont(13f));
        }
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", ACENTO);
        UIManager.put("Component.focusedBorderColor", ACENTO);
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("Table.selectionBackground", SELECCION);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Table.selectionInactiveBackground", SELECCION);
        UIManager.put("Table.selectionInactiveForeground", TEXTO);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.gridColor", new Color(0xEEF2F6));
        UIManager.put("TableHeader.height", 32);
        UIManager.put("TableHeader.background", new Color(0xF1F5F9));
        // Campos y botones un poco más altos (más fáciles de usar con el mouse)
        UIManager.put("TextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("PasswordField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("FormattedTextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("Button.margin", new Insets(6, 14, 6, 14));
        UIManager.put("ToggleButton.margin", new Insets(6, 14, 6, 14));
        UIManager.put("ComboBox.padding", new Insets(5, 8, 5, 8));
        UIManager.put("Spinner.padding", new Insets(5, 8, 5, 8));
        UIManager.put("ToggleButton.selectedBackground", ACENTO);
        UIManager.put("ToggleButton.selectedForeground", Color.WHITE);
        UIManager.put("ToggleButton.pressedBackground", ACENTO);
        UIManager.put("TabbedPane.selectedBackground", BLANCO);
        UIManager.put("TabbedPane.underlineColor", ACENTO);
        UIManager.put("ScrollBar.width", 12);
    }


    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(new Color(0x374151));
        return l;
    }

    public static JLabel suave(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(12f));
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel grande(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 26f));
        l.setForeground(TEXTO);
        return l;
    }

    // ---------- Botones ----------

    public static JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(ACENTO);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.putClientProperty("JButton.buttonType", null);
        return b;
    }

    public static JButton botonOscuro(String texto) {
        JButton b = botonPrimario(texto);
        b.setBackground(MENU_FONDO);
        return b;
    }

    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    public static JButton botonPeligro(String texto) {
        JButton b = botonSecundario(texto);
        b.setForeground(PELIGRO);
        return b;
    }

    public static JButton alto(JButton b, int alto) {
        Dimension d = b.getPreferredSize();
        b.setPreferredSize(new Dimension(d.width, alto));
        return b;
    }

    // ---------- Campos ----------

    public static JTextField campo(String placeholder) {
        JTextField t = new JTextField();
        if (placeholder != null) {
            t.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return t;
    }

    public static JTextField campoNumero(String valor) {
        JTextField t = new JTextField(valor);
        t.setHorizontalAlignment(JTextField.RIGHT);
        return t;
    }

    // Lee un número de un campo de texto. Acepta "1,234.50" y "1234.5"
    public static double leerNumero(JTextField campo, String nombre) {
        String texto = campo.getText().trim().replace(",", "").replace("Q", "").trim();
        if (texto.isEmpty()) {
            campo.requestFocus();
            throw new IllegalArgumentException("Ingrese " + nombre);
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            campo.requestFocus();
            throw new IllegalArgumentException("El valor de " + nombre + " no es un número válido");
        }
    }

    public static int leerEntero(JTextField campo, String nombre) {
        double valor = leerNumero(campo, nombre);
        if (valor != Math.floor(valor)) {
            campo.requestFocus();
            throw new IllegalArgumentException(nombre + " debe ser un número entero");
        }
        return (int) valor;
    }

    // Número sin símbolo para precargar campos: 85.0 -> "85.00"
    public static String numeroPlano(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor);
    }

    // Cantidad sin decimales sobrantes: 4.0 -> "4", 2.5 -> "2.5"
    public static String numeroCorto(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    // ---------- Fechas ----------

    public static JSpinner selectorFecha(LocalDate fecha) {
        Date d = Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, java.util.Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        s.setPreferredSize(new Dimension(130, s.getPreferredSize().height));
        return s;
    }

    public static LocalDate leerFecha(JSpinner selector) {
        Date d = (Date) selector.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ---------- Contenedores ----------

    public static Border borde(int arriba, int izq, int abajo, int der) {
        return BorderFactory.createEmptyBorder(arriba, izq, abajo, der);
    }

    // Panel blanco con borde gris, como una "tarjeta"
    public static JPanel tarjeta(JPanel panel) {
        panel.setBackground(BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), borde(14, 16, 14, 16)));
        return panel;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getViewport().setBackground(BLANCO);
        return s;
    }

    public static JTable tabla() {
        JTable t = new JTable();
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.setAutoCreateRowSorter(true);
        return t;
    }

    public static void transparente(JComponent... componentes) {
        for (JComponent c : componentes) {
            c.setOpaque(false);
        }
    }
}