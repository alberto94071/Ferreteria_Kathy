package com.ferreteria.api;

import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.dao.CreditoDAO;
import com.ferreteria.dao.MarcaDAO;
import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.dao.UsuarioDAO;
import com.ferreteria.dao.VentaDAO;
import com.ferreteria.modelo.Abono;
import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.Cliente;
import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.Marca;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.util.Conexion;
import com.ferreteria.util.Formato;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Todas las rutas de la API. Cada una devuelve JSON.
 *
 *   GET  /api/ping                          ¿está encendido el servidor?
 *   POST /api/login      {usuario, contrasena}   -> {token, nombre, rol}
 *   POST /api/logout
 *   GET  /api/resumen                        ventas de hoy, bajo stock, créditos
 *   GET  /api/productos?buscar=texto
 *   GET  /api/productos/bajo-stock
 *   GET  /api/productos/codigo?valor=ABC     (código de barras)
 *   GET  /api/creditos?filtro=pendientes|vencidos
 *   GET  /api/creditos/detalle?id=5
 *   POST /api/creditos/abono  {idCredito, monto}
 *   GET  /api/catalogos                      marcas, categorías y unidades para el formulario
 *   POST /api/productos/nuevo  {codigo, nombre, unidad, presentacion, idMarca, idCategoria,
 *                               precioCompra, precioVenta, existencia, minimo}
 *
 * Todas excepto ping y login piden el encabezado  Authorization: Bearer <token>
 */
final class Rutas {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    // Sesiones abiertas desde celulares: token -> usuario
    private static final Map<String, Usuario> SESIONES = new ConcurrentHashMap<>();

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final ProductoDAO productoDAO = new ProductoDAO();
    private static final VentaDAO ventaDAO = new VentaDAO();
    private static final CreditoDAO creditoDAO = new CreditoDAO();
    private static final MarcaDAO marcaDAO = new MarcaDAO();
    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private static final String[] UNIDADES = {
        "unidad", "libra", "kilo", "quintal", "metro", "pie", "yarda", "galón", "litro",
        "caja", "saco", "rollo", "par", "juego", "bolsa", "cubeta"
    };

    private Rutas() {
    }

    static void registrar(HttpServer s) {
        s.createContext("/api/ping", ruta("GET", false, p -> mapa(
                "ok", true, "negocio", Conexion.nombreNegocio(), "version", "1.0")));
        s.createContext("/api/login", ruta("POST", false, Rutas::login));
        s.createContext("/api/logout", ruta("POST", true, p -> {
            SESIONES.remove(p.token);
            return mapa("ok", true);
        }));
        s.createContext("/api/resumen", ruta("GET", true, p -> resumen()));
        s.createContext("/api/productos", ruta("GET", true, Rutas::productos));
        s.createContext("/api/productos/bajo-stock", ruta("GET", true, p -> listaProductos(productoDAO.listarBajoStock(), p)));
        s.createContext("/api/productos/codigo", ruta("GET", true, Rutas::productoPorCodigo));
        s.createContext("/api/creditos", ruta("GET", true, Rutas::creditos));
        s.createContext("/api/creditos/detalle", ruta("GET", true, p -> credito(buscarCredito(p.entero("id")))));
        s.createContext("/api/creditos/abono", ruta("POST", true, Rutas::abonar));
        s.createContext("/api/catalogos", ruta("GET", true, p -> catalogos()));
        s.createContext("/api/productos/nuevo", ruta("POST", true, Rutas::nuevoProducto));
    }

    // ---------- Rutas ----------

    private static Object login(Peticion p) throws Exception {
        String usuario = p.texto("usuario");
        String contrasena = p.texto("contrasena");
        Usuario u = usuarioDAO.autenticar(usuario, contrasena);
        if (u == null) {
            throw new NoAutorizado("Usuario o contraseña incorrectos");
        }
        String token = UUID.randomUUID().toString();
        SESIONES.put(token, u);
        return mapa("token", token, "nombre", u.getNombre(), "usuario", u.getNombreUsuario(),
                "rol", u.getRol().name(), "negocio", Conexion.nombreNegocio());
    }

    private static Object resumen() throws Exception {
        LocalDate hoy = LocalDate.now();
        List<Credito> pendientes = creditoDAO.listarPendientes();
        int vencidos = 0;
        double porCobrar = 0;
        for (Credito c : pendientes) {
            porCobrar += c.getSaldo();
            if (c.estaVencido()) {
                vencidos++;
            }
        }
        return mapa(
                "fecha", Formato.fecha(hoy),
                "vendidoHoy", ventaDAO.totalVendido(hoy, hoy),
                "ventasHoy", ventaDAO.listarPorRango(hoy, hoy).size(),
                "productosBajoStock", productoDAO.listarBajoStock().size(),
                "creditosPendientes", pendientes.size(),
                "creditosVencidos", vencidos,
                "totalPorCobrar", Formato.redondear(porCobrar));
    }

    private static Object productos(Peticion p) throws Exception {
        String texto = p.query("buscar");
        List<Producto> lista = texto.isEmpty() ? productoDAO.listar() : productoDAO.buscar(texto);
        if (lista.size() > 200) {
            lista = lista.subList(0, 200);
        }
        return listaProductos(lista, p);
    }

    private static Object productoPorCodigo(Peticion p) throws Exception {
        String codigo = p.query("valor");
        Producto prod = productoDAO.buscarPorCodigo(codigo);
        if (prod == null || !prod.isActivo()) {
            throw new NoEncontrado("No hay ningún producto con el código " + codigo);
        }
        return producto(prod, p.usuario.esAdministrador());
    }

    private static Object creditos(Peticion p) throws Exception {
        List<Credito> lista = "vencidos".equals(p.query("filtro"))
                ? creditoDAO.listarVencidos() : creditoDAO.listarPendientes();
        List<Object> salida = new ArrayList<>();
        for (Credito c : lista) {
            salida.add(credito(c));
        }
        return salida;
    }

    private static Object abonar(Peticion p) throws Exception {
        Credito c = buscarCredito(p.entero("idCredito"));
        double monto = p.numero("monto");
        creditoDAO.registrarAbono(c, new Abono(monto, p.usuario));
        return credito(c);
    }

    private static Object catalogos() throws Exception {
        List<Object> marcas = new ArrayList<>();
        for (Marca m : marcaDAO.listarActivas()) {
            marcas.add(mapa("id", m.getIdMarca(), "nombre", m.getNombre()));
        }
        List<Object> categorias = new ArrayList<>();
        for (Categoria c : categoriaDAO.listar()) {
            if (c.isActiva()) {
                categorias.add(mapa("id", c.getIdCategoria(), "nombre", c.getNombre()));
            }
        }
        return mapa("marcas", marcas, "categorias", categorias, "unidades", UNIDADES);
    }

    // Crea un producto desde el celular. Las validaciones son las del modelo Producto y del DAO
    // (por ejemplo, no permite dos productos con el mismo código).
    private static Object nuevoProducto(Peticion p) throws Exception {
        Marca marca = null;
        int idMarca = p.enteroOpcional("idMarca");
        if (idMarca > 0) {
            marca = marcaDAO.buscarPorId(idMarca);
        }
        Categoria categoria = null;
        int idCategoria = p.enteroOpcional("idCategoria");
        if (idCategoria > 0) {
            categoria = categoriaDAO.buscarPorId(idCategoria);
        }
        Producto nuevo = new Producto(
                p.textoOpcional("codigo"),
                p.texto("nombre"),
                p.texto("unidad"),
                p.textoOpcional("presentacion"),
                marca,
                categoria,
                p.numero("precioCompra"),
                p.numero("precioVenta"),
                p.numero("existencia"),
                p.numero("minimo"),
                null);
        productoDAO.insertar(nuevo);
        System.out.println("Producto creado desde el celular por " + p.usuario.getNombreUsuario() + ": " + nuevo);
        return producto(nuevo, true);
    }

    private static Credito buscarCredito(int id) throws SQLException {
        Credito c = creditoDAO.buscarPorId(id);
        if (c == null) {
            throw new NoEncontrado("No existe el crédito " + id);
        }
        return c;
    }

    // ---------- Conversión a JSON ----------

    private static List<Object> listaProductos(List<Producto> lista, Peticion p) {
        List<Object> salida = new ArrayList<>();
        for (Producto prod : lista) {
            salida.add(producto(prod, p.usuario.esAdministrador()));
        }
        return salida;
    }

    private static Map<String, Object> producto(Producto p, boolean admin) {
        Map<String, Object> m = mapa(
                "id", p.getIdProducto(),
                "codigo", p.getCodigo(),
                "nombre", p.toString(),
                "unidad", p.getUnidad(),
                "marca", p.getMarca() == null ? null : p.getMarca().getNombre(),
                "categoria", p.getCategoria() == null ? null : p.getCategoria().getNombre(),
                "precioVenta", p.getPrecioVenta(),
                "existencia", p.getCantidadActual(),
                "minimo", p.getExistenciaMinima(),
                "bajoStock", p.estaBajoStock(),
                "esKit", p.esKit());
        if (admin) {
            m.put("precioCompra", p.getPrecioCompra());
        }
        return m;
    }

    private static Map<String, Object> credito(Credito c) {
        Cliente cli = c.getVenta().getCliente();
        List<Object> abonos = new ArrayList<>();
        for (Abono a : c.getAbonos()) {
            abonos.add(mapa("fecha", Formato.fechaHora(a.getFecha()), "monto", a.getMonto(),
                    "recibio", a.getUsuario().getNombreUsuario()));
        }
        return mapa(
                "id", c.getIdCredito(),
                "venta", c.getVenta().getIdVenta(),
                "cliente", cli == null ? "" : cli.getNombre(),
                "celular", cli == null ? null : cli.getCelular(),
                "fechaVenta", Formato.fecha(c.getVenta().getFecha().toLocalDate()),
                "vence", Formato.fecha(c.getFechaVencimiento()),
                "total", Formato.redondear(c.getVenta().getTotal()),
                "abonado", Formato.redondear(c.getTotalAbonado()),
                "saldo", Formato.redondear(c.getSaldo()),
                "vencido", c.estaVencido(),
                "pagado", c.estaPagado(),
                "abonos", abonos);
    }

    private static Map<String, Object> mapa(Object... claveValor) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) {
            m.put((String) claveValor[i], claveValor[i + 1]);
        }
        return m;
    }

    // ---------- Infraestructura ----------

    interface Accion {
        Object atender(Peticion p) throws Exception;
    }

    static class NoAutorizado extends RuntimeException {
        NoAutorizado(String m) {
            super(m);
        }
    }

    static class NoEncontrado extends RuntimeException {
        NoEncontrado(String m) {
            super(m);
        }
    }

    // Envuelve cada ruta: revisa método y token, y convierte errores en respuestas JSON
    private static HttpHandler ruta(String metodo, boolean pideSesion, Accion accion) {
        return ex -> {
            try {
                if (!ex.getRequestMethod().equalsIgnoreCase(metodo)) {
                    enviar(ex, 405, mapa("error", "Método no permitido"));
                    return;
                }
                Peticion p = new Peticion(ex);
                if (pideSesion) {
                    p.usuario = p.token == null ? null : SESIONES.get(p.token);
                    if (p.usuario == null) {
                        throw new NoAutorizado("La sesión expiró. Inicie sesión de nuevo.");
                    }
                }
                enviar(ex, 200, accion.atender(p));
            } catch (NoAutorizado e) {
                enviar(ex, 401, mapa("error", e.getMessage()));
            } catch (NoEncontrado e) {
                enviar(ex, 404, mapa("error", e.getMessage()));
            } catch (IllegalArgumentException | IllegalStateException e) {
                enviar(ex, 400, mapa("error", e.getMessage()));
            } catch (SQLException e) {
                enviar(ex, 500, mapa("error", "Error de base de datos: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                enviar(ex, 500, mapa("error", "Error en el servidor: " + e));
            } finally {
                ex.close();
            }
        };
    }

    private static void enviar(HttpExchange ex, int estado, Object cuerpo) throws IOException {
        byte[] bytes = GSON.toJson(cuerpo).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(estado, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Datos de una petición: parámetros de la URL, cuerpo JSON, token y usuario
    static class Peticion {
        final Map<String, String> parametros = new HashMap<>();
        final JsonObject cuerpo;
        final String token;
        Usuario usuario;

        Peticion(HttpExchange ex) throws IOException {
            String q = ex.getRequestURI().getRawQuery();
            if (q != null) {
                for (String par : q.split("&")) {
                    int i = par.indexOf('=');
                    if (i > 0) {
                        parametros.put(URLDecoder.decode(par.substring(0, i), "UTF-8"),
                                URLDecoder.decode(par.substring(i + 1), "UTF-8"));
                    }
                }
            }
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            token = auth != null && auth.startsWith("Bearer ") ? auth.substring(7).trim() : null;

            String texto = leer(ex.getRequestBody()).trim();
            JsonObject json = new JsonObject();
            if (!texto.isEmpty()) {
                try {
                    JsonElement e = JsonParser.parseString(texto);
                    if (e.isJsonObject()) {
                        json = e.getAsJsonObject();
                    }
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("El cuerpo de la petición no es JSON válido");
                }
            }
            cuerpo = json;
        }

        String query(String nombre) {
            String v = parametros.get(nombre);
            return v == null ? "" : v.trim();
        }

        String texto(String nombre) {
            JsonElement e = cuerpo.get(nombre);
            if (e == null || e.isJsonNull() || e.getAsString().trim().isEmpty()) {
                throw new IllegalArgumentException("Falta el dato '" + nombre + "'");
            }
            return e.getAsString();
        }

        // Dato que puede venir vacío: devuelve null
        String textoOpcional(String nombre) {
            JsonElement e = cuerpo.get(nombre);
            if (e == null || e.isJsonNull() || e.getAsString().trim().isEmpty()) {
                return null;
            }
            return e.getAsString().trim();
        }

        // Número entero que puede venir vacío: devuelve 0
        int enteroOpcional(String nombre) {
            String v = textoOpcional(nombre);
            if (v == null) {
                return 0;
            }
            try {
                return (int) Double.parseDouble(v);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + nombre + "' debe ser un número entero");
            }
        }

        double numero(String nombre) {
            try {
                return Double.parseDouble(texto(nombre));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + nombre + "' debe ser un número");
            }
        }

        int entero(String nombre) {
            String v = cuerpo.has(nombre) ? texto(nombre) : query(nombre);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + nombre + "' debe ser un número entero");
            }
        }

        private static String leer(InputStream in) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}