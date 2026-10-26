package com.ferreteria.modelo;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private String unidad;
    private String presentacion;
    private Marca marca;
    private Categoria categoria;
    private double precioCompra;
    private double precioVenta;
    private double cantidadActual;
    private double existenciaMinima;
    private String foto;
    private boolean activo;

    public Producto() {
        this.activo = true;
    }

    public Producto(String codigo, String nombre, String unidad, String presentacion,
                    Marca marca, Categoria categoria, double precioCompra, double precioVenta,
                    double cantidadActual, double existenciaMinima, String foto) {
        setCodigo(codigo);
        setNombre(nombre);
        setUnidad(unidad);
        setPresentacion(presentacion);
        setMarca(marca);
        setCategoria(categoria);
        setPrecioCompra(precioCompra);
        setPrecioVenta(precioVenta);
        setCantidadActual(cantidadActual);
        setExistenciaMinima(existenciaMinima);
        setFoto(foto);
        this.activo = true;
    }

    public boolean esKit() {
        return false;
    }

    public double getGanancia() {
        return precioVenta - precioCompra;
    }

    public boolean estaBajoStock() {
        return cantidadActual <= existenciaMinima;
    }

    public void descontarExistencia(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que 0");
        }
        if (cantidad > cantidadActual) {
            throw new IllegalArgumentException("No hay suficiente existencia de " + nombre
                    + ". Disponible: " + cantidadActual + ", solicitado: " + cantidad);
        }
        this.cantidadActual = cantidadActual - cantidad;
    }

    public void aumentarExistencia(double cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor que 0");
        }
        this.cantidadActual = cantidadActual + cantidad;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            this.codigo = null;
            return;
        }
        if (codigo.trim().length() > 100) {
            throw new IllegalArgumentException("El código no puede tener más de 100 caracteres");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del producto no puede tener más de 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        if (unidad == null || unidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria");
        }
        if (unidad.trim().length() > 20) {
            throw new IllegalArgumentException("La unidad no puede tener más de 20 caracteres");
        }
        this.unidad = unidad.trim();
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        if (presentacion != null && presentacion.trim().length() > 100) {
            throw new IllegalArgumentException("La presentación no puede tener más de 100 caracteres");
        }
        this.presentacion = presentacion;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        if (precioCompra < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo");
        }
        this.precioCompra = precioCompra;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        if (precioVenta < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }
        this.precioVenta = precioVenta;
    }

    public double getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(double cantidadActual) {
        if (cantidadActual < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa");
        }
        this.cantidadActual = cantidadActual;
    }

    public double getExistenciaMinima() {
        return existenciaMinima;
    }

    public void setExistenciaMinima(double existenciaMinima) {
        if (existenciaMinima <= 0) {
            throw new IllegalArgumentException("La existencia mínima debe ser mayor que 0");
        }
        this.existenciaMinima = existenciaMinima;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        if (foto != null && foto.trim().length() > 260) {
            throw new IllegalArgumentException("La ruta de la foto es demasiado larga");
        }
        this.foto = foto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        if (presentacion == null || presentacion.trim().isEmpty()) {
            return nombre;
        }
        return nombre + " " + presentacion;
    }
}
