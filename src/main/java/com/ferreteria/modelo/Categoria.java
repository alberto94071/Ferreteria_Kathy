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
        setDescripcion(descripcion);
        this.activa = true;
    }

    public Categoria(int idCategoria, String nombre, String descripcion, boolean activa) {
        this.idCategoria = idCategoria;
        setNombre(nombre);
        setDescripcion(descripcion);
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
        if (nombre.trim().length() > 50) {
            throw new IllegalArgumentException("El nombre de la categoría no puede tener más de 50 caracteres");
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
