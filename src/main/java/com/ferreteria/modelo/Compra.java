package com.ferreteria.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {

    private int idCompra;
    private LocalDateTime fecha;
    private Proveedor proveedor;
    private Usuario usuario;
    private boolean esCredito;
    private List<DetalleCompra> detalles;

    public Compra() {
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>();
    }

    public Compra(Proveedor proveedor, Usuario usuario, boolean esCredito) {
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>();
        setProveedor(proveedor);
        setUsuario(usuario);
        setEsCredito(esCredito);
    }

    public void agregarDetalle(DetalleCompra detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede estar vacío");
        }
        detalles.add(detalle);
    }

    public void quitarDetalle(DetalleCompra detalle) {
        detalles.remove(detalle);
    }

    public double getTotal() {
        double total = 0;
        for (DetalleCompra d : detalles) {
            total += d.getSubtotal();
        }
        return total;
    }

    public void validarParaConfirmar() {
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La compra debe tener al menos un producto");
        }
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (fecha.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("La fecha de la compra no puede ser futura");
        }
        this.fecha = fecha;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor");
        }
        this.proveedor = proveedor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("La compra debe registrar el usuario que la realizó");
        }
        this.usuario = usuario;
    }

    public boolean isEsCredito() {
        return esCredito;
    }

    public void setEsCredito(boolean esCredito) {
        this.esCredito = esCredito;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        if (detalles == null) {
            this.detalles = new ArrayList<>();
        } else {
            this.detalles = detalles;
        }
    }
}
