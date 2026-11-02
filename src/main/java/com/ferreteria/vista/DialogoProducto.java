package com.ferreteria.vista;

import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.dao.MarcaDAO;
import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.ComponenteKit;
import com.ferreteria.modelo.Kit;
import com.ferreteria.modelo.Marca;
import com.ferreteria.modelo.Producto;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Formulario para crear o editar un producto (o un kit con sus componentes).
 */
public final class DialogoProducto {

    private static final String[] UNIDADES = {
        "unidad", "libra", "kilo", "quintal", "metro", "pie", "yarda", "galón", "litro",
        "caja", "saco", "rollo", "par", "juego", "bolsa", "cubeta"
    };

    private DialogoProducto() {
    }

    /**
     * @param existente el producto a editar, o null para uno nuevo
     * @return true si se guardó
     */
    public static boolean mostrar(Component padre, Producto existente) throws Exception {
        boolean nuevo = existente == null;
        ProductoDAO productoDAO = new ProductoDAO();
        DialogoFormulario d = new DialogoFormulario(padre, nuevo ? "Nuevo producto" : "Editar producto");
        Formulario f = d.getFormulario();

        JTextField codigo = f.agregar("Código", new JTextField(24));
        codigo.putClientProperty("JTextField.placeholderText", "Opcional (código de barras)");
        JTextField nombre = f.agregar("Nombre *", new JTextField());
        JComboBox<String> unidad = f.agregar("Unidad de medida *", new JComboBox<>(UNIDADES));
        unidad.setEditable(true);
        JTextField presentacion = f.agregar("Presentación", new JTextField());
        presentacion.putClientProperty("JTextField.placeholderText", "Ej.: 2 pulgadas, 16 oz, 3 m");

        JComboBox<Object> marca = f.agregar("Marca", new JComboBox<>());
        marca.addItem("(Sin marca)");
        for (Marca m : new MarcaDAO().listarActivas()) {
            marca.addItem(m);
        }
        JComboBox<Object> categoria = f.agregar("Categoría", new JComboBox<>());
        categoria.addItem("(Sin categoría)");
        for (Categoria c : new CategoriaDAO().listar()) {
            if (c.isActiva()) {
                categoria.addItem(c);
            }
        }

        JPanel precios = new JPanel(new GridLayout(1, 2, 8, 0));
        precios.setOpaque(false);
        JTextField precioCompra = Estilo.campoNumero("0.00");
        JTextField precioVenta = Estilo.campoNumero("0.00");
        precios.add(precioCompra);
        precios.add(precioVenta);
        f.agregar("Precio compra / venta (Q)", precios);

        JPanel existencias = new JPanel(new GridLayout(1, 2, 8, 0));
        existencias.setOpaque(false);
        JTextField existencia = Estilo.campoNumero("0");
        JTextField minimo = Estilo.campoNumero("1");
        existencias.add(existencia);
        existencias.add(minimo);
        f.agregar("Existencia / mínimo", existencias);

        JTextField foto = new JTextField();
        JButton elegirFoto = Estilo.botonSecundario("Elegir...");
        JPanel panelFoto = new JPanel(new BorderLayout(8, 0));
        panelFoto.setOpaque(false);
        panelFoto.add(foto, BorderLayout.CENTER);
        panelFoto.add(elegirFoto, BorderLayout.EAST);
        f.agregar("Foto", panelFoto);

        JCheckBox esKit = new JCheckBox("Es un kit (contiene otros productos que se pueden vender sueltos)");
        esKit.setOpaque(false);
        f.agregarCompleto(esKit, false);

        // ---- Componentes del kit ----
        List<ComponenteKit> componentes = new ArrayList<>();
        ModeloTabla<ComponenteKit> modeloKit = new ModeloTabla<>();
        modeloKit.texto("Producto", 220, c -> c.getProducto().toString())
                .entero("Piezas", 70, c -> c.getCantidadPiezas())
                .cantidad("Peso del costo", 100, c -> c.getPeso());
        JTable tablaKit = Estilo.tabla();
        modeloKit.aplicar(tablaKit);
        tablaKit.setPreferredScrollableViewportSize(new Dimension(400, 120));
        JButton agregarComp = Estilo.botonSecundario("+ Agregar componente");
        JButton quitarComp = Estilo.botonPeligro("Quitar");
        JPanel botonesKit = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesKit.setOpaque(false);
        botonesKit.add(agregarComp);
        botonesKit.add(quitarComp);
        JPanel panelKit = new JPanel(new BorderLayout(0, 6));
        panelKit.setOpaque(false);
        panelKit.add(Estilo.etiqueta("Componentes del kit (el peso reparte el costo del kit entre las piezas)"),
                BorderLayout.NORTH);
        panelKit.add(Estilo.scroll(tablaKit), BorderLayout.CENTER);
        panelKit.add(botonesKit, BorderLayout.SOUTH);
        f.agregarCompleto(panelKit, true);

        // ---- Cargar datos si es edición ----
        if (!nuevo) {
            codigo.setText(existente.getCodigo() == null ? "" : existente.getCodigo());
            nombre.setText(existente.getNombre());
            unidad.setSelectedItem(existente.getUnidad());
            presentacion.setText(existente.getPresentacion() == null ? "" : existente.getPresentacion());
            seleccionar(marca, existente.getMarca());
            seleccionar(categoria, existente.getCategoria());
            precioCompra.setText(Estilo.numeroPlano(existente.getPrecioCompra()));
            precioVenta.setText(Estilo.numeroPlano(existente.getPrecioVenta()));
            existencia.setText(Estilo.numeroCorto(existente.getCantidadActual()));
            existencia.setEnabled(false);
            existencia.setToolTipText("La existencia cambia con compras, ventas y bajas");
            minimo.setText(Estilo.numeroCorto(existente.getExistenciaMinima()));
            foto.setText(existente.getFoto() == null ? "" : existente.getFoto());
            esKit.setSelected(existente.esKit());
            esKit.setEnabled(false);
            if (existente.esKit()) {
                componentes.addAll(((Kit) existente).getComponentes());
                modeloKit.setFilas(componentes);
            }
        }
        panelKit.setVisible(esKit.isSelected());

        // ---- Acciones ----
        esKit.addActionListener(e -> {
            panelKit.setVisible(esKit.isSelected());
            d.pack();
        });
        elegirFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                foto.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        agregarComp.addActionListener(e -> Dialogos.ejecutar(d, () -> {
            Producto p = DialogoBuscarProducto.elegir(d, "", true);
            if (p == null) {
                return;
            }
            if (!nuevo && p.getIdProducto() == existente.getIdProducto()) {
                throw new IllegalArgumentException("Un kit no puede contenerse a sí mismo");
            }
            for (ComponenteKit c : componentes) {
                if (c.getProducto().getIdProducto() == p.getIdProducto()) {
                    throw new IllegalArgumentException(p + " ya está en el kit");
                }
            }
            JTextField piezas = Estilo.campoNumero("1");
            JTextField peso = Estilo.campoNumero("1");
            Formulario mini = new Formulario();
            mini.agregar("Piezas que trae", piezas);
            mini.agregar("Peso del costo", peso);
            mini.agregarCompleto(Estilo.suave("<html>Peso: qué parte del costo del kit corresponde a este<br>"
                    + "producto. Si todos tienen 1, el costo se reparte igual.</html>"), false);
            int r = JOptionPane.showConfirmDialog(d, mini, "Componente: " + p, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }
            componentes.add(new ComponenteKit(p, Estilo.leerEntero(piezas, "las piezas"),
                    Estilo.leerNumero(peso, "el peso")));
            modeloKit.setFilas(componentes);
        }));
        quitarComp.addActionListener(e -> {
            ComponenteKit c = modeloKit.seleccionado(tablaKit);
            if (c != null) {
                componentes.remove(c);
                modeloKit.setFilas(componentes);
            }
        });

        return d.mostrar(() -> {
            String cod = codigo.getText().trim();
            String nom = nombre.getText();
            String uni = unidad.getSelectedItem() == null ? "" : unidad.getSelectedItem().toString();
            String pres = presentacion.getText().trim().isEmpty() ? null : presentacion.getText().trim();
            Marca m = marca.getSelectedItem() instanceof Marca ? (Marca) marca.getSelectedItem() : null;
            Categoria c = categoria.getSelectedItem() instanceof Categoria ? (Categoria) categoria.getSelectedItem() : null;
            double pc = Estilo.leerNumero(precioCompra, "el precio de compra");
            double pv = Estilo.leerNumero(precioVenta, "el precio de venta");
            double min = Estilo.leerNumero(minimo, "la existencia mínima");
            String rutaFoto = foto.getText().trim().isEmpty() ? null : foto.getText().trim();
            if (rutaFoto != null && !new File(rutaFoto).exists()) {
                throw new IllegalArgumentException("No se encontró el archivo de la foto");
            }
            if (esKit.isSelected() && componentes.isEmpty()) {
                throw new IllegalArgumentException("Agregue al menos un componente al kit");
            }
            if (pv < pc && !Dialogos.confirmar(d, "El precio de venta es menor que el de compra. ¿Guardar de todos modos?")) {
                throw new IllegalStateException("Revise los precios");
            }

            if (nuevo) {
                double ex = Estilo.leerNumero(existencia, "la existencia");
                Producto p;
                if (esKit.isSelected()) {
                    Kit k = new Kit(cod, nom, uni, pres, m, c, pc, pv, ex, min, rutaFoto);
                    for (ComponenteKit comp : componentes) {
                        k.agregarComponente(comp);
                    }
                    p = k;
                } else {
                    p = new Producto(cod, nom, uni, pres, m, c, pc, pv, ex, min, rutaFoto);
                }
                productoDAO.insertar(p);
            } else {
                existente.setCodigo(cod);
                existente.setNombre(nom);
                existente.setUnidad(uni);
                existente.setPresentacion(pres);
                existente.setMarca(m);
                existente.setCategoria(c);
                existente.setPrecioCompra(pc);
                existente.setPrecioVenta(pv);
                existente.setExistenciaMinima(min);
                existente.setFoto(rutaFoto);
                productoDAO.actualizar(existente);
                if (existente.esKit()) {
                    ((Kit) existente).setComponentes(new ArrayList<>(componentes));
                    productoDAO.guardarComponentes((Kit) existente);
                }
            }
        });
    }

    // Selecciona la marca o categoría actual; si está desactivada (no aparece en la lista) la agrega
    private static void seleccionar(JComboBox<Object> combo, Object actual) {
        if (actual == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (mismoId(combo.getItemAt(i), actual)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.addItem(actual);
        combo.setSelectedItem(actual);
    }

    private static boolean mismoId(Object a, Object b) {
        if (a instanceof Marca && b instanceof Marca) {
            return ((Marca) a).getIdMarca() == ((Marca) b).getIdMarca();
        }
        if (a instanceof Categoria && b instanceof Categoria) {
            return ((Categoria) a).getIdCategoria() == ((Categoria) b).getIdCategoria();
        }
        return false;
    }
}
