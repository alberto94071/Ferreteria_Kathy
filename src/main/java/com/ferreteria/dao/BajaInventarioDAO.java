package com.ferreteria.dao;

import com.ferreteria.modelo.BajaInventario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BajaInventarioDAO {

    public void registrar(BajaInventario baja) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<BajaInventario> listar() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<BajaInventario> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private BajaInventario mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
