package com.ferreteria.modelo;

public abstract class Persona {

    private int idPersona;
    private String nombre;
    private String direccion;
    private String celular;
    private boolean activo;

    public Persona() {
        this.activo = true;
    }

    public Persona(String nombre, String direccion, String celular) {
        setNombre(nombre);
        this.direccion = direccion;
        setCelular(celular);
        this.activo = true;
    }

    public abstract String getTipo();

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCelular() {
        if (celular != null && celular.trim().length() > 15) {
        throw new IllegalArgumentException("El celular no puede tener más de 15 caracteres");
    }
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
