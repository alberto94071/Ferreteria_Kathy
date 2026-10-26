package com.ferreteria.vista;

import com.ferreteria.util.Formato;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Modelo de tabla genérico: se le dice qué columnas tiene y cómo sacar
 * cada valor del objeto, y él se encarga del resto.
 *
 * Ejemplo:
 *   ModeloTabla<Cliente> m = new ModeloTabla<>();
 *   m.texto("Nombre", 200, c -> c.getNombre())
 *    .dinero("Límite", 100, c -> c.getLimiteCredito());
 *   m.aplicar(tabla);
 *   m.setFilas(clienteDAO.listar());
 */
public class ModeloTabla<T> extends AbstractTableModel {

    public enum Tipo { TEXTO, DINERO, CANTIDAD, ENTERO }

    private static class Columna<T> {
        String nombre;
        Tipo tipo;
        int ancho;
        Function<T, Object> valor;
    }

    private final List<Columna<T>> columnas = new ArrayList<>();
    private List<T> filas = new ArrayList<>();
    private Function<T, Color> colorTexto;
    private Function<T, Color> colorFondo;

    // ---------- Definición de columnas ----------

    public ModeloTabla<T> columna(String nombre, Tipo tipo, int ancho, Function<T, Object> valor) {
        Columna<T> c = new Columna<>();
        c.nombre = nombre;
        c.tipo = tipo;
        c.ancho = ancho;
        c.valor = valor;
        columnas.add(c);
        return this;
    }

    public ModeloTabla<T> texto(String nombre, int ancho, Function<T, Object> valor) {
        return columna(nombre, Tipo.TEXTO, ancho, valor);
    }

    public ModeloTabla<T> dinero(String nombre, int ancho, Function<T, Object> valor) {
        return columna(nombre, Tipo.DINERO, ancho, valor);
    }

    public ModeloTabla<T> cantidad(String nombre, int ancho, Function<T, Object> valor) {
        return columna(nombre, Tipo.CANTIDAD, ancho, valor);
    }

    public ModeloTabla<T> entero(String nombre, int ancho, Function<T, Object> valor) {
        return columna(nombre, Tipo.ENTERO, ancho, valor);
    }

    // Color de letra por fila (por ejemplo rojo si está bajo stock)
    public ModeloTabla<T> colorTexto(Function<T, Color> funcion) {
        this.colorTexto = funcion;
        return this;
    }

    // Color de fondo por fila
    public ModeloTabla<T> colorFondo(Function<T, Color> funcion) {
        this.colorFondo = funcion;
        return this;
    }

    // Conecta este modelo con la tabla: anchos y formato de cada columna
    public void aplicar(JTable tabla) {
        tabla.setModel(this);
        for (int i = 0; i < columnas.size(); i++) {
            TableColumn tc = tabla.getColumnModel().getColumn(i);
            Columna<T> c = columnas.get(i);
            if (c.ancho > 0) {
                tc.setPreferredWidth(c.ancho);
            }
            tc.setCellRenderer(new Renderizador(c.tipo, tabla));
        }
    }

    // ---------- Datos ----------

    public void setFilas(List<T> filas) {
        this.filas = filas == null ? new ArrayList<T>() : new ArrayList<>(filas);
        fireTableDataChanged();
    }

    public List<T> getFilas() {
        return filas;
    }

    public T getFila(int indice) {
        return filas.get(indice);
    }

    // Objeto de la fila seleccionada (tomando en cuenta el ordenamiento), o null
    public T seleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return filas.get(tabla.convertRowIndexToModel(fila));
    }

    // Selecciona la fila del objeto indicado, si está en la tabla
    public void seleccionar(JTable tabla, T objeto) {
        int i = filas.indexOf(objeto);
        if (i >= 0) {
            int vista = tabla.convertRowIndexToView(i);
            tabla.setRowSelectionInterval(vista, vista);
        }
    }

    // ---------- AbstractTableModel ----------

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna).nombre;
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        switch (columnas.get(columna).tipo) {
            case DINERO:
            case CANTIDAD:
                return Double.class;
            case ENTERO:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Object valor = columnas.get(columna).valor.apply(filas.get(fila));
        if (valor instanceof Number) {
            Tipo tipo = columnas.get(columna).tipo;
            if (tipo == Tipo.ENTERO) {
                return ((Number) valor).intValue();
            }
            if (tipo == Tipo.DINERO || tipo == Tipo.CANTIDAD) {
                return ((Number) valor).doubleValue();
            }
        }
        return valor == null ? "" : valor;
    }

    // ---------- Formato de celdas ----------

    private class Renderizador extends DefaultTableCellRenderer {

        private final Tipo tipo;
        private final JTable tabla;

        Renderizador(Tipo tipo, JTable tabla) {
            this.tipo = tipo;
            this.tabla = tabla;
            setBorder(Estilo.borde(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, valor, seleccionada, false, fila, columna);
            l.setBorder(Estilo.borde(0, 10, 0, 10));
            if (tipo == Tipo.TEXTO) {
                l.setHorizontalAlignment(LEFT);
            } else {
                l.setHorizontalAlignment(RIGHT);
            }
            if (valor instanceof Number) {
                double n = ((Number) valor).doubleValue();
                if (tipo == Tipo.DINERO) {
                    l.setText(Formato.dinero(n));
                } else if (tipo == Tipo.CANTIDAD) {
                    l.setText(Formato.cantidad(n));
                }
            }
            T objeto = filas.get(tabla.convertRowIndexToModel(fila));
            if (!seleccionada) {
                Color fondo = colorFondo == null ? null : colorFondo.apply(objeto);
                l.setBackground(fondo == null ? Estilo.BLANCO : fondo);
            }
            Color texto = colorTexto == null ? null : colorTexto.apply(objeto);
            l.setForeground(texto == null ? Estilo.TEXTO : texto);
            return l;
        }
    }
}
