package com.ferreteria.vista;

import com.ferreteria.dao.CreditoDAO;
import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.dao.VentaDAO;
import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Pantalla de inicio: resumen del día.
 */
public class PanelInicio extends JPanel implements Pantalla {

    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CreditoDAO creditoDAO = new CreditoDAO();

    private final JLabel lblVendido = Estilo.grande("Q 0.00");
    private final JLabel lblVentas = Estilo.grande("0");
    private final JLabel lblBajoStock = Estilo.grande("0");
    private final JLabel lblVencidos = Estilo.grande("0");
    private final JLabel lblFecha = Estilo.suave("");

    private final ModeloTabla<Producto> modeloStock = new ModeloTabla<>();
    private final ModeloTabla<Credito> modeloCreditos = new ModeloTabla<>();

    public PanelInicio(VentanaPrincipal ventana) {
        super(new BorderLayout(0, 20));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(24, 28, 24, 28));

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JPanel saludo = new JPanel();
        saludo.setLayout(new BoxLayout(saludo, BoxLayout.Y_AXIS));
        saludo.setOpaque(false);
        saludo.add(Estilo.titulo("Bienvenido, " + Sesion.getUsuarioActual().getNombre()));
        saludo.add(lblFecha);
        encabezado.add(saludo, BorderLayout.WEST);
        JButton nuevaVenta = Estilo.alto(Estilo.botonPrimario("+ Nueva venta (F2)"), 40);
        nuevaVenta.addActionListener(e -> ventana.mostrar(VentanaPrincipal.VENTAS));
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(nuevaVenta, BorderLayout.SOUTH);
        encabezado.add(derecha, BorderLayout.EAST);

        // Tarjetas
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta("Vendido hoy", lblVendido, Estilo.BORDE, Estilo.TEXTO_SUAVE));
        tarjetas.add(tarjeta("Ventas hoy", lblVentas, Estilo.BORDE, Estilo.TEXTO_SUAVE));
        tarjetas.add(tarjeta("Productos bajo stock", lblBajoStock, new Color(0xFED7AA), Estilo.ALERTA));
        tarjetas.add(tarjeta("Créditos vencidos", lblVencidos, new Color(0xFECACA), Estilo.PELIGRO));

        JPanel arriba = new JPanel(new BorderLayout(0, 20));
        arriba.setOpaque(false);
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(tarjetas, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        // Tablas
        modeloStock.texto("Producto", 260, p -> p.toString())
                .cantidad("Existencia", 90, p -> p.getCantidadActual())
                .cantidad("Mínimo", 90, p -> p.getExistenciaMinima())
                .colorTexto(p -> p.getCantidadActual() <= 0 ? Estilo.PELIGRO : null);
        modeloCreditos.texto("Cliente", 170, c -> c.getVenta().getCliente())
                .texto("Vence", 150, c -> Formato.fecha(c.getFechaVencimiento()) + (c.estaVencido() ? " · vencido" : ""))
                .dinero("Saldo", 100, c -> c.getSaldo())
                .colorTexto(c -> c.estaVencido() ? Estilo.PELIGRO : null);

        JPanel tablas = new JPanel(new GridLayout(1, 2, 16, 0));
        tablas.setOpaque(false);
        boolean admin = Sesion.esAdministrador();
        tablas.add(seccion("Productos por agotarse", admin ? "Ir a compras" : "Ver productos", modeloStock,
                () -> ventana.mostrar(admin ? VentanaPrincipal.COMPRAS : VentanaPrincipal.PRODUCTOS)));
        tablas.add(seccion("Créditos por cobrar", "Ver créditos", modeloCreditos,
                () -> ventana.mostrar(VentanaPrincipal.CREDITOS)));
        add(tablas, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        LocalDate hoy = LocalDate.now();
        String dia = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es"));
        lblFecha.setText(Character.toUpperCase(dia.charAt(0)) + dia.substring(1) + " "
                + hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Dialogos.ejecutar(this, () -> {
            lblVendido.setText(Formato.dinero(ventaDAO.totalVendido(hoy, hoy)));
            lblVentas.setText(String.valueOf(ventaDAO.listarPorRango(hoy, hoy).size()));
            List<Producto> bajo = productoDAO.listarBajoStock();
            lblBajoStock.setText(String.valueOf(bajo.size()));
            modeloStock.setFilas(bajo);
            List<Credito> pendientes = creditoDAO.listarPendientes();
            int vencidos = 0;
            for (Credito c : pendientes) {
                if (c.estaVencido()) {
                    vencidos++;
                }
            }
            lblVencidos.setText(String.valueOf(vencidos));
            modeloCreditos.setFilas(pendientes);
        });
    }

    private JPanel tarjeta(String titulo, JLabel valor, Color borde, Color colorTitulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Estilo.BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde), Estilo.borde(14, 16, 14, 16)));
        JLabel t = Estilo.etiqueta(titulo);
        t.setForeground(colorTitulo);
        p.add(t);
        valor.setForeground(colorTitulo == Estilo.TEXTO_SUAVE ? Estilo.TEXTO : colorTitulo);
        p.add(valor);
        return p;
    }

    private JPanel seccion(String titulo, String textoEnlace, ModeloTabla<?> modelo, Runnable accion) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        Estilo.tarjeta(p);
        JPanel cabeza = new JPanel(new BorderLayout());
        cabeza.setOpaque(false);
        cabeza.add(Estilo.subtitulo(titulo), BorderLayout.WEST);
        JButton enlace = Estilo.botonSecundario(textoEnlace);
        enlace.addActionListener(e -> accion.run());
        cabeza.add(enlace, BorderLayout.EAST);
        p.add(cabeza, BorderLayout.NORTH);
        JTable t = Estilo.tabla();
        modelo.aplicar(t);
        p.add(Estilo.scroll(t), BorderLayout.CENTER);
        return p;
    }
}
