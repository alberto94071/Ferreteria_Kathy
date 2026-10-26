package com.ferreteria.dao;

import com.ferreteria.modelo.Proveedor;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    private static final String SELECT_BASE =
            "SELECT id_proveedor, nombre, direccion, celular, nit, empresa, activo FROM proveedor";

    public void insertar(Proveedor proveedor) throws SQLException {
        if (buscarPorNit(proveedor.getNit()) != null) {
            throw new IllegalArgumentException("Ya existe un proveedor con el NIT " + proveedor.getNit());
        }
        String sql = "INSERT INTO proveedor (nombre, direccion, celular, nit, empresa, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getDireccion());
            ps.setString(3, proveedor.getCelular());
            ps.setString(4, proveedor.getNit());
            ps.setString(5, proveedor.getEmpresa());
            ps.setBoolean(6, proveedor.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    proveedor.setIdPersona(rs.getInt(1));
                }
            }
        }
    }

    // Solo proveedores activos
    public List<Proveedor> listar() throws SQLException {
        String sql = SELECT_BASE + " WHERE activo = TRUE ORDER BY empresa";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_proveedor = ?";
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

    public Proveedor buscarPorNit(String nit) throws SQLException {
        if (nit == null || nit.trim().isEmpty()) {
            return null;
        }
        String sql = SELECT_BASE + " WHERE nit = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nit.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        Proveedor otro = buscarPorNit(proveedor.getNit());
        if (otro != null && otro.getIdPersona() != proveedor.getIdPersona()) {
            throw new IllegalArgumentException("Ya existe otro proveedor con el NIT " + proveedor.getNit());
        }
        String sql = "UPDATE proveedor SET nombre = ?, direccion = ?, celular = ?, nit = ?, "
                + "empresa = ?, activo = ? WHERE id_proveedor = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getDireccion());
            ps.setString(3, proveedor.getCelular());
            ps.setString(4, proveedor.getNit());
            ps.setString(5, proveedor.getEmpresa());
            ps.setBoolean(6, proveedor.isActivo());
            ps.setInt(7, proveedor.getIdPersona());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE proveedor SET activo = FALSE WHERE id_proveedor = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdPersona(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setDireccion(rs.getString("direccion"));
        p.setCelular(rs.getString("celular"));
        p.setNit(rs.getString("nit"));
        p.setEmpresa(rs.getString("empresa"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}