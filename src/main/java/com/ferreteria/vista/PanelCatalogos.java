package com.ferreteria.vista;

import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.dao.MarcaDAO;
import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.Marca;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Categorías y marcas, una al lado de la otra.
 */
public class PanelCatalogos extends JPanel implements Pantalla {

    private final PanelCategorias categorias = new PanelCategorias();
    private final PanelMarcas marcas = new PanelMarcas();

    public PanelCatalogos() {
        super(new BorderLayout(0, 16));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.borde(20, 24, 20, 24));
        add(Estilo.titulo("Categorías y marcas"), BorderLayout.NORTH);
        JPanel dos = new JPanel(new GridLayout(1, 2, 20, 0));
        dos.setOpaque(false);
        dos.add(categorias);
        dos.add(marcas);
        add(dos, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        categorias.refrescar();
        marcas.refrescar();
    }

    // ---------- Categorías ----------

    static class PanelCategorias extends PanelCrud<Categoria> {

        private final CategoriaDAO dao = new CategoriaDAO();

        PanelCategorias() {
            super("Categorías", "+ Nueva", true);
        }

        @Override
        protected void definirColumnas(ModeloTabla<Categoria> m) {
            m.texto("Nombre", 160, c -> c.getNombre())
                    .texto("Descripción", 200, c -> c.getDescripcion())
                    .texto("Estado", 70, c -> c.isActiva() ? "Activa" : "Inactiva")
                    .colorTexto(c -> c.isActiva() ? null : Estilo.TEXTO_SUAVE);
        }

        @Override
        protected List<Categoria> cargar(String filtro) throws Exception {
            List<Categoria> lista = new ArrayList<>();
            for (Categoria c : dao.listar()) {
                if (contiene(c.getNombre() + " " + c.getDescripcion(), filtro)) {
                    lista.add(c);
                }
            }
            return lista;
        }

        @Override
        protected void nuevo() throws Exception {
            formulario(null);
        }

        @Override
        protected void editar(Categoria c) throws Exception {
            formulario(c);
        }

        @Override
        protected void eliminar(Categoria c) throws Exception {
            dao.eliminar(c.getIdCategoria());
        }

        private void formulario(Categoria existente) {
            DialogoFormulario d = new DialogoFormulario(this, existente == null ? "Nueva categoría" : "Editar categoría");
            JTextField nombre = d.getFormulario().agregar("Nombre *", new JTextField(22));
            JTextField descripcion = d.getFormulario().agregar("Descripción", new JTextField());
            JCheckBox activa = d.getFormulario().agregar("Estado", new JCheckBox("Activa", true));
            activa.setOpaque(false);
            if (existente != null) {
                nombre.setText(existente.getNombre());
                descripcion.setText(existente.getDescripcion());
                activa.setSelected(existente.isActiva());
            }
            d.mostrar(() -> {
                Categoria c = existente == null ? new Categoria() : existente;
                c.setNombre(nombre.getText());
                c.setDescripcion(PanelClientes.vacioANull(descripcion.getText()));
                c.setActiva(activa.isSelected());
                if (existente == null) {
                    dao.insertar(c);
                } else {
                    dao.actualizar(c);
                }
            });
        }
    }

    // ---------- Marcas ----------

    static class PanelMarcas extends PanelCrud<Marca> {

        private final MarcaDAO dao = new MarcaDAO();

        PanelMarcas() {
            super("Marcas", "+ Nueva", true);
        }

        @Override
        protected void definirColumnas(ModeloTabla<Marca> m) {
            m.texto("Nombre", 160, x -> x.getNombre())
                    .texto("Descripción", 200, x -> x.getDescripcion())
                    .texto("Estado", 70, x -> x.isActiva() ? "Activa" : "Inactiva")
                    .colorTexto(x -> x.isActiva() ? null : Estilo.TEXTO_SUAVE);
        }

        @Override
        protected List<Marca> cargar(String filtro) throws Exception {
            List<Marca> lista = new ArrayList<>();
            for (Marca x : dao.listar()) {
                if (contiene(x.getNombre() + " " + x.getDescripcion(), filtro)) {
                    lista.add(x);
                }
            }
            return lista;
        }

        @Override
        protected void nuevo() throws Exception {
            formulario(null);
        }

        @Override
        protected void editar(Marca x) throws Exception {
            formulario(x);
        }

        @Override
        protected void eliminar(Marca x) throws Exception {
            dao.eliminar(x.getIdMarca());
        }

        private void formulario(Marca existente) {
            DialogoFormulario d = new DialogoFormulario(this, existente == null ? "Nueva marca" : "Editar marca");
            JTextField nombre = d.getFormulario().agregar("Nombre *", new JTextField(22));
            JTextField descripcion = d.getFormulario().agregar("Descripción", new JTextField());
            JCheckBox activa = d.getFormulario().agregar("Estado", new JCheckBox("Activa", true));
            activa.setOpaque(false);
            if (existente != null) {
                nombre.setText(existente.getNombre());
                descripcion.setText(existente.getDescripcion());
                activa.setSelected(existente.isActiva());
            }
            d.mostrar(() -> {
                Marca x = existente == null ? new Marca() : existente;
                x.setNombre(nombre.getText());
                x.setDescripcion(PanelClientes.vacioANull(descripcion.getText()));
                x.setActiva(activa.isSelected());
                if (existente == null) {
                    dao.insertar(x);
                } else {
                    dao.actualizar(x);
                }
            });
        }
    }
}
