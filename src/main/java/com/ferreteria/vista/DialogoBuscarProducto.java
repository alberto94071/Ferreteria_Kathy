package com.ferreteria.vista;

import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.modelo.Producto;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Ventana para elegir un producto de una lista con buscador.
 */
public class DialogoBuscarProducto extends JDialog {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ModeloTabla<Producto> modelo = new ModeloTabla<>();
    private final JTable tabla = Estilo.tabla();
    private final JTextField buscador = Estilo.campo("Buscar por nombre o código");
    private final boolean excluirKits;
    private Producto elegido;

    private DialogoBuscarProducto(Component padre, String texto, boolean excluirKits) {
        super(DialogoFormulario.ventanaDe(padre), "Buscar producto", ModalityType.APPLICATION_MODAL);
        this.excluirKits = excluirKits;

        modelo.texto("Código", 90, p -> p.getCodigo())
                .texto("Producto", 260, p -> p.toString() + (p.esKit() ? "  [KIT]" : ""))
                .texto("Marca", 100, p -> p.getMarca())
                .cantidad("Existencia", 80, p -> p.getCantidadActual())
                .dinero("Precio", 90, p -> p.getPrecioVenta())
                .colorTexto(p -> p.estaBajoStock() ? Estilo.PELIGRO : null);
        modelo.aplicar(tabla);

        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBorder(Estilo.borde(16, 16, 16, 16));
        raiz.add(buscador, BorderLayout.NORTH);
        raiz.add(Estilo.scroll(tabla), BorderLayout.CENTER);

        JButton aceptar = Estilo.botonPrimario("Elegir");
        JButton cancelar = Estilo.botonSecundario("Cancelar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(cancelar);
        botones.add(aceptar);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        buscador.setText(texto == null ? "" : texto);
        buscador.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && tabla.getRowCount() > 0) {
                    tabla.requestFocus();
                    tabla.setRowSelectionInterval(0, 0);
                } else if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    cargar();
                }
            }
        });
        buscador.addActionListener(e -> {
            if (tabla.getRowCount() > 0) {
                if (tabla.getSelectedRow() < 0) {
                    tabla.setRowSelectionInterval(0, 0);
                }
                aceptar();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    aceptar();
                }
            }
        });
        tabla.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "elegir");
        tabla.getActionMap().put("elegir", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                aceptar();
            }
        });
        aceptar.addActionListener(e -> aceptar());
        cancelar.addActionListener(e -> dispose());
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        cargar();
        setSize(720, 460);
        setLocationRelativeTo(getOwner());
    }

    /**
     * Muestra la ventana y devuelve el producto elegido, o null si se canceló.
     * @param excluirKits true para no mostrar kits (al armar un kit)
     */
    public static Producto elegir(Component padre, String texto, boolean excluirKits) {
        DialogoBuscarProducto d = new DialogoBuscarProducto(padre, texto, excluirKits);
        d.setVisible(true);
        return d.elegido;
    }

    /**
     * Busca lo que se escribió en un campo: primero por código exacto (lector de barras),
     * si hay un solo resultado lo devuelve directo, si hay varios abre la ventana para elegir.
     */
    public static Producto resolver(Component padre, String texto) throws java.sql.SQLException {
        String t = texto == null ? "" : texto.trim();
        if (!t.isEmpty()) {
            Producto porCodigo = new ProductoDAO().buscarPorCodigo(t);
            if (porCodigo != null && porCodigo.isActivo()) {
                return porCodigo;
            }
            List<Producto> lista = new ProductoDAO().buscar(t);
            if (lista.size() == 1) {
                return lista.get(0);
            }
        }
        return elegir(padre, t, false);
    }

    private void cargar() {
        Dialogos.ejecutar(this, () -> {
            List<Producto> lista = productoDAO.buscar(buscador.getText().trim());
            if (excluirKits) {
                List<Producto> sinKits = new ArrayList<>();
                for (Producto p : lista) {
                    if (!p.esKit()) {
                        sinKits.add(p);
                    }
                }
                lista = sinKits;
            }
            modelo.setFilas(lista);
        });
    }

    private void aceptar() {
        Producto p = modelo.seleccionado(tabla);
        if (p == null) {
            Dialogos.aviso(this, "Seleccione un producto");
            return;
        }
        elegido = p;
        dispose();
    }
}
