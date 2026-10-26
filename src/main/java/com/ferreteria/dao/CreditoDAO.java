package com.ferreteria.dao;

import com.ferreteria.modelo.Abono;
import com.ferreteria.modelo.Credito;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CreditoDAO {

    public Credito buscarPorVenta(int idVenta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Credito> listarPendientes() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Credito> listarVencidos() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Credito> listarPorCliente(int idCliente) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void registrarAbono(Credito credito, Abono abono) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Abono> listarAbonos(int idCredito) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private Credito mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
