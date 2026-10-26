package com.ferreteria.dao;

import com.ferreteria.modelo.Abono;
import com.ferreteria.modelo.Credito;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CreditoDAO {

    private static final String SELECT_BASE =
            "SELECT cr.id_credito, cr.id_venta, cr.fecha_vencimiento FROM credito cr "
            + "JOIN venta v ON v.id_venta = cr.id_venta";

    // Condición SQL: el crédito todavía tiene saldo
    private static final String CON_SALDO =
            " v.total > COALESCE((SELECT SUM(a.monto) FROM abono a WHERE a.id_credito = cr.id_credito), 0)";

    private final VentaDAO ventaDAO = new VentaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Credito buscarPorId(int idCredito) throws SQLException {
        String sql = SELECT_BASE + " WHERE cr.id_credito = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCredito);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public Credito buscarPorVenta(int idVenta) throws SQLException {
        String sql = SELECT_BASE + " WHERE cr.id_venta = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // Créditos con saldo, los que vencen primero arriba
    public List<Credito> listarPendientes() throws SQLException {
        return consultar(SELECT_BASE + " WHERE v.anulada = FALSE AND" + CON_SALDO
                + " ORDER BY cr.fecha_vencimiento", 0);
    }

    // Créditos con saldo cuya fecha de vencimiento ya pasó
    public List<Credito> listarVencidos() throws SQLException {
        return consultar(SELECT_BASE + " WHERE v.anulada = FALSE AND cr.fecha_vencimiento < CURDATE() AND"
                + CON_SALDO + " ORDER BY cr.fecha_vencimiento", 0);
    }

    // Todos los créditos del cliente (pagados y pendientes)
    public List<Credito> listarPorCliente(int idCliente) throws SQLException {
        return consultar(SELECT_BASE + " WHERE v.anulada = FALSE AND v.id_cliente = ?"
                + " ORDER BY v.fecha DESC", idCliente);
    }

    public void registrarAbono(Credito credito, Abono abono) throws SQLException {
        // Validación del modelo: no pagado, abono no mayor al saldo
        credito.registrarAbono(abono);

        String sql = "INSERT INTO abono (id_credito, fecha, monto, id_usuario) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, credito.getIdCredito());
            ps.setTimestamp(2, Timestamp.valueOf(abono.getFecha()));
            ps.setDouble(3, abono.getMonto());
            ps.setInt(4, abono.getUsuario().getIdPersona());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    abono.setIdAbono(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            credito.getAbonos().remove(abono); // deshacer en memoria si falló la BD
            throw e;
        }
    }

    public List<Abono> listarAbonos(int idCredito) throws SQLException {
        String sql = "SELECT id_abono, fecha, monto, id_usuario FROM abono "
                + "WHERE id_credito = ? ORDER BY fecha";
        List<Abono> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCredito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Abono a = new Abono();
                    a.setIdAbono(rs.getInt("id_abono"));
                    a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    a.setMonto(rs.getDouble("monto"));
                    a.setUsuario(usuarioDAO.buscarPorId(rs.getInt("id_usuario")));
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    private List<Credito> consultar(String sql, int parametro) throws SQLException {
        List<Credito> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro > 0) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Credito mapear(ResultSet rs) throws SQLException {
        Credito c = new Credito();
        c.setIdCredito(rs.getInt("id_credito"));
        c.setVenta(ventaDAO.buscarPorId(rs.getInt("id_venta")));
        c.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
        c.setAbonos(listarAbonos(c.getIdCredito()));
        return c;
    }
}
