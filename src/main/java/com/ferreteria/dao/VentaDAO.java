package com.ferreteria.dao;

import com.ferreteria.modelo.Cliente;
import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Venta;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Formato;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    private static final String SELECT_BASE =
            "SELECT id_venta, fecha, id_cliente, id_usuario, es_credito, total, anulada FROM venta";

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Guarda la venta, sus detalles, descuenta el inventario y, si es al crédito,
     * crea el crédito. Todo en una sola transacción.
     * @param diasCredito días de plazo; si es 0 se usan los días configurados en el cliente
     */
    public void registrar(Venta venta, int diasCredito) throws SQLException {
        venta.validarParaConfirmar();

        int dias = 0;
        if (venta.isEsCredito()) {
            Cliente cliente = venta.getCliente();
            double deuda = clienteDAO.obtenerDeudaPendiente(cliente.getIdPersona());
            if (!cliente.puedeRecibirCredito(deuda)) {
                if (cliente.getLimiteCredito() <= 0) {
                    throw new IllegalStateException("El cliente " + cliente + " no tiene crédito autorizado");
                }
                throw new IllegalStateException("El cliente " + cliente + " tiene un saldo pendiente de "
                        + Formato.dinero(deuda) + ". Debe cancelarlo antes de otro crédito");
            }
            if (venta.getTotal() > cliente.getLimiteCredito()) {
                throw new IllegalStateException("El total (" + Formato.dinero(venta.getTotal())
                        + ") supera el límite de crédito del cliente ("
                        + Formato.dinero(cliente.getLimiteCredito()) + ")");
            }
            dias = diasCredito > 0 ? diasCredito : cliente.getDiasCredito();
            if (dias <= 0) {
                throw new IllegalStateException("Debe indicar los días de crédito");
            }
        }

        String sqlVenta = "INSERT INTO venta (fecha, id_cliente, id_usuario, es_credito, total, anulada) "
                + "VALUES (?, ?, ?, ?, ?, FALSE)";
        String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_final) "
                + "VALUES (?, ?, ?, ?)";
        String sqlCredito = "INSERT INTO credito (id_venta, fecha_vencimiento) VALUES (?, ?)";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                // 1. Encabezado
                try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(venta.getFecha()));
                    if (venta.getCliente() == null) {
                        ps.setNull(2, Types.INTEGER);
                    } else {
                        ps.setInt(2, venta.getCliente().getIdPersona());
                    }
                    ps.setInt(3, venta.getUsuario().getIdPersona());
                    ps.setBoolean(4, venta.isEsCredito());
                    ps.setDouble(5, Formato.redondear(venta.getTotal()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            venta.setIdVenta(rs.getInt(1));
                        }
                    }
                }

                // 2. Detalles + descuento de inventario
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS)) {
                    for (DetalleVenta d : venta.getDetalles()) {
                        Producto p = d.getProducto();
                        p.setCantidadActual(productoDAO.obtenerExistencia(con, p.getIdProducto()));
                        p.descontarExistencia(d.getCantidad()); // valida que alcance
                        productoDAO.actualizarExistencia(con, p.getIdProducto(), p.getCantidadActual());

                        ps.setInt(1, venta.getIdVenta());
                        ps.setInt(2, p.getIdProducto());
                        ps.setDouble(3, d.getCantidad());
                        ps.setDouble(4, d.getPrecioFinal());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                d.setIdDetalleVenta(rs.getInt(1));
                            }
                        }
                    }
                }

                // 3. Crédito
                if (venta.isEsCredito()) {
                    Credito credito = new Credito(venta, dias);
                    try (PreparedStatement ps = con.prepareStatement(sqlCredito)) {
                        ps.setInt(1, venta.getIdVenta());
                        ps.setDate(2, Date.valueOf(credito.getFechaVencimiento()));
                        ps.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                venta.setIdVenta(0);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Venta buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_venta = ?";
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

    public List<DetalleVenta> listarDetalles(int idVenta) throws SQLException {
        String sql = "SELECT id_detalle_venta, id_producto, cantidad, precio_final "
                + "FROM detalle_venta WHERE id_venta = ? ORDER BY id_detalle_venta";
        List<DetalleVenta> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta d = new DetalleVenta();
                    d.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                    d.setProducto(productoDAO.buscarPorId(rs.getInt("id_producto")));
                    d.setCantidad(rs.getDouble("cantidad"));
                    d.setPrecioFinal(rs.getDouble("precio_final"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    // Ventas no anuladas entre dos fechas (ambas incluidas)
    public List<Venta> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_BASE + " WHERE anulada = FALSE AND fecha >= ? AND fecha < ? ORDER BY fecha DESC";
        List<Venta> lista = new ArrayList<>();
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

    public List<Venta> listarPorCliente(int idCliente) throws SQLException {
        String sql = SELECT_BASE + " WHERE anulada = FALSE AND id_cliente = ? ORDER BY fecha DESC";
        List<Venta> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public double totalVendido(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM venta "
                + "WHERE anulada = FALSE AND fecha >= ? AND fecha < ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Formato.redondear(rs.getDouble(1));
                }
            }
        }
        return 0;
    }

    /**
     * Anula la venta: devuelve los productos al inventario y elimina su crédito.
     * No se permite si el crédito ya tiene abonos.
     */
    public void anular(int idVenta) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT anulada FROM venta WHERE id_venta = ? FOR UPDATE")) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("No existe la venta " + idVenta);
                        }
                        if (rs.getBoolean("anulada")) {
                            throw new IllegalStateException("La venta " + idVenta + " ya está anulada");
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM abono a JOIN credito cr ON cr.id_credito = a.id_credito "
                        + "WHERE cr.id_venta = ?")) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new IllegalStateException(
                                    "No se puede anular: el crédito de esta venta ya tiene abonos");
                        }
                    }
                }

                // Devolver existencias
                List<Integer> ids = new ArrayList<>();
                List<Double> cantidades = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_producto, cantidad FROM detalle_venta WHERE id_venta = ?")) {
                    ps.setInt(1, idVenta);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt("id_producto"));
                            cantidades.add(rs.getDouble("cantidad"));
                        }
                    }
                }
                for (int i = 0; i < ids.size(); i++) {
                    int idProducto = ids.get(i);
                    double actual = productoDAO.obtenerExistencia(con, idProducto);
                    productoDAO.actualizarExistencia(con, idProducto, actual + cantidades.get(i));
                }

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM credito WHERE id_venta = ?")) {
                    ps.setInt(1, idVenta);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE venta SET anulada = TRUE WHERE id_venta = ?")) {
                    ps.setInt(1, idVenta);
                    ps.executeUpdate();
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

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        int idCliente = rs.getInt("id_cliente");
        if (!rs.wasNull()) {
            v.setCliente(clienteDAO.buscarPorId(idCliente));
        }
        v.setUsuario(usuarioDAO.buscarPorId(rs.getInt("id_usuario")));
        v.setEsCredito(rs.getBoolean("es_credito"));
        // setDetalles no vuelve a validar existencias (es una venta ya registrada)
        v.setDetalles(listarDetalles(v.getIdVenta()));
        return v;
    }
}
