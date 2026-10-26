package com.ferreteria.modelo;

public class Cliente extends Persona {

    private double limiteCredito;
    private int diasCredito;

    public Cliente() {
    }

    public Cliente(String nombre, String direccion, String celular,
                   double limiteCredito, int diasCredito) {
        super(nombre, direccion, celular);
        setLimiteCredito(limiteCredito);
        setDiasCredito(diasCredito);
    }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    public boolean puedeRecibirCredito(double deudaPendiente) {
        return limiteCredito > 0 && deudaPendiente <= 0;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        if (limiteCredito < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo");
        }
        this.limiteCredito = limiteCredito;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        if (diasCredito < 0) {
            throw new IllegalArgumentException("Los días de crédito no pueden ser negativos");
        }
        this.diasCredito = diasCredito;
    }
}
