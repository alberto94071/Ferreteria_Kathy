package com.ferreteria.vista;

import com.ferreteria.dao.UsuarioDAO;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Ventana de inicio de sesión.
 */
public class LoginFrame extends JFrame {

    private final JTextField campoUsuario = Estilo.campo("Usuario");
    private final JPasswordField campoContrasena = new JPasswordField();
    private final JLabel mensaje = new JLabel(" ");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        super(Conexion.nombreNegocio() + " — Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearLadoIzquierdo(), BorderLayout.WEST);
        raiz.add(crearFormulario(), BorderLayout.CENTER);
        setContentPane(raiz);

        setSize(780, 460);
        setLocationRelativeTo(null);
    }

    private JPanel crearLadoIzquierdo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Estilo.MENU_FONDO);
        p.setBorder(Estilo.borde(48, 40, 32, 40));
        p.setPreferredSize(new Dimension(320, 0));

        Logo logo = new Logo(56);
        logo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(logo);
        p.add(Box.createVerticalStrut(20));

        JLabel nombre = new JLabel("<html>" + Conexion.nombreNegocio() + "</html>");
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 24f));
        nombre.setForeground(Color.WHITE);
        nombre.setAlignmentX(LEFT_ALIGNMENT);
        p.add(nombre);
        p.add(Box.createVerticalStrut(12));

        JLabel descripcion = new JLabel("<html>Sistema de inventario, ventas, compras y créditos.</html>");
        descripcion.setForeground(Estilo.MENU_TEXTO);
        descripcion.setAlignmentX(LEFT_ALIGNMENT);
        p.add(descripcion);
        p.add(Box.createVerticalGlue());

        JLabel version = new JLabel("Versión 1.0");
        version.setForeground(new Color(0x94A3B8));
        version.setFont(version.getFont().deriveFont(12f));
        version.setAlignmentX(LEFT_ALIGNMENT);
        p.add(version);
        return p;
    }

    private JPanel crearFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(Estilo.borde(40, 56, 40, 56));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 4, 0);

        g.gridy = 0;
        p.add(Estilo.titulo("Iniciar sesión"), g);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 22, 0);
        p.add(Estilo.suave("Ingrese su usuario y contraseña"), g);

        g.insets = new Insets(0, 0, 6, 0);
        g.gridy = 2;
        p.add(Estilo.etiqueta("Usuario"), g);
        g.gridy = 3;
        g.insets = new Insets(0, 0, 14, 0);
        campoUsuario.setPreferredSize(new Dimension(0, 38));
        p.add(campoUsuario, g);

        g.insets = new Insets(0, 0, 6, 0);
        g.gridy = 4;
        p.add(Estilo.etiqueta("Contraseña"), g);
        g.gridy = 5;
        g.insets = new Insets(0, 0, 10, 0);
        campoContrasena.setPreferredSize(new Dimension(0, 38));
        campoContrasena.putClientProperty("JTextField.placeholderText", "Contraseña");
        campoContrasena.putClientProperty("JTextField.showRevealButton", true);
        p.add(campoContrasena, g);

        g.gridy = 6;
        mensaje.setForeground(Estilo.PELIGRO);
        mensaje.setFont(mensaje.getFont().deriveFont(12f));
        p.add(mensaje, g);

        g.gridy = 7;
        g.insets = new Insets(8, 0, 0, 0);
        JButton entrar = Estilo.alto(Estilo.botonPrimario("Entrar"), 44);
        p.add(entrar, g);

        g.gridy = 8;
        g.weighty = 1;
        p.add(Box.createVerticalGlue(), g);

        entrar.addActionListener(e -> entrar());
        getRootPane().setDefaultButton(entrar);
        campoUsuario.addActionListener(e -> campoContrasena.requestFocus());
        return p;
    }

    private void entrar() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mensaje.setText("Ingrese usuario y contraseña");
            return;
        }
        try {
            Usuario u = usuarioDAO.autenticar(usuario, contrasena);
            if (u == null) {
                mensaje.setText("Usuario o contraseña incorrectos");
                campoContrasena.setText("");
                campoContrasena.requestFocus();
                return;
            }
            Sesion.iniciar(u);
            new VentanaPrincipal().setVisible(true);
            dispose();
        } catch (SQLException ex) {
            Dialogos.error(this, "No se pudo conectar a la base de datos.\n\n"
                    + "Verifique que MySQL esté encendido y que los datos de\n"
                    + "config.properties sean correctos.\n\n"
                    + Conexion.descripcion() + "\n\nDetalle: " + ex.getMessage());
        }
    }
}
