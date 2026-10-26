package com.ferreteria.modelo;

public class Marca {

    private int idMarca;
    private String nombre;
    private String descripcion;
    private boolean activa;

    public Marca() {
        this.activa = true;
    }

    public Marca(String nombre, String descripcion) {
        setNombre(nombre);
        setDescripcion(descripcion);
        this.activa = true;
    }

    public Marca(int idMarca, String nombre, String descripcion, boolean activa) {
        this.idMarca = idMarca;
        setNombre(nombre);
        setDescripcion(descripcion);
        this.activa = activa;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la marca es obligatorio");
        }
        if (nombre.trim().length() > 60) {
            throw new IllegalArgumentException("El nombre de la marca no puede tener más de 60 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 200) {
            throw new IllegalArgumentException("La descripción no puede tener más de 200 caracteres");
        }
        this.descripcion = descripcion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
