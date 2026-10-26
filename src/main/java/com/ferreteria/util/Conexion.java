package com.ferreteria.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones a MySQL.
 * Los datos de conexión se leen del archivo config.properties que está
 * junto al programa. Si no existe, se usan los valores por defecto
 * (localhost, usuario root, contraseña root).
 */
public class Conexion {

    private static final String ARCHIVO = "config.properties";

    private static Properties config;
    private static String url;

    private Conexion() {
    }

    public static Connection getConexion() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(url,
                config.getProperty("usuario", "root"),
                config.getProperty("contrasena", "root"));
    }

    // Nombre que aparece en las ventanas (propiedad nombre_negocio)
    public static String nombreNegocio() {
        cargarConfiguracion();
        return config.getProperty("nombre_negocio", "Ferretería");
    }

    // Cualquier otra propiedad de config.properties (por ejemplo api_puerto)
    public static String propiedad(String clave, String porDefecto) {
        cargarConfiguracion();
        return config.getProperty(clave, porDefecto).trim();
    }

    // Texto para mostrar en mensajes de error (sin la contraseña)
    public static String descripcion() {
        cargarConfiguracion();
        return url.substring(0, url.indexOf('?')) + " (usuario " + config.getProperty("usuario", "root") + ")";
    }

    private static void cargarConfiguracion() {
        if (config != null) {
            return;
        }
        config = new Properties();
        File archivo = new File(ARCHIVO);
        if (archivo.exists()) {
            try {
                config.load(new StringReader(leerTexto(archivo)));
            } catch (IOException e) {
                System.out.println("No se pudo leer " + ARCHIVO + ": " + e.getMessage());
            }
        }
        url = "jdbc:mysql://" + config.getProperty("servidor", "localhost")
                + ":" + config.getProperty("puerto", "3306")
                + "/" + config.getProperty("base_de_datos", "ferreteria")
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=America/Guatemala&characterEncoding=UTF-8";
    }

    // El Bloc de notas de Windows 7 guarda en ANSI; si el archivo no es UTF-8 válido se lee como Windows-1252
    private static String leerTexto(File archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo.toPath());
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            texto = new String(bytes, Charset.forName("windows-1252"));
        }
        if (texto.startsWith("﻿")) {
            texto = texto.substring(1);
        }
        return texto;
    }
}