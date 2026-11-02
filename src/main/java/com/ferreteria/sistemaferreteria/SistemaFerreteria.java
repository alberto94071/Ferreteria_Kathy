package com.ferreteria.sistemaferreteria;

import com.ferreteria.dao.CategoriaDAO;
import com.ferreteria.modelo.Categoria;
import java.sql.SQLException;

public class SistemaFerreteria {

    public static void main(String[] args) {
        CategoriaDAO dao = new CategoriaDAO();
        try {
            Categoria c = new Categoria("Herramientas", "Martillos y llaves");
            dao.insertar(c);
            System.out.println("CREATE -> id " + c.getIdCategoria());

            Categoria buscada = dao.buscarPorId(c.getIdCategoria());
            System.out.println("READ -> " + buscada.getNombre());

            buscada.setDescripcion("Martillos, llaves y desarmadores");
            dao.actualizar(buscada);
            System.out.println("UPDATE -> " + dao.buscarPorId(c.getIdCategoria()).getDescripcion());

            dao.eliminar(c.getIdCategoria());
            System.out.println("DELETE -> activa = " + dao.buscarPorId(c.getIdCategoria()).isActiva());

            for (Categoria cat : dao.listar()) {
                System.out.println(cat.getIdCategoria() + " - " + cat + " - activa: " + cat.isActiva());
            }
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Dato inválido: " + e.getMessage());
        }
    }
}