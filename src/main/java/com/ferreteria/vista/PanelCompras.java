package com.ferreteria.vista;

import com.ferreteria.dao.CompraDAO;
import com.ferreteria.dao.ProveedorDAO;
import com.ferreteria.modelo.Compra;
import com.ferreteria.modelo.DetalleCompra;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Proveedor;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Registro de compras a proveedores (entrada de mercadería).
 */
public class PanelCompras extends JPanel implements Pantalla {

    private final CompraDAO compraDAO = new CompraDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    private final JTextField campoProducto = Estilo.campo("Código o nombre y presione Enter");
    private final JTextField campoCantidad = Estilo.campoNumero("1");
    private final JTextField campoCosto = Estilo.campoNumero("");
    private final JLabel lblUltimo = chip();
    private final JLabel lblMasBarato = chip();
    private final JLabel lblExistencia = chip();

    private final ModeloTabla<DetalleCompra> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();
    private final List<DetalleCompra> detalles = new ArrayList<>();

    private final JComboBox<Proveedor> comboProveedor = new JComboBox<>();
    private final JLabel lblProveedor = Estilo.suave(" ");
    private final JCheckBox chkCredito = new JCheckBox("Compra al crédito");
    private final JLabel lblTotal = Estilo.grande("Q 0.00");

    private Producto productoActual;

    public PanelCompras() {
        super(new BorderLayout(20, 0));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));
        add(crearCentro(), BorderLayout.CENTER);
        add(crearLateral(), BorderLayout.EAST);
        limpiarProducto();
    }

    private JPanel crearCentro() {
        JPanel buscador = new JPanel(new GridBagLayout());
        Estilo.tarjeta(buscador);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 6, 12);
        g.gridx = 0;
        buscador.add(Estilo.etiqueta("Producto (código o nombre)"), g);
        g.gridx = 1;
        buscador.add(Estilo.etiqueta("Cantidad"), g);
        g.gridx = 2;
        buscador.add(Estilo.etiqueta("Costo unitario (Q)"), g);

        g.gridy = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 12);
        g.gridx = 0;
        g.weightx = 1;
        campoProducto.setPreferredSize(new Dimension(200, 38));
        buscador.add(campoProducto, g);
        g.weightx = 0;
        g.gridx = 1;
        campoCantidad.setPreferredSize(new Dimension(90, 38));
        buscador.add(campoCantidad, g);
        g.gridx = 2;
        campoCosto.setPreferredSize(new Dimension(120, 38));
        buscador.add(campoCosto, g);
        g.gridx = 3;
        g.insets = new Insets(0, 0, 0, 0);
        JButton agregar = Estilo.alto(Estilo.botonOscuro("Agregar"), 38);
        buscador.add(agregar, g);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.add(lblUltimo);
        chips.add(lblMasBarato);
        chips.add(lblExistencia);
        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 4;
        g.insets = new Insets(10, -8, 0, 0);
        buscador.add(chips, g);

        modelo.texto("Código", 90, d -> d.getProducto().getCodigo())
                .texto("Producto", 280, d -> d.getProducto().toString())
                .cantidad("Cantidad", 90, d -> d.getCantidad())
                .dinero("Costo", 100, d -> d.getPrecioCosto())
                .dinero("Subtotal", 110, d -> d.getSubtotal());
        modelo.aplicar(tabla);
        tabla.setAutoCreateRowSorter(false);

        JPanel pieTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pieTabla.setOpaque(false);
        JButton quitar = Estilo.botonPeligro("Quitar producto (Supr)");
        pieTabla.add(quitar);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(buscador, BorderLayout.NORTH);
        cuerpo.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        cuerpo.add(pieTabla, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(Estilo.titulo("Nueva compra"), BorderLayout.NORTH);
        centro.add(cuerpo, BorderLayout.CENTER);

        campoProducto.addActionListener(e -> buscarProducto());
        campoCantidad.addActionListener(e -> campoCosto.requestFocus());
        campoCosto.addActionListener(e -> agregar());
        agregar.addActionListener(e -> agregar());
        quitar.addActionListener(e -> quitar());
        tabla.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "quitar");
        tabla.getActionMap().put("quitar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                quitar();
            }
        });
        return centro;
    }

    private JPanel crearLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        Estilo.tarjeta(lateral);
        lateral.setPreferredSize(new Dimension(310, 0));

        izq(lateral, Estilo.etiqueta("Proveedor"));
        lateral.add(Box.createVerticalStrut(6));
        comboProveedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        izq(lateral, comboProveedor);
        lateral.add(Box.createVerticalStrut(6));
        izq(lateral, lblProveedor);
        lateral.add(Box.createVerticalStrut(14));
        chkCredito.setOpaque(false);
        izq(lateral, chkCredito);
        lateral.add(Box.createVerticalStrut(12));

        JTextArea nota = new JTextArea("Al confirmar, la cantidad se suma al inventario y el precio "
                + "de compra de cada producto se actualiza con el costo ingresado.");
        nota.setLineWrap(true);
        nota.setWrapStyleWord(true);
        nota.setEditable(false);
        nota.setFocusable(false);
        nota.setBackground(Estilo.FONDO);
        nota.setForeground(Estilo.TEXTO_SUAVE);
        nota.setFont(nota.getFont().deriveFont(12f));
        nota.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), Estilo.borde(10, 10, 10, 10)));
        nota.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        izq(lateral, nota);

        lateral.add(Box.createVerticalGlue());

        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE), Estilo.borde(12, 0, 8, 0)));
        filaTotal.add(Estilo.subtitulo("Total"), BorderLayout.WEST);
        filaTotal.add(lblTotal, BorderLayout.EAST);
        filaTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        izq(lateral, filaTotal);

        JButton confirmar = Estilo.alto(Estilo.botonPrimario("Confirmar compra"), 48);
        confirmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        izq(lateral, confirmar);
        lateral.add(Box.createVerticalStrut(8));
        JButton cancelar = Estilo.alto(Estilo.botonSecundario("Cancelar"), 40);
        cancelar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        izq(lateral, cancelar);

        comboProveedor.addActionListener(e -> {
            Proveedor p = (Proveedor) comboProveedor.getSelectedItem();
            lblProveedor.setText(p == null ? " " : "NIT " + p.getNit() + " · " + p.getNombre()
                    + (p.getCelular() == null ? "" : " · " + p.getCelular()));
        });
        confirmar.addActionListener(e -> confirmar());
        cancelar.addActionListener(e -> {
            if (detalles.isEmpty() || Dialogos.confirmar(this, "¿Cancelar la compra actual?")) {
                detalles.clear();
                actualizarTabla();
                limpiarProducto();
            }
        });
        return lateral;
    }

    @Override
    public void refrescar() {
        Proveedor anterior = (Proveedor) comboProveedor.getSelectedItem();
        Dialogos.ejecutar(this, () -> {
            comboProveedor.removeAllItems();
            for (Proveedor p : proveedorDAO.listar()) {
                comboProveedor.addItem(p);
                if (anterior != null && anterior.getIdPersona() == p.getIdPersona()) {
                    comboProveedor.setSelectedItem(p);
                }
            }
        });
    }

    private void buscarProducto() {
        Dialogos.ejecutar(this, () -> {
            Producto p = DialogoBuscarProducto.resolver(this, campoProducto.getText());
            if (p == null) {
                return;
            }
            productoActual = p;
            campoProducto.setText(p.toString());
            double ultimo = compraDAO.ultimoPrecioCompra(p.getIdProducto());
            String barato = compraDAO.proveedorMasBarato(p.getIdProducto());
            campoCosto.setText(Estilo.numeroPlano(ultimo > 0 ? ultimo : p.getPrecioCompra()));
            mostrarChip(lblUltimo, "Último costo: " + (ultimo > 0 ? Formato.dinero(ultimo) : "sin compras"),
                    Estilo.FONDO, Estilo.TEXTO);
            mostrarChip(lblMasBarato, "Proveedor más barato: " + (barato == null ? "—" : barato),
                    Estilo.EXITO_FONDO, Estilo.EXITO);
            boolean bajo = p.estaBajoStock();
            mostrarChip(lblExistencia, "Existencia: " + Formato.cantidad(p.getCantidadActual())
                            + " (mín. " + Formato.cantidad(p.getExistenciaMinima()) + ")",
                    bajo ? Estilo.PELIGRO_FONDO : Estilo.FONDO, bajo ? Estilo.PELIGRO : Estilo.TEXTO);
            campoCantidad.requestFocus();
            campoCantidad.selectAll();
        });
    }

    private void agregar() {
        Dialogos.ejecutar(this, () -> {
            if (productoActual == null) {
                campoProducto.requestFocus();
                throw new IllegalStateException("Primero busque un producto");
            }
            double cantidad = Estilo.leerNumero(campoCantidad, "la cantidad");
            double costo = Estilo.leerNumero(campoCosto, "el costo");
            detalles.add(new DetalleCompra(productoActual, cantidad, costo));
            actualizarTabla();
            limpiarProducto();
        });
    }

    private void quitar() {
        DetalleCompra d = modelo.seleccionado(tabla);
        if (d == null) {
            Dialogos.aviso(this, "Seleccione en la tabla el producto que desea quitar");
            return;
        }
        detalles.remove(d);
        actualizarTabla();
    }

    private void confirmar() {
        Dialogos.ejecutar(this, () -> {
            Proveedor proveedor = (Proveedor) comboProveedor.getSelectedItem();
            if (proveedor == null) {
                throw new IllegalStateException("Registre primero un proveedor en el menú Proveedores");
            }
            Compra compra = new Compra(proveedor, Sesion.getUsuarioActual(), chkCredito.isSelected());
            for (DetalleCompra d : detalles) {
                compra.agregarDetalle(d);
            }
            compra.validarParaConfirmar();
            if (!Dialogos.confirmar(this, "¿Confirmar compra a " + proveedor + " por "
                    + Formato.dinero(compra.getTotal()) + "?")) {
                return;
            }
            compra.setFecha(LocalDateTime.now());
            compraDAO.registrar(compra);
            Dialogos.info(this, "Compra No. " + compra.getIdCompra() + " registrada.\n"
                    + "El inventario fue actualizado.");
            detalles.clear();
            actualizarTabla();
            limpiarProducto();
            chkCredito.setSelected(false);
        });
    }

    private void actualizarTabla() {
        modelo.setFilas(detalles);
        double total = 0;
        for (DetalleCompra d : detalles) {
            total += d.getSubtotal();
        }
        lblTotal.setText(Formato.dinero(total));
    }

    private void limpiarProducto() {
        productoActual = null;
        campoProducto.setText("");
        campoCantidad.setText("1");
        campoCosto.setText("");
        lblUltimo.setVisible(false);
        lblMasBarato.setVisible(false);
        lblExistencia.setVisible(false);
        campoProducto.requestFocus();
    }

    private static JLabel chip() {
        JLabel l = new JLabel();
        l.setOpaque(true);
        l.setFont(l.getFont().deriveFont(12f));
        l.setBorder(Estilo.borde(4, 8, 4, 8));
        return l;
    }

    private static void mostrarChip(JLabel l, String texto, java.awt.Color fondo, java.awt.Color color) {
        l.setText(texto);
        l.setBackground(fondo);
        l.setForeground(color);
        l.setVisible(true);
    }

    private static void izq(JPanel panel, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(c);
    }
}
