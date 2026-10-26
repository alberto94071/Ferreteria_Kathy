package com.ferreteria.dao;

import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Venta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VentaDAO {

    public void registrar(Venta venta, int diasCredito) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Venta buscarPorId(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<DetalleVenta> listarDetalles(int idVenta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Venta> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Venta> listarPorCliente(int idCliente) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public double totalVendido(LocalDate desde, LocalDate hasta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void anular(int idVenta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
