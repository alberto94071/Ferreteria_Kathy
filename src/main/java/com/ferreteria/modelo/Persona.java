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
        setDireccion(direccion);
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
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede tener más de 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion != null && direccion.trim().length() > 200) {
            throw new IllegalArgumentException("La dirección no puede tener más de 200 caracteres");
        }
        this.direccion = direccion;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        if (celular != null && celular.trim().length() > 15) {
            throw new IllegalArgumentException("El celular no puede tener más de 15 caracteres");
        }
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
