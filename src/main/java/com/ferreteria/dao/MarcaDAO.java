package com.ferreteria.dao;

import com.ferreteria.modelo.Marca;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MarcaDAO {

    private static final String SELECT_BASE = "SELECT id_marca, nombre, descripcion, activa FROM marca";

    public void insertar(Marca marca) throws SQLException {
        String sql = "INSERT INTO marca (nombre, descripcion, activa) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marca.getNombre());
            ps.setString(2, marca.getDescripcion());
            ps.setBoolean(3, marca.isActiva());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    marca.setIdMarca(rs.getInt(1));
                }
            }
        }
    }

    public List<Marca> listar() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY nombre");
    }

    public List<Marca> listarActivas() throws SQLException {
        return consultar(SELECT_BASE + " WHERE activa = TRUE ORDER BY nombre");
    }

    public Marca buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_marca = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(Marca marca) throws SQLException {
        String sql = "UPDATE marca SET nombre = ?, descripcion = ?, activa = ? WHERE id_marca = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, marca.getNombre());
            ps.setString(2, marca.getDescripcion());
            ps.setBoolean(3, marca.isActiva());
            ps.setInt(4, marca.getIdMarca());
            ps.executeUpdate();
        }
    }

    // Eliminación lógica: solo se desactiva para no perder el historial
    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE marca SET activa = FALSE WHERE id_marca = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Marca> consultar(String sql) throws SQLException {
        List<Marca> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Marca mapear(ResultSet rs) throws SQLException {
        return new Marca(
                rs.getInt("id_marca"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activa"));
    }
}
