package com.ferreteria.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private int idVenta;
    private LocalDateTime fecha;
    private Cliente cliente;
    private Usuario usuario;
    private boolean esCredito;
    private List<DetalleVenta> detalles;

    public Venta() {
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>();
    }

    public Venta(Cliente cliente, Usuario usuario, boolean esCredito) {
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>();
        setCliente(cliente);
        setUsuario(usuario);
        setEsCredito(esCredito);
    }

    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle no puede estar vacío");
        }
        double cantidadYaAgregada = 0;
        for (DetalleVenta d : detalles) {
            if (d.getProducto().getIdProducto() == detalle.getProducto().getIdProducto()) {
                cantidadYaAgregada += d.getCantidad();
            }
        }
        double existencia = detalle.getProducto().getCantidadActual();
        if (cantidadYaAgregada + detalle.getCantidad() > existencia) {
            throw new IllegalArgumentException("No hay suficiente existencia de "
                    + detalle.getProducto() + ". Disponible: " + existencia);
        }
        detalles.add(detalle);
    }

    public void quitarDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
    }

    public double getTotal() {
        double total = 0;
        for (DetalleVenta d : detalles) {
            total += d.getSubtotal();
        }
        return total;
    }

    public boolean tieneProductosBajoCosto() {
        for (DetalleVenta d : detalles) {
            if (d.esPrecioBajoCosto()) {
                return true;
            }
        }
        return false;
    }

    public void validarParaConfirmar() {
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La venta debe tener al menos un producto");
        }
        if (esCredito && cliente == null) {
            throw new IllegalStateException("Una venta al crédito debe tener un cliente registrado");
        }
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (fecha.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new IllegalArgumentException("La fecha de la venta no puede ser futura");
        }
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("La venta debe registrar el usuario que la realizó");
        }
        this.usuario = usuario;
    }

    public boolean isEsCredito() {
        return esCredito;
    }

    public void setEsCredito(boolean esCredito) {
        this.esCredito = esCredito;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        if (detalles == null) {
            this.detalles = new ArrayList<>();
        } else {
            this.detalles = detalles;
        }
    }
}
