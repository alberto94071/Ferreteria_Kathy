package com.ferreteria.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Pantalla de mantenimiento genérica: título, botones Nuevo / Editar / Eliminar,
 * buscador y tabla. Clientes, proveedores, usuarios, categorías y marcas la usan.
 *
 * Cada hija solo define las columnas y qué hacer en cada botón.
 */
public abstract class PanelCrud<T> extends JPanel implements Pantalla {

    protected final ModeloTabla<T> modelo = new ModeloTabla<>();
    protected final JTable tabla = Estilo.tabla();
    protected final JTextField buscador = Estilo.campo("Buscar...");
    protected final JPanel barraBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    protected final JButton btnNuevo;
    protected final JButton btnEditar = Estilo.botonSecundario("Editar");
    protected final JButton btnEliminar = Estilo.botonPeligro("Desactivar");

    protected PanelCrud(String titulo, String textoNuevo, boolean compacto) {
        super(new BorderLayout(0, 12));
        setBackground(Estilo.FONDO);
        setBorder(compacto ? Estilo.borde(0, 0, 0, 0) : Estilo.borde(20, 24, 20, 24));

        btnNuevo = Estilo.botonPrimario(textoNuevo);
        barraBotones.setOpaque(false);
        barraBotones.add(btnEditar);
        barraBotones.add(btnEliminar);
        barraBotones.add(btnNuevo);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(compacto ? Estilo.subtitulo(titulo) : Estilo.titulo(titulo), BorderLayout.WEST);
        encabezado.add(barraBotones, BorderLayout.EAST);

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(encabezado, BorderLayout.NORTH);
        arriba.add(buscador, BorderLayout.SOUTH);

        add(arriba, BorderLayout.NORTH);
        add(Estilo.scroll(tabla), BorderLayout.CENTER);

        definirColumnas(modelo);
        modelo.aplicar(tabla);

        btnNuevo.addActionListener(e -> {
            if (Dialogos.ejecutar(this, this::nuevo)) {
                refrescar();
            }
        });
        btnEditar.addActionListener(e -> accionEditar());
        btnEliminar.addActionListener(e -> accionEliminar());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    accionEditar();
                }
            }
        });
        buscador.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refrescar();
            }
        });
    }

    // ---------- Lo que define cada pantalla ----------

    protected abstract void definirColumnas(ModeloTabla<T> m);

    // Devuelve los registros que coinciden con el texto del buscador ("" = todos)
    protected abstract List<T> cargar(String filtro) throws Exception;

    protected abstract void nuevo() throws Exception;

    protected abstract void editar(T registro) throws Exception;

    protected abstract void eliminar(T registro) throws Exception;

    protected String preguntaEliminar(T registro) {
        return "¿Desactivar \"" + registro + "\"?";
    }

    // ---------- Comportamiento común ----------

    @Override
    public void refrescar() {
        T anterior = modelo.seleccionado(tabla);
        Dialogos.ejecutar(this, () -> modelo.setFilas(cargar(buscador.getText().trim())));
        if (anterior != null) {
            modelo.seleccionar(tabla, anterior);
        }
    }

    protected T seleccionado() {
        T t = modelo.seleccionado(tabla);
        if (t == null) {
            throw new IllegalStateException("Seleccione un registro de la tabla");
        }
        return t;
    }

    private void accionEditar() {
        Dialogos.ejecutar(this, () -> editar(seleccionado()));
        refrescar(); // siempre recarga, por si se canceló a medio editar
    }

    private void accionEliminar() {
        T t = modelo.seleccionado(tabla);
        if (t == null) {
            Dialogos.aviso(this, "Seleccione un registro de la tabla");
            return;
        }
        if (Dialogos.confirmar(this, preguntaEliminar(t))) {
            if (Dialogos.ejecutar(this, () -> eliminar(t))) {
                refrescar();
            }
        }
    }

    // Ayuda para filtrar en memoria
    protected static boolean contiene(String texto, String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            return true;
        }
        return texto != null && texto.toLowerCase().contains(filtro.toLowerCase());
    }
}
