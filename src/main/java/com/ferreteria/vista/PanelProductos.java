package com.ferreteria.vista;

import com.ferreteria.dao.BajaInventarioDAO;
import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.dao.MarcaDAO;
import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.modelo.BajaInventario;
import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.ComponenteKit;
import com.ferreteria.modelo.Kit;
import com.ferreteria.modelo.Marca;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Catálogo de productos e inventario.
 * El vendedor solo puede consultar; el administrador puede crear, editar, dar de baja y desarmar kits.
 */
public class PanelProductos extends JPanel implements Pantalla {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final MarcaDAO marcaDAO = new MarcaDAO();

    private final JTextField buscador = Estilo.campo("Buscar por nombre, código o presentación");
    private final JComboBox<Object> filtroCategoria = new JComboBox<>();
    private final JComboBox<Object> filtroMarca = new JComboBox<>();
    private final JCheckBox soloBajoStock = new JCheckBox("Solo bajo stock");

    private final ModeloTabla<Producto> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();
    private List<Producto> todos = new ArrayList<>();

    // Detalle
    private final JLabel lblFoto = new JLabel("Sin foto", SwingConstants.CENTER);
    private final JLabel lblNombre = Estilo.subtitulo(" ");
    private final JLabel lblCodigo = Estilo.suave(" ");
    private final JLabel lblCompra = new JLabel();
    private final JLabel lblVenta = new JLabel();
    private final JLabel lblGanancia = new JLabel();
    private final JLabel lblExistencia = new JLabel();
    private final JLabel lblKit = Estilo.suave(" ");
    private final JButton btnDesarmar = Estilo.botonSecundario("Desarmar 1 kit");
    private final JButton btnBaja = Estilo.botonSecundario("Dar de baja (pérdida / daño)");
    private final JButton btnEditar = Estilo.botonSecundario("Editar");
    private final JButton btnEliminar = Estilo.botonPeligro("Desactivar");

    public PanelProductos() {
        super(new BorderLayout(0, 12));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));
        boolean admin = Sesion.esAdministrador();

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo("Productos"), BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        JButton btnNuevo = Estilo.botonPrimario("+ Nuevo producto");
        if (admin) {
            botones.add(btnEditar);
            botones.add(btnEliminar);
            botones.add(btnNuevo);
        }
        encabezado.add(botones, BorderLayout.EAST);

        // Filtros
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 10);
        g.weightx = 1;
        filtros.add(buscador, g);
        g.weightx = 0;
        filtroCategoria.setPreferredSize(new Dimension(180, 34));
        filtros.add(filtroCategoria, g);
        filtroMarca.setPreferredSize(new Dimension(160, 34));
        filtros.add(filtroMarca, g);
        soloBajoStock.setOpaque(false);
        g.insets = new Insets(0, 0, 0, 0);
        filtros.add(soloBajoStock, g);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(filtros, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        // Tabla
        modelo.texto("Código", 90, p -> p.getCodigo())
                .texto("Producto", 240, p -> p.toString() + (p.esKit() ? "  [KIT]" : ""))
                .texto("Marca", 100, p -> p.getMarca())
                .texto("Categoría", 110, p -> p.getCategoria())
                .dinero("Costo", 85, p -> p.getPrecioCompra())
                .dinero("Venta", 85, p -> p.getPrecioVenta())
                .cantidad("Exist.", 65, p -> p.getCantidadActual())
                .cantidad("Mín.", 55, p -> p.getExistenciaMinima())
                .colorTexto(p -> p.estaBajoStock() ? Estilo.PELIGRO : null);
        modelo.aplicar(tabla);

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        centro.add(crearDetalle(admin), BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        // Eventos
        buscador.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrar();
            }
        });
        filtroCategoria.addActionListener(e -> filtrar());
        filtroMarca.addActionListener(e -> filtrar());
        soloBajoStock.addActionListener(e -> filtrar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(modelo.seleccionado(tabla));
            }
        });
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (admin && e.getClickCount() == 2) {
                    editar();
                }
            }
        });
        btnNuevo.addActionListener(e -> {
            if (Dialogos.ejecutar(this, () -> DialogoProducto.mostrar(this, null))) {
                refrescar();
            }
        });
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnBaja.addActionListener(e -> darDeBaja());
        btnDesarmar.addActionListener(e -> desarmar());
    }

    private JPanel crearDetalle(boolean admin) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Estilo.tarjeta(p);
        p.setPreferredSize(new Dimension(290, 0));

        lblFoto.setOpaque(true);
        lblFoto.setBackground(new Color(0xF1F5F9));
        lblFoto.setForeground(Estilo.TEXTO_SUAVE);
        lblFoto.setBorder(BorderFactory.createDashedBorder(new Color(0xCBD5E1)));
        lblFoto.setPreferredSize(new Dimension(256, 150));
        lblFoto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        izq(p, lblFoto);
        p.add(Box.createVerticalStrut(12));
        izq(p, lblNombre);
        izq(p, lblCodigo);
        p.add(Box.createVerticalStrut(10));

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setOpaque(false);
        fila(datos, 0, "Precio de compra", lblCompra);
        fila(datos, 1, "Precio de venta", lblVenta);
        fila(datos, 2, "Ganancia", lblGanancia);
        fila(datos, 3, "Existencia / mínimo", lblExistencia);
        datos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        izq(p, datos);
        p.add(Box.createVerticalStrut(10));
        izq(p, lblKit);
        p.add(Box.createVerticalStrut(8));
        btnDesarmar.setForeground(Estilo.INFO);
        btnDesarmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        izq(p, btnDesarmar);
        p.add(Box.createVerticalGlue());
        btnBaja.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        izq(p, btnBaja);
        btnBaja.setVisible(admin);
        btnDesarmar.setVisible(false);
        return p;
    }

    @Override
    public void refrescar() {
        Producto anterior = modelo.seleccionado(tabla);
        Dialogos.ejecutar(this, () -> {
            todos = productoDAO.listar();
            recargarCombo(filtroCategoria, "Todas las categorías", new ArrayList<Object>(categoriaDAO.listar()));
            recargarCombo(filtroMarca, "Todas las marcas", new ArrayList<Object>(marcaDAO.listar()));
        });
        filtrar();
        if (anterior != null) {
            for (Producto p : modelo.getFilas()) {
                if (p.getIdProducto() == anterior.getIdProducto()) {
                    modelo.seleccionar(tabla, p);
                    break;
                }
            }
        }
    }

    private void filtrar() {
        String texto = buscador.getText().trim().toLowerCase();
        Object cat = filtroCategoria.getSelectedItem();
        Object mar = filtroMarca.getSelectedItem();
        List<Producto> lista = new ArrayList<>();
        for (Producto p : todos) {
            if (!texto.isEmpty()) {
                String hay = (p.getNombre() + " " + (p.getCodigo() == null ? "" : p.getCodigo()) + " "
                        + (p.getPresentacion() == null ? "" : p.getPresentacion())).toLowerCase();
                if (!hay.contains(texto)) {
                    continue;
                }
            }
            if (cat instanceof Categoria && (p.getCategoria() == null
                    || p.getCategoria().getIdCategoria() != ((Categoria) cat).getIdCategoria())) {
                continue;
            }
            if (mar instanceof Marca && (p.getMarca() == null
                    || p.getMarca().getIdMarca() != ((Marca) mar).getIdMarca())) {
                continue;
            }
            if (soloBajoStock.isSelected() && !p.estaBajoStock()) {
                continue;
            }
            lista.add(p);
        }
        modelo.setFilas(lista);
        mostrarDetalle(null);
    }

    private void mostrarDetalle(Producto p) {
        boolean hay = p != null;
        btnEditar.setEnabled(hay);
        btnEliminar.setEnabled(hay);
        btnBaja.setEnabled(hay);
        btnDesarmar.setVisible(hay && p.esKit() && Sesion.esAdministrador());
        if (!hay) {
            lblNombre.setText("Seleccione un producto");
            lblCodigo.setText(" ");
            lblCompra.setText("—");
            lblVenta.setText("—");
            lblGanancia.setText("—");
            lblExistencia.setText("—");
            lblKit.setText(" ");
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
            return;
        }
        lblNombre.setText("<html>" + p + "</html>");
        lblCodigo.setText((p.getCodigo() == null ? "Sin código" : p.getCodigo()) + " · " + p.getUnidad());
        lblCompra.setText(Formato.dinero(p.getPrecioCompra()));
        lblVenta.setText(Formato.dinero(p.getPrecioVenta()));
        lblGanancia.setText(Formato.dinero(p.getGanancia()));
        lblGanancia.setForeground(p.getGanancia() < 0 ? Estilo.PELIGRO : Estilo.EXITO);
        lblExistencia.setText(Formato.cantidad(p.getCantidadActual()) + " / " + Formato.cantidad(p.getExistenciaMinima()));
        lblExistencia.setForeground(p.estaBajoStock() ? Estilo.PELIGRO : Estilo.TEXTO);
        if (p.esKit()) {
            StringBuilder sb = new StringBuilder("<html><b>Contenido del kit:</b><br>");
            for (ComponenteKit c : ((Kit) p).getComponentes()) {
                sb.append("• ").append(c.getProducto()).append(" × ").append(c.getCantidadPiezas()).append("<br>");
            }
            lblKit.setText(sb.append("</html>").toString());
        } else {
            lblKit.setText(" ");
        }
        mostrarFoto(p.getFoto());
    }

    private void mostrarFoto(String ruta) {
        lblFoto.setIcon(null);
        lblFoto.setText("Sin foto");
        if (ruta == null || !new File(ruta).exists()) {
            return;
        }
        ImageIcon original = new ImageIcon(ruta);
        int w = original.getIconWidth();
        int h = original.getIconHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double escala = Math.min(250.0 / w, 144.0 / h);
        Image img = original.getImage().getScaledInstance((int) (w * escala), (int) (h * escala), Image.SCALE_SMOOTH);
        lblFoto.setIcon(new ImageIcon(img));
        lblFoto.setText(null);
    }

    // ---------- Acciones ----------

    private Producto seleccionado() {
        Producto p = modelo.seleccionado(tabla);
        if (p == null) {
            throw new IllegalStateException("Seleccione un producto de la tabla");
        }
        return p;
    }

    private void editar() {
        if (Dialogos.ejecutar(this, () -> DialogoProducto.mostrar(this, seleccionado()))) {
            refrescar();
        }
    }

    private void eliminar() {
        Producto p = modelo.seleccionado(tabla);
        if (p != null && Dialogos.confirmar(this, "¿Desactivar el producto \"" + p + "\"?\n"
                + "Ya no aparecerá en ventas, pero se conserva su historial.")) {
            if (Dialogos.ejecutar(this, () -> productoDAO.eliminar(p.getIdProducto()))) {
                refrescar();
            }
        }
    }

    private void darDeBaja() {
        if (Dialogos.ejecutar(this, () -> DialogoBaja.mostrar(this, seleccionado()))) {
            refrescar();
        }
    }

    private void desarmar() {
        Producto p = modelo.seleccionado(tabla);
        if (!(p instanceof Kit)) {
            return;
        }
        Kit kit = (Kit) p;
        StringBuilder sb = new StringBuilder("¿Desarmar 1 \"" + kit + "\"?\n\nSe sumarán al inventario:\n");
        for (ComponenteKit c : kit.getComponentes()) {
            sb.append("  • ").append(c.getCantidadPiezas()).append(" × ").append(c.getProducto()).append("\n");
        }
        if (Dialogos.confirmar(this, sb.toString())) {
            if (Dialogos.ejecutar(this, () -> productoDAO.desarmarKit(kit))) {
                Dialogos.info(this, "Kit desarmado. Las piezas ya se pueden vender sueltas.");
                refrescar();
            }
        }
    }

    // ---------- Ayudas ----------

    private static void recargarCombo(JComboBox<Object> combo, String todos, List<Object> items) {
        Object anterior = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(todos);
        for (Object o : items) {
            combo.addItem(o);
            if (anterior != null && o.toString().equals(anterior.toString())) {
                combo.setSelectedItem(o);
            }
        }
    }

    private static void fila(JPanel panel, int fila, String texto, JLabel valor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila;
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 0, 4, 8);
        panel.add(Estilo.suave(texto), g);
        g.gridx = 1;
        g.weightx = 0;
        g.anchor = GridBagConstraints.EAST;
        panel.add(valor, g);
    }

    private static void izq(JPanel panel, JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(c);
    }

    /**
     * Ventana para registrar una baja de inventario.
     */
    public static final class DialogoBaja {

        private DialogoBaja() {
        }

        public static boolean mostrar(java.awt.Component padre, Producto p) {
            DialogoFormulario d = new DialogoFormulario(padre, "Dar de baja");
            Formulario f = d.getFormulario();
            JTextField producto = f.agregar("Producto", new JTextField(p.toString(), 24));
            producto.setEditable(false);
            f.agregar("Existencia actual", Estilo.suave(Formato.cantidad(p.getCantidadActual()) + " " + p.getUnidad()));
            JTextField cantidad = f.agregar("Cantidad a dar de baja", Estilo.campoNumero("1"));
            JComboBox<String> motivo = f.agregar("Motivo", new JComboBox<>(new String[]{
                "Producto dañado", "Producto vencido", "Pérdida o robo", "Uso interno", "Error de inventario"}));
            motivo.setEditable(true);
            d.setTextoGuardar("Registrar baja");
            return d.mostrar(() -> {
                double cant = Estilo.leerNumero(cantidad, "la cantidad");
                String mot = motivo.getSelectedItem() == null ? "" : motivo.getSelectedItem().toString();
                new BajaInventarioDAO().registrar(new BajaInventario(p, cant, mot, Sesion.getUsuarioActual()));
            });
        }
    }
}
