package com.ferreteria.vista;

import com.ferreteria.dao.UsuarioDAO;
import com.ferreteria.modelo.Rol;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.util.Sesion;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Usuarios del sistema (solo administrador).
 */
public class PanelUsuarios extends PanelCrud<Usuario> {

    private final UsuarioDAO dao = new UsuarioDAO();

    public PanelUsuarios() {
        super("Usuarios", "+ Nuevo usuario", false);
        JButton cambiar = Estilo.botonSecundario("Cambiar contraseña");
        barraBotones.add(cambiar, 0);
        cambiar.addActionListener(e -> Dialogos.ejecutar(this, () -> cambiarContrasena(seleccionado())));
    }

    @Override
    protected void definirColumnas(ModeloTabla<Usuario> m) {
        m.texto("Nombre", 220, u -> u.getNombre())
                .texto("Usuario", 140, u -> u.getNombreUsuario())
                .texto("Rol", 130, u -> u.getRol())
                .texto("Celular", 110, u -> u.getCelular());
    }

    @Override
    protected List<Usuario> cargar(String filtro) throws Exception {
        List<Usuario> lista = new ArrayList<>();
        for (Usuario u : dao.listar()) {
            if (contiene(u.getNombre() + " " + u.getNombreUsuario(), filtro)) {
                lista.add(u);
            }
        }
        return lista;
    }

    @Override
    protected void nuevo() throws Exception {
        DialogoFormulario d = new DialogoFormulario(this, "Nuevo usuario");
        Formulario f = d.getFormulario();
        JTextField nombre = f.agregar("Nombre completo *", new JTextField(24));
        JTextField usuario = f.agregar("Usuario (para entrar) *", new JTextField());
        JComboBox<Rol> rol = f.agregar("Rol", new JComboBox<>(Rol.values()));
        rol.setSelectedItem(Rol.VENDEDOR);
        JPasswordField contrasena = f.agregar("Contraseña *", new JPasswordField());
        JPasswordField repetir = f.agregar("Repetir contraseña *", new JPasswordField());
        JTextField celular = f.agregar("Celular", new JTextField());
        f.agregarCompleto(Estilo.suave("<html>VENDEDOR: ventas, créditos, clientes y consultas.<br>"
                + "ADMINISTRADOR: además compras, productos, bajas, reportes completos y usuarios.</html>"), false);
        d.mostrar(() -> {
            String c1 = new String(contrasena.getPassword());
            if (!c1.equals(new String(repetir.getPassword()))) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            Usuario u = new Usuario(nombre.getText(), null, PanelClientes.vacioANull(celular.getText()),
                    usuario.getText(), c1, (Rol) rol.getSelectedItem());
            dao.insertar(u);
        });
    }

    @Override
    protected void editar(Usuario u) throws Exception {
        DialogoFormulario d = new DialogoFormulario(this, "Editar usuario");
        Formulario f = d.getFormulario();
        JTextField nombre = f.agregar("Nombre completo *", new JTextField(u.getNombre(), 24));
        JTextField usuario = f.agregar("Usuario (para entrar) *", new JTextField(u.getNombreUsuario()));
        JComboBox<Rol> rol = f.agregar("Rol", new JComboBox<>(Rol.values()));
        rol.setSelectedItem(u.getRol());
        JTextField celular = f.agregar("Celular", new JTextField(u.getCelular()));
        d.mostrar(() -> {
            boolean esYo = u.getIdPersona() == Sesion.getUsuarioActual().getIdPersona();
            if (esYo && rol.getSelectedItem() != Rol.ADMINISTRADOR) {
                throw new IllegalArgumentException("No puede quitarse a sí mismo el rol de administrador");
            }
            u.setNombre(nombre.getText());
            u.setNombreUsuario(usuario.getText());
            u.setRol((Rol) rol.getSelectedItem());
            u.setCelular(PanelClientes.vacioANull(celular.getText()));
            dao.actualizar(u);
        });
    }

    @Override
    protected void eliminar(Usuario u) throws Exception {
        if (u.getIdPersona() == Sesion.getUsuarioActual().getIdPersona()) {
            throw new IllegalStateException("No puede desactivar su propio usuario");
        }
        dao.eliminar(u.getIdPersona());
    }

    private void cambiarContrasena(Usuario u) {
        DialogoFormulario d = new DialogoFormulario(this, "Contraseña de " + u.getNombreUsuario());
        JPasswordField nueva = d.getFormulario().agregar("Nueva contraseña", new JPasswordField(18));
        JPasswordField repetir = d.getFormulario().agregar("Repetir", new JPasswordField(18));
        if (d.mostrar(() -> {
            String n = new String(nueva.getPassword());
            if (!n.equals(new String(repetir.getPassword()))) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            dao.cambiarContrasena(u.getIdPersona(), n);
        })) {
            Dialogos.info(this, "Contraseña actualizada");
        }
    }
}
