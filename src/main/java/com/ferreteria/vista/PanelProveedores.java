package com.ferreteria.vista;

import com.ferreteria.dao.ProveedorDAO;
import com.ferreteria.modelo.Proveedor;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;

/**
 * Mantenimiento de proveedores.
 */
public class PanelProveedores extends PanelCrud<Proveedor> {

    private final ProveedorDAO dao = new ProveedorDAO();

    public PanelProveedores() {
        super("Proveedores", "+ Nuevo proveedor", false);
    }

    @Override
    protected void definirColumnas(ModeloTabla<Proveedor> m) {
        m.texto("Empresa", 220, p -> p.getEmpresa())
                .texto("NIT", 100, p -> p.getNit())
                .texto("Contacto", 180, p -> p.getNombre())
                .texto("Celular", 100, p -> p.getCelular())
                .texto("Dirección", 220, p -> p.getDireccion());
    }

    @Override
    protected List<Proveedor> cargar(String filtro) throws Exception {
        List<Proveedor> lista = new ArrayList<>();
        for (Proveedor p : dao.listar()) {
            if (contiene(p.getEmpresa() + " " + p.getNit() + " " + p.getNombre(), filtro)) {
                lista.add(p);
            }
        }
        return lista;
    }

    @Override
    protected void nuevo() throws Exception {
        formulario(null);
    }

    @Override
    protected void editar(Proveedor p) throws Exception {
        formulario(p);
    }

    @Override
    protected void eliminar(Proveedor p) throws Exception {
        dao.eliminar(p.getIdPersona());
    }

    private void formulario(Proveedor existente) {
        DialogoFormulario d = new DialogoFormulario(this, existente == null ? "Nuevo proveedor" : "Editar proveedor");
        Formulario f = d.getFormulario();
        JTextField empresa = f.agregar("Empresa *", new JTextField(24));
        JTextField nit = f.agregar("NIT *", new JTextField());
        JTextField nombre = f.agregar("Nombre del contacto *", new JTextField());
        JTextField celular = f.agregar("Celular", new JTextField());
        JTextField direccion = f.agregar("Dirección", new JTextField());
        if (existente != null) {
            empresa.setText(existente.getEmpresa());
            nit.setText(existente.getNit());
            nombre.setText(existente.getNombre());
            celular.setText(existente.getCelular());
            direccion.setText(existente.getDireccion());
        }
        d.mostrar(() -> {
            Proveedor p = existente == null ? new Proveedor() : existente;
            p.setEmpresa(empresa.getText());
            p.setNit(nit.getText());
            p.setNombre(nombre.getText());
            p.setCelular(PanelClientes.vacioANull(celular.getText()));
            p.setDireccion(PanelClientes.vacioANull(direccion.getText()));
            if (existente == null) {
                dao.insertar(p);
            } else {
                dao.actualizar(p);
            }
        });
    }

    @Override
    protected String preguntaEliminar(Proveedor p) {
        return "¿Desactivar al proveedor \"" + p.getEmpresa() + "\"?";
    }
}
