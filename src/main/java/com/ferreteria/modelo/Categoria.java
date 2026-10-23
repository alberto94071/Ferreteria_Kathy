package com.ferreteria.modelo;

public class Categoria {

    private int idCategoria;
    private String nombre;
    private String descripcion;
    private boolean activa;

    public Categoria() {
        this.activa = true;
    }

    public Categoria(String nombre, String descripcion) {
        setNombre(nombre);
        this.descripcion = descripcion;
        this.activa = true;
    }

    public Categoria(int idCategoria, String nombre, String descripcion, boolean activa) {
        this.idCategoria = idCategoria;
        setNombre(nombre);
        this.descripcion = descripcion;
        this.activa = activa;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
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
