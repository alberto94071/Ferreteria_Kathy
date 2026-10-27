package com.ferreteria.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Inicio de sesión con los mismos usuarios del programa de escritorio.
 */
public class LoginActivity extends BaseActivity {

    private EditText edtServidor;
    private EditText edtUsuario;
    private EditText edtContrasena;
    private TextView txtMensaje;
    private Button btnEntrar;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay sesión guardada, entra directo
        if (Sesion.activa(this)) {
            abrirPrincipal();
            return;
        }

        setContentView(R.layout.activity_login);
        edtServidor = findViewById(R.id.edtServidor);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtContrasena = findViewById(R.id.edtContrasena);
        txtMensaje = findViewById(R.id.txtMensaje);
        btnEntrar = findViewById(R.id.btnEntrar);
        progreso = findViewById(R.id.progreso);

        edtServidor.setText(Sesion.servidor(this));
        if (!Sesion.servidor(this).isEmpty()) {
            edtUsuario.requestFocus();
        }
        String mensaje = getIntent().getStringExtra("mensaje");
        if (mensaje != null) {
            mostrarMensaje(mensaje);
        }

        btnEntrar.setOnClickListener(v -> entrar());
        edtContrasena.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
    }

    private void entrar() {
        String servidor = limpiarServidor(edtServidor.getText().toString());
        String usuario = edtUsuario.getText().toString().trim();
        String contrasena = edtContrasena.getText().toString();
        if (servidor.isEmpty()) {
            mostrarMensaje("Escriba la dirección de la computadora de la ferretería");
            return;
        }
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarMensaje("Escriba su usuario y contraseña");
            return;
        }
        Sesion.guardarServidor(this, servidor);
        edtServidor.setText(servidor);

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("usuario", usuario);
            cuerpo.put("contrasena", contrasena);
        } catch (JSONException e) {
            return;
        }
        cargando(true);
        Api.post(this, "/api/login", cuerpo, new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                cargando(false);
                JSONObject r = (JSONObject) json;
                Sesion.iniciar(LoginActivity.this, r.optString("token"), r.optString("nombre"),
                        r.optString("rol"), r.optString("negocio", "Ferretería"));
                abrirPrincipal();
            }

            @Override
            public void error(String mensaje, int codigo) {
                cargando(false);
                mostrarMensaje(mensaje);
            }
        });
    }

    // Quita "http://", espacios y "/" del final; agrega el puerto si no lo tiene
    private static String limpiarServidor(String s) {
        String r = s.trim().replace("http://", "").replace("https://", "");
        while (r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        if (!r.isEmpty() && !r.contains(":")) {
            r = r + ":8080";
        }
        return r;
    }

    private void mostrarMensaje(String mensaje) {
        txtMensaje.setText(mensaje);
        txtMensaje.setVisibility(View.VISIBLE);
    }

    private void cargando(boolean si) {
        progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        btnEntrar.setEnabled(!si);
        if (si) {
            txtMensaje.setVisibility(View.GONE);
        }
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
