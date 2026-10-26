package com.ferreteria.modelo;

public class DetalleCompra {

    private int idDetalleCompra;
    private Producto producto;
    private double cantidad;
    private double precioCosto;

    public DetalleCompra() {
    }

    public DetalleCompra(Producto producto, double cantidad, double precioCosto) {
        setProducto(producto);
        setCantidad(cantidad);
        setPrecioCosto(precioCosto);
    }

    public double getSubtotal() {
        return cantidad * precioCosto;
    }

    public int getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(int idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto");
        }
        this.producto = producto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        this.cantidad = cantidad;
    }

    public double getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(double precioCosto) {
        if (precioCosto < 0) {
            throw new IllegalArgumentException("El precio de costo no puede ser negativo");
        }
        this.precioCosto = precioCosto;
    }
}
