package com.ferreteria.movil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Créditos pendientes o vencidos. Al tocar uno se ve el detalle y se puede registrar un abono.
 */
public class CreditosActivity extends BaseActivity {

    public static final String EXTRA_VENCIDOS = "vencidos";

    private final List<JSONObject> creditos = new ArrayList<>();
    private Adaptador adaptador;
    private SwipeRefreshLayout refrescar;
    private TextView txtResumen;
    private boolean vencidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditos);
        activarRegresar();
        setTitle("Créditos");
        vencidos = getIntent().getBooleanExtra(EXTRA_VENCIDOS, false);

        refrescar = findViewById(R.id.refrescar);
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        adaptador = new Adaptador();
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((p, v, pos, id) -> mostrarDetalle(creditos.get(pos)));

        MaterialButtonToggleGroup grupo = findViewById(R.id.grupo);
        grupo.check(vencidos ? R.id.btnVencidos : R.id.btnPendientes);
        grupo.addOnButtonCheckedListener((g, id, marcado) -> {
            if (marcado) {
                vencidos = id == R.id.btnVencidos;
                cargar();
            }
        });
        refrescar.setColorSchemeResources(R.color.acento);
        refrescar.setOnRefreshListener(this::cargar);
        cargar();
    }

    private void cargar() {
        refrescar.setRefreshing(true);
        Api.get(this, "/api/creditos?filtro=" + (vencidos ? "vencidos" : "pendientes"), new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                refrescar.setRefreshing(false);
                JSONArray arreglo = (JSONArray) json;
                creditos.clear();
                double total = 0;
                for (int i = 0; i < arreglo.length(); i++) {
                    JSONObject c = arreglo.optJSONObject(i);
                    creditos.add(c);
                    total += c.optDouble("saldo");
                }
                adaptador.notifyDataSetChanged();
                txtResumen.setText(creditos.isEmpty() ? "No hay créditos " + (vencidos ? "vencidos" : "pendientes")
                        : creditos.size() + " créditos · " + Formato.dinero(total) + " por cobrar");
            }

            @Override
            public void error(String mensaje, int codigo) {
                refrescar.setRefreshing(false);
                mostrarError(mensaje, codigo);
            }
        });
    }

    private void mostrarDetalle(JSONObject c) {
        View v = getLayoutInflater().inflate(R.layout.dialogo_credito, null);
        ((TextView) v.findViewById(R.id.txtCliente)).setText(c.optString("cliente"));
        ((TextView) v.findViewById(R.id.txtVenta)).setText("Venta No. " + c.optInt("venta")
                + " del " + c.optString("fechaVenta") + " · vence " + c.optString("vence")
                + (c.optBoolean("vencido") ? " (VENCIDO)" : ""));
        ((TextView) v.findViewById(R.id.txtTotales)).setText("Total de la venta: " + Formato.dinero(c.optDouble("total"))
                + "\nTotal abonado: " + Formato.dinero(c.optDouble("abonado")));
        TextView saldo = v.findViewById(R.id.txtSaldo);
        saldo.setText("Saldo " + Formato.dinero(c.optDouble("saldo")));
        saldo.setTextColor(ContextCompat.getColor(this, c.optBoolean("vencido") ? R.color.peligro : R.color.texto));

        JSONArray abonos = c.optJSONArray("abonos");
        StringBuilder sb = new StringBuilder();
        if (abonos == null || abonos.length() == 0) {
            sb.append("Sin abonos todavía");
        } else {
            for (int i = 0; i < abonos.length(); i++) {
                JSONObject a = abonos.optJSONObject(i);
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(a.optString("fecha")).append("   ").append(Formato.dinero(a.optDouble("monto")))
                        .append("   (").append(a.optString("recibio")).append(")");
            }
        }
        ((TextView) v.findViewById(R.id.txtAbonos)).setText(sb.toString());

        EditText edtMonto = v.findViewById(R.id.edtMonto);
        v.findViewById(R.id.btnTodo).setOnClickListener(x ->
                edtMonto.setText(String.format(java.util.Locale.US, "%.2f", c.optDouble("saldo"))));

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("Registrar abono", null)
                .setNegativeButton("Cerrar", null);
        String celular = c.isNull("celular") ? "" : c.optString("celular");
        if (!celular.isEmpty()) {
            b.setNeutralButton("Llamar", (d, w) ->
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + celular))));
        }
        AlertDialog dialogo = b.create();
        dialogo.show();
        // El botón se configura después de mostrar para que no cierre la ventana si hay un error
        Button registrar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);
        registrar.setOnClickListener(x -> abonar(c, edtMonto, dialogo, registrar));
    }

    private void abonar(JSONObject c, EditText edtMonto, AlertDialog dialogo, Button boton) {
        double monto;
        try {
            monto = Double.parseDouble(edtMonto.getText().toString().replace(",", "").trim());
        } catch (NumberFormatException e) {
            edtMonto.setError("Escriba el monto");
            return;
        }
        if (monto <= 0) {
            edtMonto.setError("El monto debe ser mayor que 0");
            return;
        }
        if (monto > c.optDouble("saldo") + 0.005) {
            edtMonto.setError("No puede ser mayor que el saldo");
            return;
        }
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idCredito", c.optInt("id"));
            cuerpo.put("monto", monto);
        } catch (JSONException e) {
            return;
        }
        boton.setEnabled(false);
        Api.post(this, "/api/creditos/abono", cuerpo, new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                dialogo.dismiss();
                JSONObject r = (JSONObject) json;
                String msg = r.optBoolean("pagado") ? "Abono registrado. ¡Crédito cancelado por completo!"
                        : "Abono registrado. Saldo: " + Formato.dinero(r.optDouble("saldo"));
                Toast.makeText(CreditosActivity.this, msg, Toast.LENGTH_LONG).show();
                cargar();
            }

            @Override
            public void error(String mensaje, int codigo) {
                boton.setEnabled(true);
                mostrarError(mensaje, codigo);
            }
        });
    }

    private class Adaptador extends ArrayAdapter<JSONObject> {

        Adaptador() {
            super(CreditosActivity.this, 0, creditos);
        }

        @Override
        public View getView(int pos, View vista, ViewGroup padre) {
            View v = vista != null ? vista
                    : getLayoutInflater().inflate(R.layout.item_credito, padre, false);
            JSONObject c = creditos.get(pos);
            boolean venc = c.optBoolean("vencido");
            int color = ContextCompat.getColor(getContext(), venc ? R.color.peligro : R.color.suave);
            ((TextView) v.findViewById(R.id.txtCliente)).setText(c.optString("cliente"));
            TextView detalle = v.findViewById(R.id.txtDetalle);
            detalle.setText("Venta " + c.optInt("venta") + " · vence " + c.optString("vence") + (venc ? " · VENCIDO" : ""));
            detalle.setTextColor(color);
            TextView saldo = v.findViewById(R.id.txtSaldo);
            saldo.setText(Formato.dinero(c.optDouble("saldo")));
            saldo.setTextColor(ContextCompat.getColor(getContext(), venc ? R.color.peligro : R.color.texto));
            return v;
        }
    }
}
