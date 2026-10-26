package com.ferreteria.dao;

import com.ferreteria.modelo.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UsuarioDAO {

    public void insertar(Usuario usuario) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public List<Usuario> listar() throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Usuario buscarPorId(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public Usuario autenticar(String nombreUsuario, String contrasenaPlana) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void actualizar(Usuario usuario) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void cambiarContrasena(int idUsuario, String contrasenaPlana) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    public void eliminar(int id) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Pendiente");
    }
}
