package com.ferreteria.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONObject;

/**
 * Pantalla principal: resumen del día y accesos a las demás pantallas.
 */
public class MainActivity extends BaseActivity {

    private SwipeRefreshLayout refrescar;
    private TextView txtVendido;
    private TextView txtVentas;
    private TextView txtBajoStock;
    private TextView txtVencidos;
    private TextView txtFecha;
    private TextView txtPorCobrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(Sesion.negocio(this));

        refrescar = findViewById(R.id.refrescar);
        txtVendido = findViewById(R.id.txtVendido);
        txtVentas = findViewById(R.id.txtVentas);
        txtBajoStock = findViewById(R.id.txtBajoStock);
        txtVencidos = findViewById(R.id.txtVencidos);
        txtFecha = findViewById(R.id.txtFecha);
        txtPorCobrar = findViewById(R.id.txtPorCobrar);
        ((TextView) findViewById(R.id.txtSaludo)).setText("Hola, " + Sesion.nombre(this));

        refrescar.setColorSchemeResources(R.color.acento);
        refrescar.setOnRefreshListener(this::cargar);

        findViewById(R.id.btnEscanear).setOnClickListener(v -> Escaner.escanearYMostrar(this));
        findViewById(R.id.btnBuscar).setOnClickListener(v -> abrirProductos(false));
        findViewById(R.id.btnBajoStock).setOnClickListener(v -> abrirProductos(true));
        findViewById(R.id.cardBajoStock).setOnClickListener(v -> abrirProductos(true));
        findViewById(R.id.btnCreditos).setOnClickListener(v -> abrirCreditos(false));
        findViewById(R.id.cardVencidos).setOnClickListener(v -> abrirCreditos(true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        refrescar.setRefreshing(true);
        Api.get(this, "/api/resumen", new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                refrescar.setRefreshing(false);
                JSONObject r = (JSONObject) json;
                txtFecha.setText(r.optString("fecha"));
                txtVendido.setText(Formato.dinero(r.optDouble("vendidoHoy")));
                txtVentas.setText(String.valueOf(r.optInt("ventasHoy")));
                txtBajoStock.setText(String.valueOf(r.optInt("productosBajoStock")));
                txtVencidos.setText(String.valueOf(r.optInt("creditosVencidos")));
                txtPorCobrar.setText(r.optInt("creditosPendientes") + " créditos pendientes · "
                        + Formato.dinero(r.optDouble("totalPorCobrar")) + " por cobrar");
            }

            @Override
            public void error(String mensaje, int codigo) {
                refrescar.setRefreshing(false);
                mostrarError(mensaje, codigo);
            }
        });
    }

    private void abrirProductos(boolean bajoStock) {
        Intent i = new Intent(this, ProductosActivity.class);
        i.putExtra(ProductosActivity.EXTRA_BAJO_STOCK, bajoStock);
        startActivity(i);
    }

    private void abrirCreditos(boolean vencidos) {
        Intent i = new Intent(this, CreditosActivity.class);
        i.putExtra(CreditosActivity.EXTRA_VENCIDOS, vencidos);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_actualizar) {
            cargar();
            return true;
        }
        if (item.getItemId() == R.id.menu_salir) {
            Api.post(this, "/api/logout", null, new Api.Respuesta() {
                @Override
                public void exito(Object json) {
                }

                @Override
                public void error(String mensaje, int codigo) {
                }
            });
            Sesion.cerrar(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
