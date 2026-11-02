package com.ferreteria.movil;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Registro de productos nuevos desde el celular (por ejemplo, al hacer el inventario inicial).
 * Varios celulares pueden usarla al mismo tiempo: si dos registran el mismo código,
 * el servidor acepta solo el primero.
 */
public class NuevoProductoActivity extends BaseActivity {

    private EditText edtCodigo;
    private EditText edtNombre;
    private AutoCompleteTextView edtUnidad;
    private EditText edtPresentacion;
    private AutoCompleteTextView edtMarca;
    private AutoCompleteTextView edtCategoria;
    private EditText edtPrecioCompra;
    private EditText edtPrecioVenta;
    private EditText edtExistencia;
    private EditText edtMinimo;
    private TextView txtEstadoCodigo;
    private TextView txtContador;
    private Button btnGuardar;

    // Listas de marcas y categorías: posición 0 = "Sin marca" / "Sin categoría" (id 0)
    private final List<Integer> idsMarca = new ArrayList<>();
    private final List<Integer> idsCategoria = new ArrayList<>();
    private final List<String> nombresMarca = new ArrayList<>();
    private final List<String> nombresCategoria = new ArrayList<>();
    private int agregados = 0;
    private String ultimoVerificado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_producto);
        activarRegresar();
        setTitle("Nuevo producto");

        edtCodigo = findViewById(R.id.edtCodigo);
        edtNombre = findViewById(R.id.edtNombre);
        edtUnidad = findViewById(R.id.edtUnidad);
        edtPresentacion = findViewById(R.id.edtPresentacion);
        edtMarca = findViewById(R.id.edtMarca);
        edtCategoria = findViewById(R.id.edtCategoria);
        edtPrecioCompra = findViewById(R.id.edtPrecioCompra);
        edtPrecioVenta = findViewById(R.id.edtPrecioVenta);
        edtExistencia = findViewById(R.id.edtExistencia);
        edtMinimo = findViewById(R.id.edtMinimo);
        txtEstadoCodigo = findViewById(R.id.txtEstadoCodigo);
        txtContador = findViewById(R.id.txtContador);
        btnGuardar = findViewById(R.id.btnGuardar);

        edtUnidad.setText("unidad");
        edtMinimo.setText("1");

        findViewById(R.id.btnEscanear).setOnClickListener(v ->
                Escaner.escanearCodigo(this, codigo -> {
                    edtCodigo.setText(codigo);
                    verificarCodigo(codigo);
                }));
        // Si el código se escribe a mano (o con lector bluetooth), se revisa al pasar al siguiente campo
        edtCodigo.setOnFocusChangeListener((v, tieneFoco) -> {
            if (!tieneFoco) {
                verificarCodigo(edtCodigo.getText().toString());
            }
        });
        btnGuardar.setOnClickListener(v -> guardar());
        cargarCatalogos();
    }

    private void cargarCatalogos() {
        Api.get(this, "/api/catalogos", new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                JSONObject r = (JSONObject) json;
                List<String> unidades = nombres(r.optJSONArray("unidades"), null);
                edtUnidad.setAdapter(new ArrayAdapter<>(NuevoProductoActivity.this,
                        android.R.layout.simple_list_item_1, unidades));

                List<String> marcas = nombres(r.optJSONArray("marcas"), idsMarca);
                marcas.add(0, "(Sin marca)");
                idsMarca.add(0, 0);
                edtMarca.setAdapter(new ArrayAdapter<>(NuevoProductoActivity.this,
                        android.R.layout.simple_list_item_1, marcas));
                edtMarca.setText(marcas.get(0), false);
                nombresMarca.clear();
                nombresMarca.addAll(marcas);

                List<String> categorias = nombres(r.optJSONArray("categorias"), idsCategoria);
                categorias.add(0, "(Sin categoría)");
                idsCategoria.add(0, 0);
                edtCategoria.setAdapter(new ArrayAdapter<>(NuevoProductoActivity.this,
                        android.R.layout.simple_list_item_1, categorias));
                edtCategoria.setText(categorias.get(0), false);
                nombresCategoria.clear();
                nombresCategoria.addAll(categorias);
            }

            @Override
            public void error(String mensaje, int codigo) {
                mostrarError(mensaje, codigo);
            }
        });
    }

    // Convierte [{id, nombre}] o ["texto"] en lista de nombres (y guarda los ids si se pide)
    private static List<String> nombres(JSONArray arreglo, List<Integer> ids) {
        List<String> lista = new ArrayList<>();
        if (ids != null) {
            ids.clear();
        }
        if (arreglo == null) {
            return lista;
        }
        for (int i = 0; i < arreglo.length(); i++) {
            JSONObject o = arreglo.optJSONObject(i);
            if (o == null) {
                lista.add(arreglo.optString(i));
            } else {
                lista.add(o.optString("nombre"));
                if (ids != null) {
                    ids.add(o.optInt("id"));
                }
            }
        }
        return lista;
    }

    // Pregunta al servidor si el código ya está registrado
    private void verificarCodigo(String codigo) {
        String c = codigo == null ? "" : codigo.trim();
        if (c.isEmpty()) {
            txtEstadoCodigo.setVisibility(View.GONE);
            ultimoVerificado = "";
            return;
        }
        if (c.equals(ultimoVerificado)) {
            return; // ya se revisó este mismo código
        }
        ultimoVerificado = c;
        Api.get(this, "/api/productos/codigo?valor=" + Api.codificar(c), new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                JSONObject p = (JSONObject) json;
                mostrarEstado("Este código ya está registrado", R.color.peligro);
                new AlertDialog.Builder(NuevoProductoActivity.this)
                        .setTitle("Producto ya registrado")
                        .setMessage(p.optString("nombre") + "\n\nCódigo: " + c
                                + "\nExistencia: " + Formato.cantidad(p.optDouble("existencia")) + " " + p.optString("unidad")
                                + "\nPrecio: " + Formato.dinero(p.optDouble("precioVenta"))
                                + "\n\nNo se puede registrar dos veces.")
                        .setPositiveButton("Aceptar", (d, w) -> {
                            ultimoVerificado = "";
                            edtCodigo.setText("");
                            txtEstadoCodigo.setVisibility(View.GONE);
                            edtCodigo.requestFocus();
                        })
                        .show();
            }

            @Override
            public void error(String mensaje, int codigo) {
                if (codigo == 404) {
                    mostrarEstado("Código nuevo, puede registrarlo", R.color.exito);
                    if (edtNombre.getText().toString().trim().isEmpty()) {
                        edtNombre.requestFocus();
                    }
                } else {
                    mostrarError(mensaje, codigo);
                }
            }
        });
    }

    private void guardar() {
        if (edtNombre.getText().toString().trim().isEmpty()) {
            edtNombre.setError("Escriba el nombre");
            edtNombre.requestFocus();
            return;
        }
        if (edtUnidad.getText().toString().trim().isEmpty()) {
            edtUnidad.setError("Escriba la unidad");
            return;
        }
        for (EditText e : new EditText[]{edtPrecioCompra, edtPrecioVenta, edtExistencia, edtMinimo}) {
            if (e.getText().toString().trim().isEmpty()) {
                e.setError("Obligatorio");
                e.requestFocus();
                return;
            }
        }
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("codigo", edtCodigo.getText().toString().trim());
            cuerpo.put("nombre", edtNombre.getText().toString().trim());
            cuerpo.put("unidad", edtUnidad.getText().toString().trim());
            cuerpo.put("presentacion", edtPresentacion.getText().toString().trim());
            cuerpo.put("idMarca", idElegido(edtMarca, nombresMarca, idsMarca));
            cuerpo.put("idCategoria", idElegido(edtCategoria, nombresCategoria, idsCategoria));
            cuerpo.put("precioCompra", limpiar(edtPrecioCompra));
            cuerpo.put("precioVenta", limpiar(edtPrecioVenta));
            cuerpo.put("existencia", limpiar(edtExistencia));
            cuerpo.put("minimo", limpiar(edtMinimo));
        } catch (JSONException e) {
            return;
        }
        btnGuardar.setEnabled(false);   // evita guardar dos veces por doble toque
        btnGuardar.setText("Guardando...");
        Api.post(this, "/api/productos/nuevo", cuerpo, new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar producto");
                agregados++;
                txtContador.setText("Productos agregados desde este celular: " + agregados);
                Toast.makeText(NuevoProductoActivity.this, "Guardado: " + ((JSONObject) json).optString("nombre"),
                        Toast.LENGTH_SHORT).show();
                limpiarFormulario();
            }

            @Override
            public void error(String mensaje, int codigo) {
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar producto");
                mostrarError(mensaje, codigo);
            }
        });
    }

    private static String limpiar(EditText e) {
        return e.getText().toString().replace(",", "").replace("Q", "").trim();
    }

    // Busca el id por el nombre que quedó escrito (la posición de la lista cambia cuando se filtra)
    private static int idElegido(AutoCompleteTextView campo, List<String> nombres, List<Integer> ids) {
        int i = nombres.indexOf(campo.getText().toString().trim());
        return i >= 0 && i < ids.size() ? ids.get(i) : 0;
    }

    // Deja listos los campos para el siguiente producto (se conservan unidad, marca y categoría)
    private void limpiarFormulario() {
        for (EditText e : new EditText[]{edtCodigo, edtNombre, edtPresentacion, edtPrecioCompra,
                edtPrecioVenta, edtExistencia}) {
            e.setText("");
            e.setError(null);
        }
        txtEstadoCodigo.setVisibility(View.GONE);
        ultimoVerificado = "";
        edtCodigo.requestFocus();
    }

    private void mostrarEstado(String texto, int color) {
        txtEstadoCodigo.setText(texto);
        txtEstadoCodigo.setTextColor(ContextCompat.getColor(this, color));
        txtEstadoCodigo.setVisibility(View.VISIBLE);
    }
}
