package com.ferreteria.dao;

import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.ComponenteKit;
import com.ferreteria.modelo.Kit;
import com.ferreteria.modelo.Marca;
import com.ferreteria.modelo.Producto;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Trae el producto junto con su marca y categoría en una sola consulta
    private static final String SELECT_BASE =
            "SELECT p.id_producto, p.codigo, p.nombre, p.unidad, p.presentacion, "
            + "p.id_marca, p.id_categoria, p.precio_compra, p.precio_venta, p.cantidad_actual, "
            + "p.existencia_minima, p.foto, p.es_kit, p.activo, "
            + "m.nombre AS marca_nombre, m.descripcion AS marca_descripcion, m.activa AS marca_activa, "
            + "c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion, c.activa AS categoria_activa "
            + "FROM producto p "
            + "LEFT JOIN marca m ON m.id_marca = p.id_marca "
            + "LEFT JOIN categoria c ON c.id_categoria = p.id_categoria";

    public void insertar(Producto producto) throws SQLException {
        if (producto.getCodigo() != null && buscarPorCodigo(producto.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + producto.getCodigo());
        }
        String sql = "INSERT INTO producto (codigo, nombre, unidad, presentacion, id_marca, id_categoria, "
                + "precio_compra, precio_venta, cantidad_actual, existencia_minima, foto, es_kit, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, producto);
            ps.setBoolean(12, producto.esKit());
            ps.setBoolean(13, producto.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    producto.setIdProducto(rs.getInt(1));
                }
            }
        }
        if (producto instanceof Kit) {
            Kit kit = (Kit) producto;
            if (!kit.getComponentes().isEmpty()) {
                guardarComponentes(kit);
            }
        }
    }

    // Solo productos activos
    public List<Producto> listar() throws SQLException {
        return consultar(SELECT_BASE + " WHERE p.activo = TRUE ORDER BY p.nombre", null);
    }

    // Busca por nombre, código o presentación
    public List<Producto> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.activo = TRUE AND "
                + "(p.nombre LIKE ? OR p.codigo LIKE ? OR p.presentacion LIKE ?) ORDER BY p.nombre";
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        return consultar(sql, patron);
    }

    public Producto buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id_producto = ?";
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

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        String sql = SELECT_BASE + " WHERE p.codigo = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Producto> listarBajoStock() throws SQLException {
        return consultar(SELECT_BASE
                + " WHERE p.activo = TRUE AND p.cantidad_actual <= p.existencia_minima ORDER BY p.nombre", null);
    }

    public void actualizar(Producto producto) throws SQLException {
        if (producto.getCodigo() != null) {
            Producto otro = buscarPorCodigo(producto.getCodigo());
            if (otro != null && otro.getIdProducto() != producto.getIdProducto()) {
                throw new IllegalArgumentException("Ya existe otro producto con el código " + producto.getCodigo());
            }
        }
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, unidad = ?, presentacion = ?, id_marca = ?, "
                + "id_categoria = ?, precio_compra = ?, precio_venta = ?, cantidad_actual = ?, "
                + "existencia_minima = ?, foto = ?, activo = ? WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenarParametros(ps, producto);
            ps.setBoolean(12, producto.isActivo());
            ps.setInt(13, producto.getIdProducto());
            ps.executeUpdate();
        }
    }

    // Eliminación lógica
    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE producto SET activo = FALSE WHERE id_producto = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Se usa dentro de transacciones (ventas, compras, bajas): recibe la conexión abierta
    public void actualizarExistencia(Connection con, int idProducto, double nuevaCantidad) throws SQLException {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La existencia no puede quedar negativa");
        }
        String sql = "UPDATE producto SET cantidad_actual = ? WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, nuevaCantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    // Lee la existencia real en la BD y bloquea la fila hasta terminar la transacción,
    // así dos ventas al mismo tiempo no pueden vender la misma existencia.
    public double obtenerExistencia(Connection con, int idProducto) throws SQLException {
        String sql = "SELECT cantidad_actual FROM producto WHERE id_producto = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        throw new SQLException("No existe el producto con id " + idProducto);
    }

    // Reemplaza todos los componentes del kit por los que tiene el objeto
    public void guardarComponentes(Kit kit) throws SQLException {
        if (kit.getIdProducto() <= 0) {
            throw new IllegalStateException("Primero debe guardar el kit");
        }
        String borrar = "DELETE FROM componente_kit WHERE id_kit = ?";
        String insertar = "INSERT INTO componente_kit (id_kit, id_producto, cantidad_piezas, peso) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(borrar)) {
                    ps.setInt(1, kit.getIdProducto());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
                    for (ComponenteKit c : kit.getComponentes()) {
                        ps.setInt(1, kit.getIdProducto());
                        ps.setInt(2, c.getProducto().getIdProducto());
                        ps.setInt(3, c.getCantidadPiezas());
                        ps.setDouble(4, c.getPeso());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                c.setIdComponenteKit(rs.getInt(1));
                            }
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

    public List<ComponenteKit> listarComponentes(int idKit) throws SQLException {
        String sql = "SELECT id_componente_kit, id_producto, cantidad_piezas, peso "
                + "FROM componente_kit WHERE id_kit = ? ORDER BY id_componente_kit";
        List<ComponenteKit> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idKit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComponenteKit c = new ComponenteKit();
                    c.setIdComponenteKit(rs.getInt("id_componente_kit"));
                    c.setProducto(buscarPorId(rs.getInt("id_producto")));
                    c.setCantidadPiezas(rs.getInt("cantidad_piezas"));
                    c.setPeso(rs.getDouble("peso"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    // Abre un kit: descuenta 1 kit y suma las piezas de cada componente al inventario
    public void desarmarKit(Kit kit) throws SQLException {
        if (kit.getComponentes().isEmpty()) {
            kit.setComponentes(listarComponentes(kit.getIdProducto()));
        }
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                // Cargar existencias reales antes de modificar
                kit.setCantidadActual(obtenerExistencia(con, kit.getIdProducto()));
                for (ComponenteKit c : kit.getComponentes()) {
                    Producto p = c.getProducto();
                    p.setCantidadActual(obtenerExistencia(con, p.getIdProducto()));
                }

                kit.desarmar(); // regla de negocio del modelo

                actualizarExistencia(con, kit.getIdProducto(), kit.getCantidadActual());
                for (ComponenteKit c : kit.getComponentes()) {
                    Producto p = c.getProducto();
                    actualizarExistencia(con, p.getIdProducto(), p.getCantidadActual());
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

    private List<Producto> consultar(String sql, String patron) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (patron != null) {
                ps.setString(1, patron);
                ps.setString(2, patron);
                ps.setString(3, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // Parámetros 1 al 11, comunes a INSERT y UPDATE
    private void llenarParametros(PreparedStatement ps, Producto producto) throws SQLException {
        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setString(3, producto.getUnidad());
        ps.setString(4, producto.getPresentacion());
        if (producto.getMarca() == null) {
            ps.setNull(5, Types.INTEGER);
        } else {
            ps.setInt(5, producto.getMarca().getIdMarca());
        }
        if (producto.getCategoria() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, producto.getCategoria().getIdCategoria());
        }
        ps.setDouble(7, producto.getPrecioCompra());
        ps.setDouble(8, producto.getPrecioVenta());
        ps.setDouble(9, producto.getCantidadActual());
        ps.setDouble(10, producto.getExistenciaMinima());
        ps.setString(11, producto.getFoto());
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        boolean esKit = rs.getBoolean("es_kit");
        Producto p;
        if (esKit) {
            p = new Kit();
        } else {
            p = new Producto();
        }
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setUnidad(rs.getString("unidad"));
        p.setPresentacion(rs.getString("presentacion"));

        int idMarca = rs.getInt("id_marca");
        if (!rs.wasNull()) {
            p.setMarca(new Marca(idMarca, rs.getString("marca_nombre"),
                    rs.getString("marca_descripcion"), rs.getBoolean("marca_activa")));
        }
        int idCategoria = rs.getInt("id_categoria");
        if (!rs.wasNull()) {
            p.setCategoria(new Categoria(idCategoria, rs.getString("categoria_nombre"),
                    rs.getString("categoria_descripcion"), rs.getBoolean("categoria_activa")));
        }

        p.setPrecioCompra(rs.getDouble("precio_compra"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setCantidadActual(rs.getDouble("cantidad_actual"));
        p.setExistenciaMinima(rs.getDouble("existencia_minima"));
        p.setFoto(rs.getString("foto"));
        p.setActivo(rs.getBoolean("activo"));

        if (esKit) {
            ((Kit) p).setComponentes(listarComponentes(p.getIdProducto()));
        }
        return p;
    }
}
