package com.ferreteria.vista;

import com.ferreteria.dao.CompraDAO;
import com.ferreteria.dao.VentaDAO;
import com.ferreteria.modelo.Compra;
import com.ferreteria.modelo.DetalleCompra;
import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Venta;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 * Reportes por rango de fechas: ventas, compras y totales.
 * El administrador puede anular ventas desde aquí.
 */
public class PanelReportes extends JPanel implements Pantalla {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final CompraDAO compraDAO = new CompraDAO();

    private final JSpinner desde = Estilo.selectorFecha(LocalDate.now());
    private final JSpinner hasta = Estilo.selectorFecha(LocalDate.now());

    private final JLabel lblVendido = Estilo.grande("Q 0.00");
    private final JLabel lblNumVentas = Estilo.grande("0");
    private final JLabel lblGanancia = Estilo.grande("Q 0.00");
    private final JLabel lblComprado = Estilo.grande("Q 0.00");

    private final ModeloTabla<Venta> modeloVentas = new ModeloTabla<>();
    private final JTable tablaVentas = Estilo.tabla();
    private final ModeloTabla<Compra> modeloCompras = new ModeloTabla<>();
    private final JTable tablaCompras = Estilo.tabla();
    private final ModeloTabla<Object[]> modeloTop = new ModeloTabla<>();

    public PanelReportes() {
        super(new BorderLayout(0, 14));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));
        boolean admin = Sesion.esAdministrador();

        // Encabezado y filtros
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo("Reportes"), BorderLayout.WEST);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setOpaque(false);
        JButton hoy = Estilo.botonSecundario("Hoy");
        JButton mes = Estilo.botonSecundario("Este mes");
        filtros.add(hoy);
        filtros.add(mes);
        filtros.add(Estilo.etiqueta("  Desde"));
        filtros.add(desde);
        filtros.add(Estilo.etiqueta("Hasta"));
        filtros.add(hasta);
        JButton buscar = Estilo.botonOscuro("Ver reporte");
        filtros.add(buscar);
        encabezado.add(filtros, BorderLayout.EAST);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta("Total vendido", lblVendido));
        tarjetas.add(tarjeta("Número de ventas", lblNumVentas));
        tarjetas.add(tarjeta("Ganancia estimada", lblGanancia));
        tarjetas.add(tarjeta("Total comprado", lblComprado));

        JPanel arriba = new JPanel(new BorderLayout(0, 14));
        arriba.setOpaque(false);
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(tarjetas, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        // Ventas
        modeloVentas.entero("No.", 60, v -> v.getIdVenta())
                .texto("Fecha", 130, v -> Formato.fechaHora(v.getFecha()))
                .texto("Cliente", 200, v -> v.getCliente() == null ? "Consumidor final" : v.getCliente().toString())
                .texto("Vendedor", 110, v -> v.getUsuario().getNombreUsuario())
                .texto("Tipo", 80, v -> v.isEsCredito() ? "Crédito" : "Contado")
                .dinero("Total", 100, v -> v.getTotal());
        modeloVentas.aplicar(tablaVentas);
        JButton verVenta = Estilo.botonSecundario("Ver detalle");
        JButton anular = Estilo.botonPeligro("Anular venta");
        JPanel botonesVentas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesVentas.setOpaque(false);
        botonesVentas.add(verVenta);
        if (admin) {
            botonesVentas.add(anular);
        }
        JPanel tabVentas = new JPanel(new BorderLayout(0, 10));
        tabVentas.setBorder(Estilo.borde(10, 0, 0, 0));
        tabVentas.setOpaque(false);
        tabVentas.add(Estilo.scroll(tablaVentas), BorderLayout.CENTER);
        tabVentas.add(botonesVentas, BorderLayout.SOUTH);

        // Compras
        modeloCompras.entero("No.", 60, c -> c.getIdCompra())
                .texto("Fecha", 130, c -> Formato.fechaHora(c.getFecha()))
                .texto("Proveedor", 220, c -> c.getProveedor())
                .texto("Registró", 110, c -> c.getUsuario().getNombreUsuario())
                .texto("Tipo", 80, c -> c.isEsCredito() ? "Crédito" : "Contado")
                .dinero("Total", 100, c -> c.getTotal());
        modeloCompras.aplicar(tablaCompras);
        JButton verCompra = Estilo.botonSecundario("Ver detalle");
        JPanel botonesCompras = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesCompras.setOpaque(false);
        botonesCompras.add(verCompra);
        JPanel tabCompras = new JPanel(new BorderLayout(0, 10));
        tabCompras.setBorder(Estilo.borde(10, 0, 0, 0));
        tabCompras.setOpaque(false);
        tabCompras.add(Estilo.scroll(tablaCompras), BorderLayout.CENTER);
        tabCompras.add(botonesCompras, BorderLayout.SOUTH);

        // Más vendidos
        modeloTop.texto("Producto", 300, r -> r[0])
                .cantidad("Cantidad vendida", 120, r -> r[1])
                .dinero("Total vendido", 120, r -> r[2]);
        JTable tablaTop = Estilo.tabla();
        modeloTop.aplicar(tablaTop);
        JPanel tabTop = new JPanel(new BorderLayout());
        tabTop.setBorder(Estilo.borde(10, 0, 0, 0));
        tabTop.setOpaque(false);
        tabTop.add(Estilo.scroll(tablaTop), BorderLayout.CENTER);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Ventas", tabVentas);
        pestanas.addTab("Productos más vendidos", tabTop);
        if (admin) {
            pestanas.addTab("Compras", tabCompras);
        }
        add(pestanas, BorderLayout.CENTER);

        // Eventos
        buscar.addActionListener(e -> refrescar());
        hoy.addActionListener(e -> {
            desde.setValue(Estilo.selectorFecha(LocalDate.now()).getValue());
            hasta.setValue(Estilo.selectorFecha(LocalDate.now()).getValue());
            refrescar();
        });
        mes.addActionListener(e -> {
            desde.setValue(Estilo.selectorFecha(LocalDate.now().withDayOfMonth(1)).getValue());
            hasta.setValue(Estilo.selectorFecha(LocalDate.now()).getValue());
            refrescar();
        });
        verVenta.addActionListener(e -> {
            Venta v = modeloVentas.seleccionado(tablaVentas);
            if (v == null) {
                Dialogos.aviso(this, "Seleccione una venta");
            } else {
                detalleVenta(this, v);
            }
        });
        verCompra.addActionListener(e -> {
            Compra c = modeloCompras.seleccionado(tablaCompras);
            if (c == null) {
                Dialogos.aviso(this, "Seleccione una compra");
            } else {
                detalleCompra(this, c);
            }
        });
        anular.addActionListener(e -> anular());
        tablaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && modeloVentas.seleccionado(tablaVentas) != null) {
                    detalleVenta(PanelReportes.this, modeloVentas.seleccionado(tablaVentas));
                }
            }
        });
    }

    @Override
    public void refrescar() {
        Dialogos.ejecutar(this, () -> {
            LocalDate d = Estilo.leerFecha(desde);
            LocalDate h = Estilo.leerFecha(hasta);
            if (h.isBefore(d)) {
                throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
            }
            List<Venta> ventas = ventaDAO.listarPorRango(d, h);
            modeloVentas.setFilas(ventas);

            double total = 0;
            double ganancia = 0;
            java.util.Map<Integer, Object[]> top = new java.util.HashMap<>();
            for (Venta v : ventas) {
                total += v.getTotal();
                for (DetalleVenta dv : v.getDetalles()) {
                    ganancia += (dv.getPrecioFinal() - dv.getProducto().getPrecioCompra()) * dv.getCantidad();
                    Object[] fila = top.get(dv.getProducto().getIdProducto());
                    if (fila == null) {
                        fila = new Object[]{dv.getProducto().toString(), 0.0, 0.0};
                        top.put(dv.getProducto().getIdProducto(), fila);
                    }
                    fila[1] = (Double) fila[1] + dv.getCantidad();
                    fila[2] = (Double) fila[2] + dv.getSubtotal();
                }
            }
            List<Object[]> filasTop = new ArrayList<>(top.values());
            filasTop.sort((a, b) -> Double.compare((Double) b[2], (Double) a[2]));
            modeloTop.setFilas(filasTop);

            lblVendido.setText(Formato.dinero(total));
            lblNumVentas.setText(String.valueOf(ventas.size()));
            lblGanancia.setText(Formato.dinero(ganancia));
            lblGanancia.setToolTipText("Precio de venta menos el costo actual de cada producto");

            if (Sesion.esAdministrador()) {
                List<Compra> compras = compraDAO.listarPorRango(d, h);
                modeloCompras.setFilas(compras);
                double comprado = 0;
                for (Compra c : compras) {
                    comprado += c.getTotal();
                }
                lblComprado.setText(Formato.dinero(comprado));
            } else {
                lblComprado.setText("—");
            }
        });
    }

    private void anular() {
        Venta v = modeloVentas.seleccionado(tablaVentas);
        if (v == null) {
            Dialogos.aviso(this, "Seleccione la venta que desea anular");
            return;
        }
        if (Dialogos.confirmar(this, "¿Anular la venta No. " + v.getIdVenta() + " por " + Formato.dinero(v.getTotal())
                + "?\n\nLos productos regresarán al inventario" + (v.isEsCredito() ? " y se eliminará su crédito." : "."))) {
            if (Dialogos.ejecutar(this, () -> ventaDAO.anular(v.getIdVenta()))) {
                Dialogos.info(this, "Venta anulada");
                refrescar();
            }
        }
    }

    private JPanel tarjeta(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Estilo.BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), Estilo.borde(12, 16, 12, 16)));
        p.add(Estilo.etiqueta(titulo));
        valor.setFont(valor.getFont().deriveFont(22f));
        p.add(valor);
        return p;
    }

    // ---------- Ventanas de detalle ----------

    public static void detalleVenta(Component padre, Venta v) {
        ModeloTabla<DetalleVenta> m = new ModeloTabla<>();
        m.texto("Producto", 260, d -> d.getProducto())
                .cantidad("Cantidad", 80, d -> d.getCantidad())
                .dinero("Precio", 90, d -> d.getPrecioFinal())
                .dinero("Subtotal", 100, d -> d.getSubtotal());
        m.setFilas(v.getDetalles());
        String cliente = v.getCliente() == null ? "Consumidor final" : v.getCliente().toString();
        mostrarDetalle(padre, "Venta No. " + v.getIdVenta(),
                Formato.fechaHora(v.getFecha()) + " · " + cliente + " · " + (v.isEsCredito() ? "Crédito" : "Contado")
                        + " · vendió " + v.getUsuario().getNombreUsuario(),
                m, v.getTotal());
    }

    public static void detalleCompra(Component padre, Compra c) {
        ModeloTabla<DetalleCompra> m = new ModeloTabla<>();
        m.texto("Producto", 260, d -> d.getProducto())
                .cantidad("Cantidad", 80, d -> d.getCantidad())
                .dinero("Costo", 90, d -> d.getPrecioCosto())
                .dinero("Subtotal", 100, d -> d.getSubtotal());
        m.setFilas(c.getDetalles());
        mostrarDetalle(padre, "Compra No. " + c.getIdCompra(),
                Formato.fechaHora(c.getFecha()) + " · " + c.getProveedor() + " · " + (c.isEsCredito() ? "Crédito" : "Contado"),
                m, c.getTotal());
    }

    private static void mostrarDetalle(Component padre, String titulo, String subtitulo, ModeloTabla<?> m, double total) {
        JDialog d = new JDialog(DialogoFormulario.ventanaDe(padre), titulo, JDialog.ModalityType.APPLICATION_MODAL);
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(Estilo.borde(16, 20, 16, 20));
        JPanel cabeza = new JPanel(new GridLayout(2, 1));
        cabeza.setOpaque(false);
        cabeza.add(Estilo.subtitulo(titulo));
        cabeza.add(Estilo.suave(subtitulo));
        raiz.add(cabeza, BorderLayout.NORTH);
        JTable t = Estilo.tabla();
        m.aplicar(t);
        raiz.add(Estilo.scroll(t), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JLabel lblTotal = Estilo.subtitulo("Total: " + Formato.dinero(total));
        pie.add(lblTotal, BorderLayout.WEST);
        JButton cerrar = Estilo.botonSecundario("Cerrar");
        cerrar.addActionListener(e -> d.dispose());
        pie.add(cerrar, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);
        d.setContentPane(raiz);
        d.setSize(620, 420);
        d.setLocationRelativeTo(d.getOwner());
        d.setVisible(true);
    }
}
