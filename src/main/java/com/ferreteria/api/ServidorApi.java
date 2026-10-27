package com.ferreteria.api;

import com.ferreteria.util.Conexion;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.Executors;

/**
 * Servidor web pequeño para la app del celular.
 *
 * Arranca junto con el programa de escritorio y escucha en el puerto de
 * config.properties (api_puerto, 8080 por defecto). El celular se conecta
 * por el Wi-Fi de la tienda a  http://IP-de-esta-computadora:8080/api/...
 *
 * No necesita internet. Usa los mismos DAO que la interfaz de escritorio.
 */
public final class ServidorApi {

    private static HttpServer servidor;
    private static int puerto;
    private static String error;

    private ServidorApi() {
    }

    public static void iniciar() {
        if (servidor != null) {
            return;
        }
        if ("false".equalsIgnoreCase(Conexion.propiedad("api_activa", "true"))) {
            error = "desactivada en config.properties";
            return;
        }
        try {
            puerto = Integer.parseInt(Conexion.propiedad("api_puerto", "8080"));
            servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
            Rutas.registrar(servidor);
            servidor.setExecutor(Executors.newFixedThreadPool(8));
            servidor.start();
            System.out.println("API para la app móvil en " + direccion());
        } catch (IOException | NumberFormatException e) {
            servidor = null;
            error = e.getMessage();
            System.out.println("No se pudo iniciar la API: " + e.getMessage());
        }
    }

    public static void detener() {
        if (servidor != null) {
            servidor.stop(0);
            servidor = null;
        }
    }

    public static boolean activo() {
        return servidor != null;
    }

    public static String getError() {
        return error;
    }

    // "192.168.1.10:8080": lo que se escribe en la app del celular
    public static String direccion() {
        return ipLocal() + ":" + puerto;
    }

    // Busca la IP de esta computadora en la red local (Wi-Fi o cable)
    public static String ipLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> direcciones = ni.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress a = direcciones.nextElement();
                    if (a instanceof Inet4Address && a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // se usa el valor de abajo
        }
        return "localhost";
    }
}