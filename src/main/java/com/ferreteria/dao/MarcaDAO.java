package com.ferreteria.dao;

import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;

import com.ferreteria.modelo.Marca;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MarcaDAO {

    public void insertar(Marca marca) throws SQLException {
        String sql = "INSERT INTO marca (nombre, descripcion, activa) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        String sql = "SELECT id_marca, nombre, descripcion, activa FROM marca ORDER BY nombre";
        List<Marca> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Marca> listarActivas() throws SQLException {
        String sql = "SELECT id_marca, nombre, descripcion, activa FROM marca WHERE activa = TRUE ORDER BY nombre";
        List<Marca> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Marca buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_marca, nombre, descripcion, activa FROM marca WHERE id_marca = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, marca.getNombre());
            ps.setString(2, marca.getDescripcion());
            ps.setBoolean(3, marca.isActiva());
            ps.setInt(4, marca.getIdMarca());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE marca SET activa = FALSE WHERE id_marca = ?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Marca mapear(ResultSet rs) throws SQLException {
        return new Marca(
                rs.getInt("id_marca"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBoolean("activa"));
    }
}
