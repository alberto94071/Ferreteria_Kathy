package com.ferreteria.vista;

import com.ferreteria.dao.ClienteDAO;
import com.ferreteria.dao.CreditoDAO;
import com.ferreteria.modelo.Abono;
import com.ferreteria.modelo.Cliente;
import com.ferreteria.modelo.Credito;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * Créditos pendientes, vencidos o por cliente, y registro de abonos.
 */
public class PanelCreditos extends JPanel implements Pantalla {

    private final CreditoDAO creditoDAO = new CreditoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final JToggleButton btnPendientes = new JToggleButton("Pendientes", true);
    private final JToggleButton btnVencidos = new JToggleButton("Vencidos");
    private final JToggleButton btnCliente = new JToggleButton("Por cliente");
    private final JComboBox<Cliente> comboCliente = new JComboBox<>();

    private final ModeloTabla<Credito> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();

    private final JLabel lblCliente = Estilo.subtitulo("Seleccione un crédito");
    private final JLabel lblVenta = Estilo.suave(" ");
    private final JLabel lblTotal = new JLabel("—");
    private final JLabel lblAbonado = new JLabel("—");
    private final JLabel lblSaldo = Estilo.grande("—");
    private final ModeloTabla<Abono> modeloAbonos = new ModeloTabla<>();
    private final JTextField campoMonto = Estilo.campoNumero("");
    private final JButton btnAbonar = Estilo.alto(Estilo.botonPrimario("Registrar abono"), 44);
    private final JButton btnTodo = Estilo.botonSecundario("Pagar todo");

    public PanelCreditos() {
        super(new BorderLayout(0, 12));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo("Créditos"), BorderLayout.WEST);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filtros.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (JToggleButton b : new JToggleButton[]{btnPendientes, btnVencidos, btnCliente}) {
            grupo.add(b);
            filtros.add(b);
            b.addActionListener(e -> {
                comboCliente.setVisible(btnCliente.isSelected());
                refrescar();
            });
        }
        comboCliente.setPreferredSize(new Dimension(220, 32));
        comboCliente.setVisible(false);
        comboCliente.addActionListener(e -> {
            if (btnCliente.isSelected()) {
                cargarTabla();
            }
        });
        filtros.add(comboCliente);
        encabezado.add(filtros, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        modelo.entero("Venta", 60, c -> c.getVenta().getIdVenta())
                .texto("Cliente", 200, c -> c.getVenta().getCliente())
                .texto("Fecha venta", 100, c -> Formato.fecha(c.getVenta().getFecha().toLocalDate()))
                .texto("Vence", 100, c -> Formato.fecha(c.getFechaVencimiento()))
                .dinero("Total", 100, c -> c.getVenta().getTotal())
                .dinero("Saldo", 100, c -> c.getSaldo())
                .texto("Estado", 90, c -> estado(c))
                .colorTexto(c -> c.estaPagado() ? Estilo.EXITO : (c.estaVencido() ? Estilo.PELIGRO : null));
        modelo.aplicar(tabla);

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        centro.add(crearDetalle(), BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(modelo.seleccionado(tabla));
            }
        });
        btnAbonar.addActionListener(e -> abonar());
        campoMonto.addActionListener(e -> abonar());
        btnTodo.addActionListener(e -> {
            Credito c = modelo.seleccionado(tabla);
            if (c != null) {
                campoMonto.setText(Estilo.numeroPlano(c.getSaldo()));
                campoMonto.requestFocus();
            }
        });
    }

    private JPanel crearDetalle() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Estilo.tarjeta(p);
        p.setPreferredSize(new Dimension(320, 0));

        izq(p, lblCliente);
        izq(p, lblVenta);
        p.add(Box.createVerticalStrut(12));
        JPanel datos = new JPanel(new GridBagLayout());
        datos.setOpaque(false);
        fila(datos, 0, Estilo.suave("Total de la venta"), lblTotal);
        fila(datos, 1, Estilo.suave("Total abonado"), lblAbonado);
        fila(datos, 2, Estilo.subtitulo("Saldo"), lblSaldo);
        datos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        izq(p, datos);
        p.add(Box.createVerticalStrut(10));

        izq(p, Estilo.etiqueta("Abonos registrados"));
        p.add(Box.createVerticalStrut(6));
        modeloAbonos.texto("Fecha", 140, a -> Formato.fechaHora(a.getFecha()))
                .texto("Recibió", 60, a -> a.getUsuario().getNombreUsuario())
                .dinero("Monto", 80, a -> a.getMonto());
        JTable tablaAbonos = Estilo.tabla();
        modeloAbonos.aplicar(tablaAbonos);
        javax.swing.JScrollPane sc = Estilo.scroll(tablaAbonos);
        sc.setPreferredSize(new Dimension(280, 140));
        izq(p, sc);
        p.add(Box.createVerticalGlue());
        p.add(Box.createVerticalStrut(12));

        izq(p, Estilo.etiqueta("Monto del abono (Q)"));
        p.add(Box.createVerticalStrut(6));
        JPanel filaMonto = new JPanel(new BorderLayout(8, 0));
        filaMonto.setOpaque(false);
        filaMonto.add(campoMonto, BorderLayout.CENTER);
        filaMonto.add(btnTodo, BorderLayout.EAST);
        filaMonto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        izq(p, filaMonto);
        p.add(Box.createVerticalStrut(4));
        izq(p, Estilo.suave("No puede ser mayor que el saldo pendiente"));
        p.add(Box.createVerticalStrut(10));
        btnAbonar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        izq(p, btnAbonar);
        return p;
    }

    @Override
    public void refrescar() {
        if (btnCliente.isSelected()) {
            Cliente anterior = (Cliente) comboCliente.getSelectedItem();
            Dialogos.ejecutar(this, () -> {
                comboCliente.removeAllItems();
                for (Cliente c : clienteDAO.listar()) {
                    comboCliente.addItem(c);
                    if (anterior != null && anterior.getIdPersona() == c.getIdPersona()) {
                        comboCliente.setSelectedItem(c);
                    }
                }
            });
        }
        cargarTabla();
    }

    private void cargarTabla() {
        Credito anterior = modelo.seleccionado(tabla);
        Dialogos.ejecutar(this, () -> {
            List<Credito> lista;
            if (btnVencidos.isSelected()) {
                lista = creditoDAO.listarVencidos();
            } else if (btnCliente.isSelected()) {
                Cliente c = (Cliente) comboCliente.getSelectedItem();
                lista = c == null ? new java.util.ArrayList<Credito>() : creditoDAO.listarPorCliente(c.getIdPersona());
            } else {
                lista = creditoDAO.listarPendientes();
            }
            modelo.setFilas(lista);
        });
        pintarFiltros();
        mostrarDetalle(null);
        if (anterior != null) {
            for (Credito c : modelo.getFilas()) {
                if (c.getIdCredito() == anterior.getIdCredito()) {
                    modelo.seleccionar(tabla, c);
                }
            }
        }
    }

    private void mostrarDetalle(Credito c) {
        boolean hay = c != null;
        btnAbonar.setEnabled(hay && !c.estaPagado());
        btnTodo.setEnabled(hay && !c.estaPagado());
        campoMonto.setEnabled(hay && !c.estaPagado());
        if (!hay) {
            lblCliente.setText("Seleccione un crédito");
            lblVenta.setText(" ");
            lblTotal.setText("—");
            lblAbonado.setText("—");
            lblSaldo.setText("—");
            lblSaldo.setForeground(Estilo.TEXTO);
            modeloAbonos.setFilas(null);
            return;
        }
        lblCliente.setText(String.valueOf(c.getVenta().getCliente()));
        lblVenta.setText("Venta No. " + c.getVenta().getIdVenta() + " · vence " + Formato.fecha(c.getFechaVencimiento())
                + " · " + estado(c));
        lblTotal.setText(Formato.dinero(c.getVenta().getTotal()));
        lblAbonado.setText(Formato.dinero(c.getTotalAbonado()));
        lblSaldo.setText(Formato.dinero(c.getSaldo()));
        lblSaldo.setForeground(c.estaPagado() ? Estilo.EXITO : (c.estaVencido() ? Estilo.PELIGRO : Estilo.TEXTO));
        modeloAbonos.setFilas(c.getAbonos());
        campoMonto.setText("");
    }

    private void abonar() {
        Credito c = modelo.seleccionado(tabla);
        if (c == null) {
            Dialogos.aviso(this, "Seleccione un crédito de la tabla");
            return;
        }
        Dialogos.ejecutar(this, () -> {
            double monto = Estilo.leerNumero(campoMonto, "el monto del abono");
            if (!Dialogos.confirmar(this, "¿Registrar abono de " + Formato.dinero(monto) + " de "
                    + c.getVenta().getCliente() + "?")) {
                return;
            }
            creditoDAO.registrarAbono(c, new Abono(monto, Sesion.getUsuarioActual()));
            String msg = "Abono registrado.\nSaldo pendiente: " + Formato.dinero(c.getSaldo());
            if (c.estaPagado()) {
                msg = "Abono registrado. ¡El crédito quedó cancelado por completo!";
            }
            Dialogos.info(this, msg);
            cargarTabla();
        });
    }

    private static String estado(Credito c) {
        if (c.estaPagado()) {
            return "Pagado";
        }
        return c.estaVencido() ? "Vencido" : "Pendiente";
    }

    private void pintarFiltros() {
        for (JToggleButton b : new JToggleButton[]{btnPendientes, btnVencidos, btnCliente}) {
            b.setForeground(b.isSelected() ? Color.WHITE : Estilo.TEXTO);
        }
    }

    private static void fila(JPanel panel, int fila, JLabel texto, JLabel valor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila;
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 8);
        panel.add(texto, g);
        g.gridx = 1;
        g.weightx = 0;
        g.anchor = GridBagConstraints.EAST;
        panel.add(valor, g);
    }

    private static void izq(JPanel panel, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(c);
    }
}
