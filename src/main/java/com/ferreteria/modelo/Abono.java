package com.ferreteria.modelo;

import java.time.LocalDateTime;

public class Abono {

    private int idAbono;
    private LocalDateTime fecha;
    private double monto;
    private Usuario usuario;

    public Abono() {
        this.fecha = LocalDateTime.now();
    }

    public Abono(double monto, Usuario usuario) {
        this.fecha = LocalDateTime.now();
        setMonto(monto);
        setUsuario(usuario);
    }

    public int getIdAbono() {
        return idAbono;
    }

    public void setIdAbono(int idAbono) {
        this.idAbono = idAbono;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del abono es obligatoria");
        }
        this.fecha = fecha;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor que 0");
        }
        this.monto = monto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El abono debe registrar quién recibió el pago");
        }
        this.usuario = usuario;
    }
}
