package com.ferreteria.modelo;

public class Proveedor extends Persona {

    private String nit;
    private String empresa;

    public Proveedor() {
    }

    public Proveedor(String nombre, String direccion, String celular,
                     String nit, String empresa) {
        super(nombre, direccion, celular);
        setNit(nit);
        setEmpresa(empresa);
    }

    @Override
    public String getTipo() {
        return "Proveedor";
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        if (nit == null || nit.trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT del proveedor es obligatorio");
        }
        if (nit.trim().length() > 15) {
            throw new IllegalArgumentException("El NIT no puede tener más de 15 caracteres");
        }
        this.nit = nit.trim().toUpperCase();
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        if (empresa == null || empresa.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        this.empresa = empresa.trim();
    }

    @Override
    public String toString() {
        return empresa;
    }
}