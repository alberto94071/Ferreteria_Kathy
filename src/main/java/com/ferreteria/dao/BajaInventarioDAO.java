package com.ferreteria.dao;

import com.ferreteria.modelo.BajaInventario;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BajaInventarioDAO {

    private static final String SELECT_BASE =
            "SELECT id_baja, id_producto, cantidad, motivo, id_usuario, fecha FROM baja_inventario";

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Guarda la baja y descuenta la existencia del producto en una transacción
    public void registrar(BajaInventario baja) throws SQLException {
        String sql = "INSERT INTO baja_inventario (id_producto, cantidad, motivo, id_usuario, fecha) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                Producto p = baja.getProducto();
                p.setCantidadActual(productoDAO.obtenerExistencia(con, p.getIdProducto()));
                baja.aplicar(); // valida que haya existencia suficiente
                productoDAO.actualizarExistencia(con, p.getIdProducto(), p.getCantidadActual());

                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, p.getIdProducto());
                    ps.setDouble(2, baja.getCantidad());
                    ps.setString(3, baja.getMotivo());
                    ps.setInt(4, baja.getUsuario().getIdPersona());
                    ps.setTimestamp(5, Timestamp.valueOf(baja.getFecha()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            baja.setIdBaja(rs.getInt(1));
                        }
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<BajaInventario> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY fecha DESC";
        List<BajaInventario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // Bajas entre dos fechas (ambas incluidas)
    public List<BajaInventario> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_BASE + " WHERE fecha >= ? AND fecha < ? ORDER BY fecha DESC";
        List<BajaInventario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private BajaInventario mapear(ResultSet rs) throws SQLException {
        BajaInventario b = new BajaInventario();
        b.setIdBaja(rs.getInt("id_baja"));
        b.setProducto(productoDAO.buscarPorId(rs.getInt("id_producto")));
        b.setCantidad(rs.getDouble("cantidad"));
        b.setMotivo(rs.getString("motivo"));
        b.setUsuario(usuarioDAO.buscarPorId(rs.getInt("id_usuario")));
        b.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        return b;
    }
}
