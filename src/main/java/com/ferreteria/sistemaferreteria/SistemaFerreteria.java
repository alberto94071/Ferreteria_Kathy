
package com.ferreteria.sistemaferreteria;

import com.ferreteria.util.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public class SistemaFerreteria {

    public static void main(String[] args) {
    try (Connection con = Conexion.getConexion()) {
        System.out.println("Conexion exitosa a: " + con.getCatalog());
    } catch (SQLException e) {
        System.out.println("Error al conectar: " + e.getMessage());
    }
}
}
