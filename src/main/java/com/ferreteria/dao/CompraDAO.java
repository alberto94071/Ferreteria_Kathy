package com.ferreteria.dao;

import com.ferreteria.modelo.Compra;
import com.ferreteria.modelo.DetalleCompra;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CompraDAO {

    public void registrar(Compra compra) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Compra buscarPorId(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<DetalleCompra> listarDetalles(int idCompra) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Compra> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public double ultimoPrecioCompra(int idProducto) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public String proveedorMasBarato(int idProducto) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
