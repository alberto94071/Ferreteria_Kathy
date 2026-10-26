package com.ferreteria.vista;

import com.ferreteria.api.ServidorApi;
import com.ferreteria.dao.UsuarioDAO;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Sesion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * Ventana principal: menú a la izquierda y la pantalla elegida a la derecha.
 * Las pantallas se crean la primera vez que se abren (CardLayout).
 */
public class VentanaPrincipal extends JFrame {

    public static final String INICIO = "Inicio";
    public static final String VENTAS = "Ventas";
    public static final String COMPRAS = "Compras";
    public static final String PRODUCTOS = "Productos";
    public static final String CREDITOS = "Créditos";
    public static final String CLIENTES = "Clientes";
    public static final String PROVEEDORES = "Proveedores";
    public static final String CATALOGOS = "Categorías y marcas";
    public static final String BAJAS = "Bajas de inventario";
    public static final String REPORTES = "Reportes";
    public static final String USUARIOS = "Usuarios";

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final JPanel menu = new JPanel();
    private final Map<String, Supplier<JPanel>> fabricas = new HashMap<>();
    private final Map<String, Pantalla> pantallas = new HashMap<>();
    private final Map<String, JButton> botones = new HashMap<>();
    private String actual;

    public VentanaPrincipal() {
        super(Conexion.nombreNegocio() + " — Sistema de Inventario y Ventas");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Dialogos.confirmar(VentanaPrincipal.this, "¿Desea salir del sistema?")) {
                    System.exit(0);
                }
            }
        });

        boolean admin = Sesion.esAdministrador();
        construirMenu();
        registrar(INICIO, () -> new PanelInicio(this), true);
        registrar(VENTAS, PanelVentas::new, true);
        registrar(COMPRAS, PanelCompras::new, admin);
        registrar(PRODUCTOS, PanelProductos::new, true);
        registrar(CREDITOS, PanelCreditos::new, true);
        registrar(CLIENTES, PanelClientes::new, true);
        registrar(PROVEEDORES, PanelProveedores::new, admin);
        registrar(CATALOGOS, PanelCatalogos::new, admin);
        registrar(BAJAS, PanelBajas::new, admin);
        registrar(REPORTES, PanelReportes::new, true);
        registrar(USUARIOS, PanelUsuarios::new, admin);
        terminarMenu();

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(menu, BorderLayout.WEST);
        contenido.setBackground(Estilo.FONDO);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        atajo(KeyEvent.VK_F1, INICIO);
        atajo(KeyEvent.VK_F2, VENTAS);
        atajo(KeyEvent.VK_F3, PRODUCTOS);
        atajo(KeyEvent.VK_F4, CREDITOS);

        setMinimumSize(new Dimension(1000, 640));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
        mostrar(INICIO);
    }

    // Abre una pantalla del menú y recarga sus datos
    public void mostrar(String clave) {
        if (!fabricas.containsKey(clave)) {
            return;
        }
        Pantalla p = pantallas.get(clave);
        if (p == null) {
            JPanel panel = fabricas.get(clave).get();
            p = (Pantalla) panel;
            pantallas.put(clave, p);
            contenido.add(panel, clave);
        }
        cartas.show(contenido, clave);
        actual = clave;
        for (Map.Entry<String, JButton> e : botones.entrySet()) {
            pintarBoton(e.getValue(), e.getKey().equals(clave), false);
        }
        p.refrescar();
    }

    // ---------- Menú lateral ----------

    private void construirMenu() {
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Estilo.MENU_FONDO);
        menu.setBorder(Estilo.borde(16, 12, 16, 12));
        menu.setPreferredSize(new Dimension(210, 0));

        JPanel marca = new JPanel(new BorderLayout(10, 0));
        marca.setOpaque(false);
        marca.add(new Logo(32), BorderLayout.WEST);
        JLabel nombre = new JLabel(Conexion.nombreNegocio());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 15f));
        marca.add(nombre, BorderLayout.CENTER);
        marca.setAlignmentX(LEFT_ALIGNMENT);
        marca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        menu.add(marca);
        menu.add(Box.createVerticalStrut(18));
    }

    private void registrar(String clave, Supplier<JPanel> fabrica, boolean visible) {
        if (!visible) {
            return;
        }
        fabricas.put(clave, fabrica);
        // Botón que pinta su propio fondo redondeado (FlatLaf no lo pinta sin "content area")
        JButton b = new JButton(clave) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(Estilo.borde(9, 12, 9, 12));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.addActionListener(e -> mostrar(clave));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pintarBoton(b, clave.equals(actual), true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pintarBoton(b, clave.equals(actual), false);
            }
        });
        pintarBoton(b, false, false);
        botones.put(clave, b);
        menu.add(b);
        menu.add(Box.createVerticalStrut(2));
    }

    private void pintarBoton(JButton b, boolean activo, boolean encima) {
        if (activo) {
            b.setBackground(Estilo.ACENTO);
            b.setForeground(Color.WHITE);
            b.setFont(b.getFont().deriveFont(Font.BOLD));
        } else {
            b.setBackground(encima ? Estilo.MENU_BORDE : Estilo.MENU_FONDO);
            b.setForeground(Estilo.MENU_TEXTO);
            b.setFont(b.getFont().deriveFont(Font.PLAIN));
        }
        b.repaint();
    }

    private void terminarMenu() {
        menu.add(Box.createVerticalGlue());

        JPanel pie = new JPanel();
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.MENU_BORDE), Estilo.borde(12, 8, 0, 8)));
        pie.setAlignmentX(LEFT_ALIGNMENT);
        pie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        Usuario u = Sesion.getUsuarioActual();
        JLabel nombre = new JLabel(u.getNombre());
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        JLabel rol = new JLabel(u.getRol().name());
        rol.setForeground(new Color(0x94A3B8));
        rol.setFont(rol.getFont().deriveFont(11f));
        pie.add(nombre);
        pie.add(rol);
        pie.add(Box.createVerticalStrut(8));

        // Dirección que se escribe en la app del celular
        JLabel app = new JLabel(ServidorApi.activo()
                ? "<html>App móvil:<br><b>" + ServidorApi.direccion() + "</b></html>"
                : "<html>App móvil no disponible</html>");
        app.setForeground(new Color(0x94A3B8));
        app.setFont(app.getFont().deriveFont(11f));
        app.setToolTipText(ServidorApi.activo() ? "Escriba esta dirección en la app del celular"
                : ServidorApi.getError());
        pie.add(app);
        pie.add(Box.createVerticalStrut(8));
        pie.add(enlace("Cambiar mi contraseña", e -> cambiarContrasena()));
        pie.add(enlace("Cerrar sesión", e -> cerrarSesion()));
        menu.add(pie);
    }

    private JButton enlace(String texto, java.awt.event.ActionListener accion) {
        JButton b = new JButton(texto);
        b.setBorder(Estilo.borde(3, 0, 3, 0));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(0xFDBA74));
        b.setFont(b.getFont().deriveFont(12f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(accion);
        return b;
    }

    private void atajo(int tecla, String clave) {
        getRootPane().registerKeyboardAction(e -> mostrar(clave),
                KeyStroke.getKeyStroke(tecla, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ---------- Sesión ----------

    private void cerrarSesion() {
        if (Dialogos.confirmar(this, "¿Cerrar la sesión?")) {
            Sesion.cerrar();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void cambiarContrasena() {
        DialogoFormulario d = new DialogoFormulario(this, "Cambiar mi contraseña");
        JPasswordField actualCampo = d.getFormulario().agregar("Contraseña actual", new JPasswordField(18));
        JPasswordField nueva = d.getFormulario().agregar("Nueva contraseña", new JPasswordField(18));
        JPasswordField repetir = d.getFormulario().agregar("Repetir nueva", new JPasswordField(18));
        boolean ok = d.mostrar(() -> {
            Usuario u = Sesion.getUsuarioActual();
            if (!u.verificarContrasena(new String(actualCampo.getPassword()))) {
                throw new IllegalArgumentException("La contraseña actual no es correcta");
            }
            String n = new String(nueva.getPassword());
            if (!n.equals(new String(repetir.getPassword()))) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
            }
            new UsuarioDAO().cambiarContrasena(u.getIdPersona(), n);
            u.cambiarContrasena(n);
        });
        if (ok) {
            Dialogos.info(this, "Contraseña actualizada");
        }
    }
}