package com.ferreteria.dao;

import com.ferreteria.modelo.Rol;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String SELECT_BASE =
            "SELECT id_usuario, nombre, direccion, celular, nombre_usuario, contrasena, rol, activo FROM usuario";

    public void insertar(Usuario usuario) throws SQLException {
        if (buscarPorNombreUsuario(usuario.getNombreUsuario()) != null) {
            throw new IllegalArgumentException("El nombre de usuario '" + usuario.getNombreUsuario()
                    + "' ya está en uso");
        }
        String sql = "INSERT INTO usuario (nombre, direccion, celular, nombre_usuario, contrasena, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getDireccion());
            ps.setString(3, usuario.getCelular());
            ps.setString(4, usuario.getNombreUsuario());
            ps.setString(5, usuario.getContrasena()); // ya viene encriptada desde el modelo
            ps.setString(6, usuario.getRol().name());
            ps.setBoolean(7, usuario.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdPersona(rs.getInt(1));
                }
            }
        }
    }

    // Solo usuarios activos
    public List<Usuario> listar() throws SQLException {
        String sql = SELECT_BASE + " WHERE activo = TRUE ORDER BY nombre";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_usuario = ?";
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

    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return null;
        }
        String sql = SELECT_BASE + " WHERE nombre_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // Devuelve el usuario si las credenciales son correctas, o null si no
    public Usuario autenticar(String nombreUsuario, String contrasenaPlana) throws SQLException {
        Usuario usuario = buscarPorNombreUsuario(nombreUsuario);
        if (usuario == null || !usuario.isActivo()) {
            return null;
        }
        if (!usuario.verificarContrasena(contrasenaPlana)) {
            return null;
        }
        return usuario;
    }

    // No modifica la contraseña; para eso está cambiarContrasena()
    public void actualizar(Usuario usuario) throws SQLException {
        Usuario otro = buscarPorNombreUsuario(usuario.getNombreUsuario());
        if (otro != null && otro.getIdPersona() != usuario.getIdPersona()) {
            throw new IllegalArgumentException("El nombre de usuario '" + usuario.getNombreUsuario()
                    + "' ya está en uso");
        }
        String sql = "UPDATE usuario SET nombre = ?, direccion = ?, celular = ?, nombre_usuario = ?, "
                + "rol = ?, activo = ? WHERE id_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getDireccion());
            ps.setString(3, usuario.getCelular());
            ps.setString(4, usuario.getNombreUsuario());
            ps.setString(5, usuario.getRol().name());
            ps.setBoolean(6, usuario.isActivo());
            ps.setInt(7, usuario.getIdPersona());
            ps.executeUpdate();
        }
    }

    public void cambiarContrasena(int idUsuario, String contrasenaPlana) throws SQLException {
        if (contrasenaPlana == null || contrasenaPlana.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Usuario.encriptar(contrasenaPlana));
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        Usuario usuario = buscarPorId(id);
        if (usuario != null && usuario.esAdministrador() && contarAdministradoresActivos() <= 1) {
            throw new IllegalStateException("No se puede eliminar al único administrador del sistema");
        }
        String sql = "UPDATE usuario SET activo = FALSE WHERE id_usuario = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private int contarAdministradoresActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE rol = 'ADMINISTRADOR' AND activo = TRUE";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdPersona(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setDireccion(rs.getString("direccion"));
        u.setCelular(rs.getString("celular"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
