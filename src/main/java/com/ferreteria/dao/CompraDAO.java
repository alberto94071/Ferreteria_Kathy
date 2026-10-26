package com.ferreteria.dao;

import com.ferreteria.modelo.Compra;
import com.ferreteria.modelo.DetalleCompra;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Formato;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    private static final String SELECT_BASE =
            "SELECT id_compra, fecha, id_proveedor, id_usuario, es_credito, total FROM compra";

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Guarda la compra y sus detalles, suma las cantidades al inventario y
     * actualiza el precio de compra de cada producto con el último costo.
     */
    public void registrar(Compra compra) throws SQLException {
        compra.validarParaConfirmar();

        String sqlCompra = "INSERT INTO compra (fecha, id_proveedor, id_usuario, es_credito, total) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_compra (id_compra, id_producto, cantidad, precio_costo) "
                + "VALUES (?, ?, ?, ?)";
        String sqlPrecio = "UPDATE producto SET precio_compra = ? WHERE id_producto = ?";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(compra.getFecha()));
                    ps.setInt(2, compra.getProveedor().getIdPersona());
                    ps.setInt(3, compra.getUsuario().getIdPersona());
                    ps.setBoolean(4, compra.isEsCredito());
                    ps.setDouble(5, Formato.redondear(compra.getTotal()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            compra.setIdCompra(rs.getInt(1));
                        }
                    }
                }

                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement psPrecio = con.prepareStatement(sqlPrecio)) {
                    for (DetalleCompra d : compra.getDetalles()) {
                        Producto p = d.getProducto();

                        psDet.setInt(1, compra.getIdCompra());
                        psDet.setInt(2, p.getIdProducto());
                        psDet.setDouble(3, d.getCantidad());
                        psDet.setDouble(4, d.getPrecioCosto());
                        psDet.executeUpdate();
                        try (ResultSet rs = psDet.getGeneratedKeys()) {
                            if (rs.next()) {
                                d.setIdDetalleCompra(rs.getInt(1));
                            }
                        }

                        p.setCantidadActual(productoDAO.obtenerExistencia(con, p.getIdProducto()));
                        p.aumentarExistencia(d.getCantidad());
                        productoDAO.actualizarExistencia(con, p.getIdProducto(), p.getCantidadActual());

                        p.setPrecioCompra(d.getPrecioCosto());
                        psPrecio.setDouble(1, d.getPrecioCosto());
                        psPrecio.setInt(2, p.getIdProducto());
                        psPrecio.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                compra.setIdCompra(0);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Compra buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_compra = ?";
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

    public List<DetalleCompra> listarDetalles(int idCompra) throws SQLException {
        String sql = "SELECT id_detalle_compra, id_producto, cantidad, precio_costo "
                + "FROM detalle_compra WHERE id_compra = ? ORDER BY id_detalle_compra";
        List<DetalleCompra> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setIdDetalleCompra(rs.getInt("id_detalle_compra"));
                    d.setProducto(productoDAO.buscarPorId(rs.getInt("id_producto")));
                    d.setCantidad(rs.getDouble("cantidad"));
                    d.setPrecioCosto(rs.getDouble("precio_costo"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    // Compras entre dos fechas (ambas incluidas)
    public List<Compra> listarPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_BASE + " WHERE fecha >= ? AND fecha < ? ORDER BY fecha DESC";
        List<Compra> lista = new ArrayList<>();
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

    // Último costo pagado por el producto; 0 si nunca se ha comprado
    public double ultimoPrecioCompra(int idProducto) throws SQLException {
        String sql = "SELECT dc.precio_costo FROM detalle_compra dc "
                + "JOIN compra c ON c.id_compra = dc.id_compra "
                + "WHERE dc.id_producto = ? ORDER BY c.fecha DESC, dc.id_detalle_compra DESC LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0;
    }

    /**
     * Compara el último precio que dio cada proveedor para este producto
     * y devuelve la empresa con el precio más bajo; null si nunca se ha comprado.
     */
    public String proveedorMasBarato(int idProducto) throws SQLException {
        String sql = "SELECT pr.empresa, dc.precio_costo FROM detalle_compra dc "
                + "JOIN compra c ON c.id_compra = dc.id_compra "
                + "JOIN proveedor pr ON pr.id_proveedor = c.id_proveedor "
                + "WHERE dc.id_producto = ? AND c.fecha = ("
                + "   SELECT MAX(c2.fecha) FROM compra c2 "
                + "   JOIN detalle_compra dc2 ON dc2.id_compra = c2.id_compra "
                + "   WHERE dc2.id_producto = dc.id_producto AND c2.id_proveedor = c.id_proveedor) "
                + "ORDER BY dc.precio_costo ASC LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("empresa");
                }
            }
        }
        return null;
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setIdCompra(rs.getInt("id_compra"));
        c.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        c.setProveedor(proveedorDAO.buscarPorId(rs.getInt("id_proveedor")));
        c.setUsuario(usuarioDAO.buscarPorId(rs.getInt("id_usuario")));
        c.setEsCredito(rs.getBoolean("es_credito"));
        c.setDetalles(listarDetalles(c.getIdCompra()));
        return c;
    }
}
