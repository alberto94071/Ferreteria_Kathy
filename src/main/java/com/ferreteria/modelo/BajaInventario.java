package com.ferreteria.modelo;

import java.time.LocalDateTime;

public class BajaInventario {

    private int idBaja;
    private Producto producto;
    private double cantidad;
    private String motivo;
    private Usuario usuario;
    private LocalDateTime fecha;

    public BajaInventario() {
        this.fecha = LocalDateTime.now();
    }

    public BajaInventario(Producto producto, double cantidad, String motivo, Usuario usuario) {
        this.fecha = LocalDateTime.now();
        setProducto(producto);
        setCantidad(cantidad);
        setMotivo(motivo);
        setUsuario(usuario);
    }

    public void aplicar() {
        producto.descontarExistencia(cantidad);
    }

    public int getIdBaja() {
        return idBaja;
    }

    public void setIdBaja(int idBaja) {
        this.idBaja = idBaja;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("Debe seleccionar el producto a dar de baja");
        }
        this.producto = producto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a dar de baja debe ser mayor que 0");
        }
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la baja es obligatorio");
        }
        if (motivo.trim().length() > 200) {
            throw new IllegalArgumentException("El motivo no puede tener más de 200 caracteres");
        }
        this.motivo = motivo.trim();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("La baja debe registrar quién la autorizó");
        }
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        this.fecha = fecha;
    }
}
