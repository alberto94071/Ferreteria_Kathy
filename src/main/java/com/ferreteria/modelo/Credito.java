package com.ferreteria.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Credito {

    private static final double TOLERANCIA = 0.005;

    private int idCredito;
    private Venta venta;
    private LocalDate fechaVencimiento;
    private List<Abono> abonos;

    public Credito() {
        this.abonos = new ArrayList<>();
    }

    public Credito(Venta venta, int diasCredito) {
        this.abonos = new ArrayList<>();
        setVenta(venta);
        if (diasCredito <= 0) {
            throw new IllegalArgumentException("Los días de crédito deben ser mayores que 0");
        }
        this.fechaVencimiento = venta.getFecha().toLocalDate().plusDays(diasCredito);
    }

    public void registrarAbono(Abono abono) {
        if (abono == null) {
            throw new IllegalArgumentException("El abono no puede estar vacío");
        }
        if (estaPagado()) {
            throw new IllegalStateException("Este crédito ya está pagado");
        }
        if (abono.getMonto() > getSaldo() + TOLERANCIA) {
            throw new IllegalArgumentException("El abono (Q" + abono.getMonto()
                    + ") no puede ser mayor que el saldo pendiente (Q" + getSaldo() + ")");
        }
        abonos.add(abono);
    }

    public double getTotalAbonado() {
        double total = 0;
        for (Abono a : abonos) {
            total += a.getMonto();
        }
        return total;
    }

    public double getSaldo() {
        double saldo = venta.getTotal() - getTotalAbonado();
        if (saldo < TOLERANCIA) {
            return 0;
        }
        return saldo;
    }

    public boolean estaPagado() {
        return getSaldo() <= 0;
    }

    public boolean estaVencido() {
        return !estaPagado() && LocalDate.now().isAfter(fechaVencimiento);
    }

    public int getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(int idCredito) {
        this.idCredito = idCredito;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("El crédito debe pertenecer a una venta");
        }
        this.venta = venta;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }
        this.fechaVencimiento = fechaVencimiento;
    }

    public List<Abono> getAbonos() {
        return abonos;
    }

    public void setAbonos(List<Abono> abonos) {
        if (abonos == null) {
            this.abonos = new ArrayList<>();
        } else {
            this.abonos = abonos;
        }
    }
}
