package com.ferreteria.vista;

import com.ferreteria.dao.ClienteDAO;
import com.ferreteria.modelo.Cliente;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextField;

/**
 * Mantenimiento de clientes.
 */
public class PanelClientes extends PanelCrud<Cliente> {

    private final ClienteDAO dao = new ClienteDAO();
    private final Map<Integer, Double> deudas = new HashMap<>();

    public PanelClientes() {
        super("Clientes", "+ Nuevo cliente", false);
    }

    @Override
    protected void definirColumnas(ModeloTabla<Cliente> m) {
        m.texto("Nombre", 220, c -> c.getNombre())
                .texto("Celular", 100, c -> c.getCelular())
                .texto("Dirección", 220, c -> c.getDireccion())
                .dinero("Límite de crédito", 120, c -> c.getLimiteCredito())
                .entero("Días crédito", 90, c -> c.getDiasCredito())
                .dinero("Deuda actual", 110, c -> deudas.get(c.getIdPersona()))
                .colorTexto(c -> {
                    Double d = deudas.get(c.getIdPersona());
                    return d != null && d > 0 ? Estilo.PELIGRO : null;
                });
    }

    @Override
    protected List<Cliente> cargar(String filtro) throws Exception {
        List<Cliente> lista = filtro.isEmpty() ? dao.listar() : dao.buscarPorNombre(filtro);
        deudas.clear();
        for (Cliente c : lista) {
            deudas.put(c.getIdPersona(), dao.obtenerDeudaPendiente(c.getIdPersona()));
        }
        return lista;
    }

    @Override
    protected void nuevo() throws Exception {
        formulario(null);
    }

    @Override
    protected void editar(Cliente c) throws Exception {
        formulario(c);
    }

    @Override
    protected void eliminar(Cliente c) throws Exception {
        dao.eliminar(c.getIdPersona());
    }

    private void formulario(Cliente existente) {
        DialogoFormulario d = new DialogoFormulario(this, existente == null ? "Nuevo cliente" : "Editar cliente");
        Formulario f = d.getFormulario();
        JTextField nombre = f.agregar("Nombre *", new JTextField(24));
        JTextField celular = f.agregar("Celular", new JTextField());
        JTextField direccion = f.agregar("Dirección", new JTextField());
        JTextField limite = f.agregar("Límite de crédito (Q)", Estilo.campoNumero("0.00"));
        JTextField dias = f.agregar("Días de crédito", Estilo.campoNumero("30"));
        f.agregarCompleto(Estilo.suave("<html>Deje el límite en 0 si el cliente no tiene crédito autorizado.</html>"), false);
        if (existente != null) {
            nombre.setText(existente.getNombre());
            celular.setText(existente.getCelular());
            direccion.setText(existente.getDireccion());
            limite.setText(Estilo.numeroPlano(existente.getLimiteCredito()));
            dias.setText(String.valueOf(existente.getDiasCredito()));
        }
        d.mostrar(() -> {
            Cliente c = existente == null ? new Cliente() : existente;
            c.setNombre(nombre.getText());
            c.setCelular(vacioANull(celular.getText()));
            c.setDireccion(vacioANull(direccion.getText()));
            c.setLimiteCredito(Estilo.leerNumero(limite, "el límite de crédito"));
            c.setDiasCredito(Estilo.leerEntero(dias, "los días de crédito"));
            if (existente == null) {
                dao.insertar(c);
            } else {
                dao.actualizar(c);
            }
        });
    }

    static String vacioANull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
