package com.ferreteria.vista;

import com.ferreteria.dao.BajaInventarioDAO;
import com.ferreteria.modelo.BajaInventario;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Formato;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;

/**
 * Historial de bajas de inventario (productos dañados, vencidos, perdidos).
 */
public class PanelBajas extends JPanel implements Pantalla {

    private final BajaInventarioDAO bajaDAO = new BajaInventarioDAO();
    private final ModeloTabla<BajaInventario> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();
    private final JSpinner desde = Estilo.selectorFecha(LocalDate.now().withDayOfMonth(1));
    private final JSpinner hasta = Estilo.selectorFecha(LocalDate.now());
    private final JLabel lblResumen = Estilo.suave(" ");

    public PanelBajas() {
        super(new BorderLayout(0, 12));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo("Bajas de inventario"), BorderLayout.WEST);
        JButton nueva = Estilo.botonPrimario("+ Registrar baja");
        encabezado.add(nueva, BorderLayout.EAST);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(Estilo.etiqueta("Desde"));
        filtros.add(desde);
        filtros.add(Estilo.etiqueta("Hasta"));
        filtros.add(hasta);
        JButton buscar = Estilo.botonOscuro("Buscar");
        filtros.add(buscar);
        filtros.add(lblResumen);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(filtros, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        modelo.texto("Fecha", 130, b -> Formato.fechaHora(b.getFecha()))
                .texto("Producto", 260, b -> b.getProducto())
                .cantidad("Cantidad", 80, b -> b.getCantidad())
                .dinero("Costo perdido", 110, b -> b.getCantidad() * b.getProducto().getPrecioCompra())
                .texto("Motivo", 200, b -> b.getMotivo())
                .texto("Registró", 120, b -> b.getUsuario().getNombreUsuario());
        modelo.aplicar(tabla);
        add(Estilo.scroll(tabla), BorderLayout.CENTER);

        buscar.addActionListener(e -> refrescar());
        nueva.addActionListener(e -> Dialogos.ejecutar(this, () -> {
            Producto p = DialogoBuscarProducto.elegir(this, "", false);
            if (p != null && PanelProductos.DialogoBaja.mostrar(this, p)) {
                refrescar();
            }
        }));
    }

    @Override
    public void refrescar() {
        Dialogos.ejecutar(this, () -> {
            List<BajaInventario> lista = bajaDAO.listarPorRango(Estilo.leerFecha(desde), Estilo.leerFecha(hasta));
            modelo.setFilas(lista);
            double costo = 0;
            for (BajaInventario b : lista) {
                costo += b.getCantidad() * b.getProducto().getPrecioCompra();
            }
            lblResumen.setText("   " + lista.size() + " bajas · costo aproximado " + Formato.dinero(costo));
        });
    }
}
