package com.ferreteria.movil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Lista de productos: búsqueda por nombre/código o productos por agotarse.
 */
public class ProductosActivity extends BaseActivity {

    public static final String EXTRA_BAJO_STOCK = "bajo_stock";

    private final List<JSONObject> productos = new ArrayList<>();
    private final Handler espera = new Handler(Looper.getMainLooper());
    private Adaptador adaptador;
    private SwipeRefreshLayout refrescar;
    private EditText edtBuscar;
    private TextView txtResumen;
    private boolean bajoStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);
        activarRegresar();
        bajoStock = getIntent().getBooleanExtra(EXTRA_BAJO_STOCK, false);
        setTitle(bajoStock ? "Por agotarse" : "Productos");

        refrescar = findViewById(R.id.refrescar);
        edtBuscar = findViewById(R.id.edtBuscar);
        txtResumen = findViewById(R.id.txtResumen);
        ListView lista = findViewById(R.id.lista);
        adaptador = new Adaptador();
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((p, v, pos, id) -> Escaner.mostrarProducto(this, productos.get(pos)));

        if (bajoStock) {
            findViewById(R.id.barraBusqueda).setVisibility(View.GONE);
        }
        findViewById(R.id.btnEscanear).setOnClickListener(v -> Escaner.escanearYMostrar(this));

        // Busca mientras se escribe (espera medio segundo después de la última tecla)
        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                espera.removeCallbacksAndMessages(null);
                espera.postDelayed(ProductosActivity.this::cargar, 500);
            }
        });
        refrescar.setColorSchemeResources(R.color.acento);
        refrescar.setOnRefreshListener(this::cargar);
        cargar();
    }

    private void cargar() {
        String ruta = bajoStock ? "/api/productos/bajo-stock"
                : "/api/productos?buscar=" + Api.codificar(edtBuscar.getText().toString().trim());
        refrescar.setRefreshing(true);
        Api.get(this, ruta, new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                refrescar.setRefreshing(false);
                JSONArray arreglo = (JSONArray) json;
                productos.clear();
                for (int i = 0; i < arreglo.length(); i++) {
                    productos.add(arreglo.optJSONObject(i));
                }
                adaptador.notifyDataSetChanged();
                txtResumen.setText(productos.isEmpty() ? "No se encontraron productos"
                        : productos.size() + " productos" + (bajoStock ? " por agotarse" : ""));
            }

            @Override
            public void error(String mensaje, int codigo) {
                refrescar.setRefreshing(false);
                mostrarError(mensaje, codigo);
            }
        });
    }

    private class Adaptador extends ArrayAdapter<JSONObject> {

        Adaptador() {
            super(ProductosActivity.this, 0, productos);
        }

        @Override
        public View getView(int pos, View vista, ViewGroup padre) {
            View v = vista != null ? vista
                    : getLayoutInflater().inflate(R.layout.item_producto, padre, false);
            JSONObject p = productos.get(pos);
            ((TextView) v.findViewById(R.id.txtNombre)).setText(p.optString("nombre")
                    + (p.optBoolean("esKit") ? "  [KIT]" : ""));
            String codigo = p.isNull("codigo") ? "Sin código" : p.optString("codigo");
            String marca = p.isNull("marca") ? "" : "  ·  " + p.optString("marca");
            ((TextView) v.findViewById(R.id.txtDetalle)).setText(codigo + marca);
            ((TextView) v.findViewById(R.id.txtPrecio)).setText(Formato.dinero(p.optDouble("precioVenta")));
            TextView ex = v.findViewById(R.id.txtExistencia);
            ex.setText("Exist.: " + Formato.cantidad(p.optDouble("existencia")) + " " + p.optString("unidad"));
            ex.setTextColor(ContextCompat.getColor(getContext(),
                    p.optBoolean("bajoStock") ? R.color.peligro : R.color.suave));
            return v;
        }
    }
}
