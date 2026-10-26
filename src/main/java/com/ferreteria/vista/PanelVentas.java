package com.ferreteria.vista;

import com.ferreteria.dao.ClienteDAO;
import com.ferreteria.dao.CreditoDAO;
import com.ferreteria.dao.VentaDAO;
import com.ferreteria.modelo.Cliente;
import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Venta;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

/**
 * Punto de venta.
 */
public class PanelVentas extends JPanel implements Pantalla {

    private static final String CONSUMIDOR_FINAL = "Consumidor final";

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CreditoDAO creditoDAO = new CreditoDAO();

    private final JTextField campoProducto = Estilo.campo("Escanee el código o escriba el nombre y presione Enter");
    private final JTextField campoCantidad = Estilo.campoNumero("1");
    private final JTextField campoPrecio = Estilo.campoNumero("");
    private final JLabel lblProducto = Estilo.suave(" ");

    private final ModeloTabla<DetalleVenta> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();

    private final JComboBox<Object> comboCliente = new JComboBox<>();
    private final JToggleButton btnContado = new JToggleButton("Contado", true);
    private final JToggleButton btnCredito = new JToggleButton("Crédito");
    private final JPanel panelCredito = new JPanel(new GridBagLayout());
    private final JLabel lblLimite = new JLabel();
    private final JLabel lblDeuda = new JLabel();
    private final JSpinner spDias = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
    private final JLabel lblVence = new JLabel();
    private final JLabel lblNumProductos = Estilo.suave("0");
    private final JLabel lblTotal = Estilo.grande("Q 0.00");

    private Venta venta;
    private Producto productoActual;

    public PanelVentas() {
        super(new BorderLayout(20, 0));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));

        add(crearCentro(), BorderLayout.CENTER);
        add(crearLateral(), BorderLayout.EAST);

        // Atajos de teclado
        registrarTecla(KeyEvent.VK_F12, this::confirmar);
        // Supr solo cuando la tabla tiene el foco (para no estorbar al escribir)
        tabla.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "quitar");
        tabla.getActionMap().put("quitar", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                quitar();
            }
        });

        nuevaVenta();
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);

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
        buscador.add(Estilo.etiqueta("Precio (Q)"), g);

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
        campoPrecio.setPreferredSize(new Dimension(110, 38));
        buscador.add(campoPrecio, g);
        g.gridx = 3;
        g.insets = new Insets(0, 0, 0, 0);
        JButton agregar = Estilo.alto(Estilo.botonOscuro("Agregar"), 38);
        buscador.add(agregar, g);

        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 4;
        g.insets = new Insets(8, 0, 0, 0);
        buscador.add(lblProducto, g);

        centro.add(Estilo.titulo("Nueva venta"), BorderLayout.NORTH);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(buscador, BorderLayout.NORTH);

        modelo.texto("Código", 90, d -> d.getProducto().getCodigo())
                .texto("Producto", 280, d -> d.getProducto().toString()
                        + (d.esPrecioBajoCosto() ? "   — bajo costo (" + Formato.dinero(d.getProducto().getPrecioCompra()) + ")" : ""))
                .cantidad("Cantidad", 80, d -> d.getCantidad())
                .dinero("Precio", 90, d -> d.getPrecioFinal())
                .dinero("Subtotal", 100, d -> d.getSubtotal())
                .colorFondo(d -> d.esPrecioBajoCosto() ? Estilo.AVISO_FONDO : null);
        modelo.aplicar(tabla);
        tabla.setAutoCreateRowSorter(false);
        cuerpo.add(Estilo.scroll(tabla), BorderLayout.CENTER);

        JPanel pieTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pieTabla.setOpaque(false);
        JButton quitar = Estilo.botonPeligro("Quitar producto (Supr)");
        pieTabla.add(quitar);
        cuerpo.add(pieTabla, BorderLayout.SOUTH);
        centro.add(cuerpo, BorderLayout.CENTER);

        campoProducto.addActionListener(e -> buscarProducto());
        campoCantidad.addActionListener(e -> campoPrecio.requestFocus());
        campoPrecio.addActionListener(e -> agregar());
        agregar.addActionListener(e -> agregar());
        quitar.addActionListener(e -> quitar());
        return centro;
    }

    private JPanel crearLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        Estilo.tarjeta(lateral);
        lateral.setPreferredSize(new Dimension(310, 0));

        agregarIzq(lateral, Estilo.etiqueta("Cliente"));
        lateral.add(Box.createVerticalStrut(6));
        comboCliente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        agregarIzq(lateral, comboCliente);
        lateral.add(Box.createVerticalStrut(16));

        agregarIzq(lateral, Estilo.etiqueta("Forma de pago"));
        lateral.add(Box.createVerticalStrut(6));
        JPanel pago = new JPanel(new GridLayout(1, 2, 8, 0));
        pago.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(btnContado);
        grupo.add(btnCredito);
        pago.add(btnContado);
        pago.add(btnCredito);
        pago.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        agregarIzq(lateral, pago);
        lateral.add(Box.createVerticalStrut(12));

        panelCredito.setBackground(Estilo.FONDO);
        panelCredito.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), Estilo.borde(10, 12, 10, 12)));
        filaCredito(0, "Límite de crédito", lblLimite);
        filaCredito(1, "Deuda pendiente", lblDeuda);
        filaCredito(2, "Días de crédito", spDias);
        filaCredito(3, "Vence", lblVence);
        panelCredito.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        agregarIzq(lateral, panelCredito);

        lateral.add(Box.createVerticalGlue());

        JPanel totales = new JPanel(new BorderLayout());
        totales.setOpaque(false);
        totales.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE), Estilo.borde(12, 0, 8, 0)));
        JPanel filaProductos = new JPanel(new BorderLayout());
        filaProductos.setOpaque(false);
        filaProductos.add(Estilo.suave("Productos"), BorderLayout.WEST);
        filaProductos.add(lblNumProductos, BorderLayout.EAST);
        totales.add(filaProductos, BorderLayout.NORTH);
        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.add(Estilo.subtitulo("Total"), BorderLayout.WEST);
        filaTotal.add(lblTotal, BorderLayout.EAST);
        totales.add(filaTotal, BorderLayout.SOUTH);
        totales.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        agregarIzq(lateral, totales);

        JButton confirmar = Estilo.alto(Estilo.botonPrimario("Confirmar venta (F12)"), 48);
        confirmar.setFont(confirmar.getFont().deriveFont(15f));
        confirmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        agregarIzq(lateral, confirmar);
        lateral.add(Box.createVerticalStrut(8));
        JButton cancelar = Estilo.alto(Estilo.botonSecundario("Cancelar venta"), 40);
        cancelar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        agregarIzq(lateral, cancelar);

        btnContado.addActionListener(e -> actualizarCredito());
        btnCredito.addActionListener(e -> actualizarCredito());
        comboCliente.addActionListener(e -> {
            Object o = comboCliente.getSelectedItem();
            if (o instanceof Cliente) {
                spDias.setValue(Math.max(1, ((Cliente) o).getDiasCredito()));
            }
            actualizarCredito();
        });
        spDias.addChangeListener(e -> actualizarCredito());
        confirmar.addActionListener(e -> confirmar());
        cancelar.addActionListener(e -> {
            if (venta.getDetalles().isEmpty() || Dialogos.confirmar(this, "¿Cancelar la venta actual?")) {
                nuevaVenta();
            }
        });
        return lateral;
    }

    // ---------- Lógica ----------

    @Override
    public void refrescar() {
        Object anterior = comboCliente.getSelectedItem();
        Dialogos.ejecutar(this, () -> {
            comboCliente.removeAllItems();
            comboCliente.addItem(CONSUMIDOR_FINAL);
            for (Cliente c : clienteDAO.listar()) {
                comboCliente.addItem(c);
                if (anterior instanceof Cliente && ((Cliente) anterior).getIdPersona() == c.getIdPersona()) {
                    comboCliente.setSelectedItem(c);
                }
            }
        });
        actualizarCredito();
        campoProducto.requestFocusInWindow();
    }

    private void nuevaVenta() {
        venta = new Venta(null, Sesion.getUsuarioActual(), false);
        modelo.setFilas(venta.getDetalles());
        limpiarProducto();
        btnContado.setSelected(true);
        if (comboCliente.getItemCount() > 0) {
            comboCliente.setSelectedIndex(0);
        }
        actualizarTotales();
        actualizarCredito();
        campoProducto.requestFocusInWindow();
    }

    private void buscarProducto() {
        Dialogos.ejecutar(this, () -> {
            Producto p = DialogoBuscarProducto.resolver(this, campoProducto.getText());
            if (p == null) {
                return;
            }
            productoActual = p;
            campoProducto.setText(p.toString());
            campoPrecio.setText(Estilo.numeroPlano(p.getPrecioVenta()));
            String marca = p.getMarca() == null ? "" : " · " + p.getMarca();
            lblProducto.setText(p + marca + " · Existencia: " + Formato.cantidad(p.getCantidadActual())
                    + " " + p.getUnidad() + " · Precio de venta " + Formato.dinero(p.getPrecioVenta()));
            lblProducto.setForeground(p.getCantidadActual() <= 0 ? Estilo.PELIGRO : Estilo.TEXTO_SUAVE);
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
            double precio = Estilo.leerNumero(campoPrecio, "el precio");
            venta.agregarDetalle(new DetalleVenta(productoActual, cantidad, precio));
            modelo.setFilas(venta.getDetalles());
            actualizarTotales();
            limpiarProducto();
        });
    }

    private void quitar() {
        DetalleVenta d = modelo.seleccionado(tabla);
        if (d == null) {
            Dialogos.aviso(this, "Seleccione en la tabla el producto que desea quitar");
            return;
        }
        venta.quitarDetalle(d);
        modelo.setFilas(venta.getDetalles());
        actualizarTotales();
    }

    private void confirmar() {
        Dialogos.ejecutar(this, () -> {
            Object o = comboCliente.getSelectedItem();
            Cliente cliente = o instanceof Cliente ? (Cliente) o : null;
            boolean credito = btnCredito.isSelected();
            if (credito && cliente == null) {
                throw new IllegalStateException("Para vender al crédito seleccione un cliente registrado");
            }
            venta.setCliente(cliente);
            venta.setEsCredito(credito);
            venta.validarParaConfirmar();
            if (credito) {
                // Se revisa antes de preguntar, para no confirmar algo que el sistema va a rechazar
                double deuda = clienteDAO.obtenerDeudaPendiente(cliente.getIdPersona());
                if (!cliente.puedeRecibirCredito(deuda)) {
                    throw new IllegalStateException(cliente.getLimiteCredito() <= 0
                            ? "El cliente " + cliente + " no tiene crédito autorizado.\nPuede asignarle un límite en Clientes."
                            : "El cliente " + cliente + " tiene un saldo pendiente de " + Formato.dinero(deuda)
                            + ".\nDebe cancelarlo antes de recibir otro crédito.");
                }
                if (venta.getTotal() > cliente.getLimiteCredito()) {
                    throw new IllegalStateException("El total (" + Formato.dinero(venta.getTotal())
                            + ") supera el límite de crédito del cliente (" + Formato.dinero(cliente.getLimiteCredito()) + ")");
                }
            }

            if (venta.tieneProductosBajoCosto() && !Dialogos.confirmar(this,
                    "Hay productos con precio menor al costo (marcados en amarillo).\n¿Desea continuar de todos modos?")) {
                return;
            }
            String tipo = credito ? "AL CRÉDITO a " + cliente : "de contado";
            if (!Dialogos.confirmar(this, "¿Confirmar venta " + tipo + " por " + Formato.dinero(venta.getTotal()) + "?")) {
                return;
            }
            venta.setFecha(LocalDateTime.now());
            ventaDAO.registrar(venta, credito ? (Integer) spDias.getValue() : 0);

            String mensaje = "Venta No. " + venta.getIdVenta() + " registrada.\nTotal: " + Formato.dinero(venta.getTotal());
            if (credito) {
                Credito cr = creditoDAO.buscarPorVenta(venta.getIdVenta());
                if (cr != null) {
                    mensaje += "\nVence: " + Formato.fecha(cr.getFechaVencimiento());
                }
            }
            Dialogos.info(this, mensaje);
            nuevaVenta();
            refrescar();
        });
    }

    private void actualizarTotales() {
        lblNumProductos.setText(String.valueOf(venta.getDetalles().size()));
        lblTotal.setText(Formato.dinero(venta.getTotal()));
    }

    private void actualizarCredito() {
        boolean credito = btnCredito.isSelected();
        panelCredito.setVisible(credito);
        pintarToggle(btnContado);
        pintarToggle(btnCredito);
        if (!credito) {
            return;
        }
        Object o = comboCliente.getSelectedItem();
        if (!(o instanceof Cliente)) {
            lblLimite.setText("—");
            lblDeuda.setText("Seleccione un cliente");
            lblDeuda.setForeground(Estilo.PELIGRO);
            lblVence.setText("—");
            return;
        }
        Cliente c = (Cliente) o;
        lblLimite.setText(Formato.dinero(c.getLimiteCredito()));
        try {
            double deuda = clienteDAO.obtenerDeudaPendiente(c.getIdPersona());
            lblDeuda.setText(Formato.dinero(deuda));
            lblDeuda.setForeground(deuda > 0 ? Estilo.PELIGRO : Estilo.EXITO);
        } catch (java.sql.SQLException e) {
            lblDeuda.setText("Error al consultar");
        }
        lblVence.setText(Formato.fecha(LocalDate.now().plusDays((Integer) spDias.getValue())));
    }

    private void limpiarProducto() {
        productoActual = null;
        campoProducto.setText("");
        campoCantidad.setText("1");
        campoPrecio.setText("");
        lblProducto.setText(" ");
        campoProducto.requestFocus();
    }

    // ---------- Ayudas de diseño ----------

    // El color del botón seleccionado lo pone FlatLaf (ToggleButton.selectedBackground en Estilo)
    private void pintarToggle(JToggleButton b) {
        b.setForeground(b.isSelected() ? Color.WHITE : Estilo.TEXTO);
    }

    private void filaCredito(int fila, String texto, JComponent valor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila;
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(3, 0, 3, 8);
        panelCredito.add(Estilo.suave(texto), g);
        g.gridx = 1;
        g.weightx = 0;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(3, 0, 3, 0);
        panelCredito.add(valor, g);
    }

    private static void agregarIzq(JPanel panel, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(c);
    }

    private void registrarTecla(int tecla, Runnable accion) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla, 0), "tecla" + tecla);
        getActionMap().put("tecla" + tecla, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isShowing()) {
                    accion.run();
                }
            }
        });
    }
}
