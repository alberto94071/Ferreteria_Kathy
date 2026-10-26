package com.ferreteria.dao;

import com.ferreteria.modelo.ComponenteKit;
import com.ferreteria.modelo.Kit;
import com.ferreteria.modelo.Producto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProductoDAO {

    public void insertar(Producto producto) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Producto> listar() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Producto> buscar(String texto) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Producto buscarPorId(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Producto> listarBajoStock() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void actualizar(Producto producto) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void eliminar(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void actualizarExistencia(Connection con, int idProducto, double nuevaCantidad) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void guardarComponentes(Kit kit) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<ComponenteKit> listarComponentes(int idKit) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void desarmarKit(Kit kit) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
