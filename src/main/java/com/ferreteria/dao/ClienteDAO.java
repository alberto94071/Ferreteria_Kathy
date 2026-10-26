package com.ferreteria.dao;

import com.ferreteria.modelo.Cliente;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Formato;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String SELECT_BASE =
            "SELECT id_cliente, nombre, direccion, celular, limite_credito, dias_credito, activo FROM cliente";

    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, direccion, celular, limite_credito, dias_credito, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDireccion());
            ps.setString(3, cliente.getCelular());
            ps.setDouble(4, cliente.getLimiteCredito());
            ps.setInt(5, cliente.getDiasCredito());
            ps.setBoolean(6, cliente.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdPersona(rs.getInt(1));
                }
            }
        }
    }

    // Solo clientes activos
    public List<Cliente> listar() throws SQLException {
        String sql = SELECT_BASE + " WHERE activo = TRUE ORDER BY nombre";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Cliente> buscarPorNombre(String texto) throws SQLException {
        String sql = SELECT_BASE + " WHERE activo = TRUE AND nombre LIKE ? ORDER BY nombre";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (texto == null ? "" : texto.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_cliente = ?";
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

    public void actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, direccion = ?, celular = ?, limite_credito = ?, "
                + "dias_credito = ?, activo = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDireccion());
            ps.setString(3, cliente.getCelular());
            ps.setDouble(4, cliente.getLimiteCredito());
            ps.setInt(5, cliente.getDiasCredito());
            ps.setBoolean(6, cliente.isActivo());
            ps.setInt(7, cliente.getIdPersona());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        if (obtenerDeudaPendiente(id) > 0) {
            throw new IllegalStateException("No se puede eliminar un cliente que tiene saldo pendiente");
        }
        String sql = "UPDATE cliente SET activo = FALSE WHERE id_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Suma de los saldos de todos los créditos del cliente (total de la venta - abonos)
    public double obtenerDeudaPendiente(int idCliente) throws SQLException {
        String sql = "SELECT COALESCE(SUM(v.total - COALESCE("
                + "  (SELECT SUM(a.monto) FROM abono a WHERE a.id_credito = cr.id_credito), 0)), 0) AS deuda "
                + "FROM credito cr "
                + "JOIN venta v ON v.id_venta = cr.id_venta "
                + "WHERE v.id_cliente = ? AND v.anulada = FALSE";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double deuda = Formato.redondear(rs.getDouble("deuda"));
                    return deuda < 0 ? 0 : deuda;
                }
            }
        }
        return 0;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdPersona(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setDireccion(rs.getString("direccion"));
        c.setCelular(rs.getString("celular"));
        c.setLimiteCredito(rs.getDouble("limite_credito"));
        c.setDiasCredito(rs.getInt("dias_credito"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
