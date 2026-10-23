package com.ferreteria.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mysql://localhost:3306/ferreteria";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "root";
    
    private  Conexion(){
    }
    
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
    
    
}