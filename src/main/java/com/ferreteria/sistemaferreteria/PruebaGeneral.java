package com.ferreteria.sistemaferreteria;

import com.ferreteria.dao.BajaInventarioDAO;
import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.dao.ClienteDAO;
import com.ferreteria.dao.CompraDAO;
import com.ferreteria.dao.CreditoDAO;
import com.ferreteria.dao.MarcaDAO;
import com.ferreteria.dao.ProductoDAO;
import com.ferreteria.dao.ProveedorDAO;
import com.ferreteria.dao.UsuarioDAO;
import com.ferreteria.dao.VentaDAO;
import com.ferreteria.modelo.Abono;
import com.ferreteria.modelo.BajaInventario;
import com.ferreteria.modelo.Categoria;
import com.ferreteria.modelo.Cliente;
import com.ferreteria.modelo.ComponenteKit;
import com.ferreteria.modelo.Compra;
import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.DetalleCompra;
import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Kit;
import com.ferreteria.modelo.Marca;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Proveedor;
import com.ferreteria.modelo.Usuario;
import com.ferreteria.modelo.Venta;
import com.ferreteria.util.Formato;
import com.ferreteria.util.Sesion;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Prueba de punta a punta de todos los DAO.
 * Ejecutar sobre una base de datos de pruebas (crea registros).
 */
public class PruebaGeneral {

    public static void main(String[] args) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        MarcaDAO marcaDAO = new MarcaDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        CompraDAO compraDAO = new CompraDAO();
        VentaDAO ventaDAO = new VentaDAO();
        CreditoDAO creditoDAO = new CreditoDAO();
        BajaInventarioDAO bajaDAO = new BajaInventarioDAO();

        try {
            // 1. Login
            Usuario admin = usuarioDAO.autenticar("admin", "admin123");
            if (admin == null) {
                System.out.println("No se pudo iniciar sesión con admin / admin123");
                return;
            }
            Sesion.iniciar(admin);
            System.out.println("1. Sesión iniciada: " + Sesion.getUsuarioActual().getNombre()
                    + " | admin = " + Sesion.esAdministrador());

            // 2. Catálogos
            long sufijo = System.currentTimeMillis() % 100000;
            Categoria cat = new Categoria("Tornillería " + sufijo, "Tornillos, clavos y tuercas");
            categoriaDAO.insertar(cat);
            Marca marca = new Marca("Truper " + sufijo, "Herramienta mexicana");
            marcaDAO.insertar(marca);
            Proveedor prov = new Proveedor("Juan López", "Zona 1, San Marcos", "55554444",
                    "CF" + sufijo, "Distribuidora El Martillo");
            proveedorDAO.insertar(prov);
            Proveedor prov2 = new Proveedor("Ana Pérez", "Quetzaltenango", "44443333",
                    "NIT" + sufijo, "Ferremayoreo Xela");
            proveedorDAO.insertar(prov2);
            Cliente cli = new Cliente("Pedro Ramírez", "San Pedro Sac.", "33332222", 1000, 30);
            clienteDAO.insertar(cli);
            System.out.println("2. Catálogos creados. Cliente id " + cli.getIdPersona()
                    + ", proveedor por NIT: " + proveedorDAO.buscarPorNit("cf" + sufijo));

            // 3. Productos y kit
            Producto clavo = new Producto("CLV" + sufijo, "Clavo", "libra", "2 pulgadas",
                    marca, cat, 8.00, 12.00, 0, 5, null);
            productoDAO.insertar(clavo);
            Producto tornillo = new Producto("TOR" + sufijo, "Tornillo", "unidad", "1/4",
                    marca, cat, 0.50, 1.00, 0, 50, null);
            productoDAO.insertar(tornillo);
            Kit kit = new Kit("KIT" + sufijo, "Kit de fijación", "caja", "100 piezas",
                    marca, cat, 60.00, 95.00, 0, 1, null);
            kit.agregarComponente(new ComponenteKit(tornillo, 80, 1));
            kit.agregarComponente(new ComponenteKit(clavo, 2, 1));
            productoDAO.insertar(kit);
            Producto kitLeido = productoDAO.buscarPorId(kit.getIdProducto());
            System.out.println("3. Kit leído es Kit: " + kitLeido.esKit() + ", componentes: "
                    + ((Kit) kitLeido).getComponentes().size() + ", marca: " + kitLeido.getMarca()
                    + ", búsqueda 'clavo': " + productoDAO.buscar("clavo").size());

            // 4. Compras (dos proveedores, precios distintos)
            Compra compra = new Compra(prov, admin, false);
            compra.agregarDetalle(new DetalleCompra(clavo, 20, 8.50));
            compra.agregarDetalle(new DetalleCompra(tornillo, 100, 0.45));
            compra.agregarDetalle(new DetalleCompra(kit, 3, 60.00));
            compraDAO.registrar(compra);
            Compra compra2 = new Compra(prov2, admin, true);
            compra2.agregarDetalle(new DetalleCompra(clavo, 10, 7.90));
            compraDAO.registrar(compra2);
            System.out.println("4. Compras " + compra.getIdCompra() + " y " + compra2.getIdCompra()
                    + ". Existencia clavo: " + productoDAO.buscarPorId(clavo.getIdProducto()).getCantidadActual()
                    + " | último precio: " + compraDAO.ultimoPrecioCompra(clavo.getIdProducto())
                    + " | más barato: " + compraDAO.proveedorMasBarato(clavo.getIdProducto())
                    + " | total compra 1 leída: " + Formato.dinero(compraDAO.buscarPorId(compra.getIdCompra()).getTotal()));

            // 5. Desarmar kit
            Kit kitBD = (Kit) productoDAO.buscarPorId(kit.getIdProducto());
            productoDAO.desarmarKit(kitBD);
            System.out.println("5. Kit desarmado. Kits: "
                    + productoDAO.buscarPorId(kit.getIdProducto()).getCantidadActual()
                    + ", tornillos: " + productoDAO.buscarPorId(tornillo.getIdProducto()).getCantidadActual()
                    + ", costo por tornillo dentro del kit: "
                    + Formato.dinero(kitBD.calcularCostoComponente(kitBD.getComponentes().get(0))));

            // 6. Venta de contado (consumidor final)
            Producto clavoBD = productoDAO.buscarPorId(clavo.getIdProducto());
            Venta contado = new Venta(null, admin, false);
            contado.agregarDetalle(new DetalleVenta(clavoBD, 3, 12.00));
            ventaDAO.registrar(contado, 0);
            System.out.println("6. Venta contado " + contado.getIdVenta() + " total "
                    + Formato.dinero(contado.getTotal()));

            // 7. Venta al crédito
            clavoBD = productoDAO.buscarPorId(clavo.getIdProducto());
            Producto tornilloBD = productoDAO.buscarPorId(tornillo.getIdProducto());
            Venta credito = new Venta(cli, admin, true);
            credito.agregarDetalle(new DetalleVenta(clavoBD, 5, 12.00));
            credito.agregarDetalle(new DetalleVenta(tornilloBD, 40, 1.00));
            ventaDAO.registrar(credito, 0);
            System.out.println("7. Venta crédito " + credito.getIdVenta() + " total "
                    + Formato.dinero(credito.getTotal()) + " | deuda cliente: "
                    + Formato.dinero(clienteDAO.obtenerDeudaPendiente(cli.getIdPersona())));

            // 8. Segundo crédito debe ser rechazado (tiene saldo)
            try {
                Venta otra = new Venta(cli, admin, true);
                otra.agregarDetalle(new DetalleVenta(productoDAO.buscarPorId(clavo.getIdProducto()), 1, 12));
                ventaDAO.registrar(otra, 0);
                System.out.println("8. ERROR: se permitió un segundo crédito");
            } catch (IllegalStateException e) {
                System.out.println("8. Segundo crédito rechazado: " + e.getMessage());
            }

            // 9. Abonos
            Credito cr = creditoDAO.buscarPorVenta(credito.getIdVenta());
            System.out.println("9. Crédito " + cr.getIdCredito() + " vence " + Formato.fecha(cr.getFechaVencimiento())
                    + " saldo " + Formato.dinero(cr.getSaldo()) + " | pendientes: "
                    + creditoDAO.listarPendientes().size() + " | vencidos: " + creditoDAO.listarVencidos().size());
            creditoDAO.registrarAbono(cr, new Abono(50, admin));
            try {
                creditoDAO.registrarAbono(cr, new Abono(1000, admin));
            } catch (IllegalArgumentException e) {
                System.out.println("   Abono excesivo rechazado: " + e.getMessage());
            }
            creditoDAO.registrarAbono(cr, new Abono(cr.getSaldo(), admin));
            Credito crBD = creditoDAO.buscarPorId(cr.getIdCredito());
            System.out.println("   Abonos: " + crBD.getAbonos().size() + ", pagado: " + crBD.estaPagado()
                    + ", deuda cliente: " + Formato.dinero(clienteDAO.obtenerDeudaPendiente(cli.getIdPersona())));

            // 10. Baja de inventario
            BajaInventario baja = new BajaInventario(productoDAO.buscarPorId(tornillo.getIdProducto()),
                    5, "Tornillos oxidados", admin);
            bajaDAO.registrar(baja);
            System.out.println("10. Baja " + baja.getIdBaja() + ". Tornillos ahora: "
                    + productoDAO.buscarPorId(tornillo.getIdProducto()).getCantidadActual()
                    + " | bajas hoy: " + bajaDAO.listarPorRango(LocalDate.now(), LocalDate.now()).size());

            // 11. Anular venta de contado
            double antes = productoDAO.buscarPorId(clavo.getIdProducto()).getCantidadActual();
            ventaDAO.anular(contado.getIdVenta());
            double despues = productoDAO.buscarPorId(clavo.getIdProducto()).getCantidadActual();
            System.out.println("11. Venta anulada. Clavos " + antes + " -> " + despues);
            try {
                ventaDAO.anular(credito.getIdVenta());
            } catch (IllegalStateException e) {
                System.out.println("    Anular venta con abonos rechazado: " + e.getMessage());
            }

            // 12. Reportes
            System.out.println("12. Total vendido hoy: " + Formato.dinero(ventaDAO.totalVendido(LocalDate.now(), LocalDate.now()))
                    + " | ventas hoy: " + ventaDAO.listarPorRango(LocalDate.now(), LocalDate.now()).size()
                    + " | ventas del cliente: " + ventaDAO.listarPorCliente(cli.getIdPersona()).size()
                    + " | bajo stock: " + productoDAO.listarBajoStock().size()
                    + " | compras hoy: " + compraDAO.listarPorRango(LocalDate.now(), LocalDate.now()).size());

            // 13. Venta sin existencia suficiente debe fallar y no dejar nada a medias
            try {
                Producto c = productoDAO.buscarPorId(clavo.getIdProducto());
                c.setCantidadActual(9999); // simula objeto desactualizado en pantalla
                Venta v = new Venta(null, admin, false);
                v.agregarDetalle(new DetalleVenta(c, 500, 12));
                ventaDAO.registrar(v, 0);
                System.out.println("13. ERROR: se vendió sin existencia");
            } catch (IllegalArgumentException e) {
                System.out.println("13. Venta sin existencia rechazada: " + e.getMessage());
            }

            // 14. Usuarios
            Usuario vendedor = new Usuario("María Vendedora", null, null, "maria" + sufijo, "clave123",
                    com.ferreteria.modelo.Rol.VENDEDOR);
            usuarioDAO.insertar(vendedor);
            usuarioDAO.cambiarContrasena(vendedor.getIdPersona(), "nueva456");
            System.out.println("14. Login vendedor con clave nueva: "
                    + (usuarioDAO.autenticar("maria" + sufijo, "nueva456") != null)
                    + " | con clave vieja: " + (usuarioDAO.autenticar("maria" + sufijo, "clave123") != null));

            Sesion.cerrar();
            System.out.println("Sesión cerrada: " + !Sesion.haySesion());
            System.out.println("PRUEBA COMPLETA");
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
