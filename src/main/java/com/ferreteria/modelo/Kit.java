package com.ferreteria.modelo;

import java.util.ArrayList;
import java.util.List;

public class Kit extends Producto {

    private List<ComponenteKit> componentes;

    public Kit() {
        super();
        this.componentes = new ArrayList<>();
    }

    public Kit(String codigo, String nombre, String unidad, String presentacion,
               Marca marca, Categoria categoria, double precioCompra, double precioVenta,
               double cantidadActual, double existenciaMinima, String foto) {
        super(codigo, nombre, unidad, presentacion, marca, categoria, precioCompra,
                precioVenta, cantidadActual, existenciaMinima, foto);
        this.componentes = new ArrayList<>();
    }

    @Override
    public boolean esKit() {
        return true;
    }

    public void agregarComponente(ComponenteKit componente) {
        if (componente == null) {
            throw new IllegalArgumentException("El componente no puede estar vacío");
        }
        for (ComponenteKit c : componentes) {
            if (c.getProducto().getIdProducto() == componente.getProducto().getIdProducto()) {
                throw new IllegalArgumentException("El producto " + componente.getProducto()
                        + " ya está en el kit");
            }
        }
        componentes.add(componente);
    }

    public void quitarComponente(ComponenteKit componente) {
        componentes.remove(componente);
    }

    public double getSumaPesos() {
        double suma = 0;
        for (ComponenteKit c : componentes) {
            suma += c.getPeso();
        }
        return suma;
    }

    public double calcularCostoComponente(ComponenteKit componente) {
        double sumaPesos = getSumaPesos();
        if (sumaPesos <= 0) {
            throw new IllegalStateException("El kit no tiene componentes");
        }
        double costoTotalComponente = getPrecioCompra() * (componente.getPeso() / sumaPesos);
        return costoTotalComponente / componente.getCantidadPiezas();
    }

    public void desarmar() {
        if (componentes.isEmpty()) {
            throw new IllegalStateException("El kit no tiene componentes registrados");
        }
        descontarExistencia(1);
        for (ComponenteKit c : componentes) {
            c.getProducto().aumentarExistencia(c.getCantidadPiezas());
        }
    }

    public List<ComponenteKit> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<ComponenteKit> componentes) {
        if (componentes == null) {
            this.componentes = new ArrayList<>();
        } else {
            this.componentes = componentes;
        }
    }
}
