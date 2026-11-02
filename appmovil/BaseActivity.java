package com.ferreteria.movil;

import android.content.Intent;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Comportamiento común de las pantallas: manejo de errores de la API
 * y botón de regresar en la barra superior.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Si la sesión venció (el programa de escritorio se reinició) regresa al login
    protected void mostrarError(String mensaje, int codigo) {
        if (codigo == 401) {
            Sesion.cerrar(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("mensaje", mensaje);
            startActivity(i);
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(codigo == 0 ? "Sin conexión" : "Atención")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    protected void activarRegresar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
