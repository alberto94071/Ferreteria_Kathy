package com.ferreteria.modelo;

public class ComponenteKit {

    private int idComponenteKit;
    private Producto producto;
    private int cantidadPiezas;
    private double peso;

    public ComponenteKit() {
        this.peso = 1;
    }

    public ComponenteKit(Producto producto, int cantidadPiezas, double peso) {
        setProducto(producto);
        setCantidadPiezas(cantidadPiezas);
        setPeso(peso);
    }

    public int getIdComponenteKit() {
        return idComponenteKit;
    }

    public void setIdComponenteKit(int idComponenteKit) {
        this.idComponenteKit = idComponenteKit;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El componente debe tener un producto");
        }
        if (producto.esKit()) {
            throw new IllegalArgumentException("Un kit no puede contener otro kit");
        }
        this.producto = producto;
    }

    public int getCantidadPiezas() {
        return cantidadPiezas;
    }

    public void setCantidadPiezas(int cantidadPiezas) {
        if (cantidadPiezas <= 0) {
            throw new IllegalArgumentException("La cantidad de piezas debe ser mayor que 0");
        }
        this.cantidadPiezas = cantidadPiezas;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que 0");
        }
        this.peso = peso;
    }
}
