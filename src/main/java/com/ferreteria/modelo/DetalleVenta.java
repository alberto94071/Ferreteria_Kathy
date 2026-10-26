package com.ferreteria.modelo;

public class DetalleVenta {

    private int idDetalleVenta;
    private Producto producto;
    private double cantidad;
    private double precioFinal;

    public DetalleVenta() {
    }

    public DetalleVenta(Producto producto, double cantidad, double precioFinal) {
        setProducto(producto);
        setCantidad(cantidad);
        setPrecioFinal(precioFinal);
    }

    public double getSubtotal() {
        return cantidad * precioFinal;
    }

    public boolean esPrecioBajoCosto() {
        return precioFinal < producto.getPrecioCompra();
    }

    public int getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(int idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
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

    public double getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(double precioFinal) {
        if (precioFinal < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        this.precioFinal = precioFinal;
    }
}
