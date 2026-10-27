package com.ferreteria.movil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Habla con la API del programa de escritorio por el Wi-Fi de la tienda.
 *
 * Las peticiones se hacen en un hilo aparte (Android no permite usar la red
 * en el hilo de la pantalla) y la respuesta regresa al hilo principal.
 *
 * Ejemplo:
 *   Api.get(this, "/api/resumen", new Api.Respuesta() {
 *       public void exito(Object json) { ... }
 *       public void error(String mensaje, int codigo) { ... }
 *   });
 */
public final class Api {

    public interface Respuesta {
        // json es un JSONObject o un JSONArray
        void exito(Object json);

        // codigo: 401 sesión vencida, 404 no encontrado, 0 sin conexión, etc.
        void error(String mensaje, int codigo);
    }

    private static final ExecutorService HILOS = Executors.newFixedThreadPool(3);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private Api() {
    }

    public static void get(Context c, String ruta, Respuesta r) {
        pedir(c, "GET", ruta, null, r);
    }

    public static void post(Context c, String ruta, JSONObject cuerpo, Respuesta r) {
        pedir(c, "POST", ruta, cuerpo, r);
    }

    public static String codificar(String texto) {
        try {
            return URLEncoder.encode(texto, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return texto;
        }
    }

    private static void pedir(Context c, String metodo, String ruta, JSONObject cuerpo, Respuesta r) {
        final String servidor = Sesion.servidor(c);
        final String token = Sesion.token(c);
        HILOS.execute(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL("http://" + servidor + ruta);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(15000);
                con.setRequestMethod(metodo);
                con.setRequestProperty("Accept", "application/json");
                if (token != null) {
                    con.setRequestProperty("Authorization", "Bearer " + token);
                }
                if (cuerpo != null) {
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                final int codigo = con.getResponseCode();
                InputStream in = codigo >= 400 ? con.getErrorStream() : con.getInputStream();
                String texto = in == null ? "" : leer(in);
                final Object json = texto.trim().isEmpty() ? new JSONObject() : new JSONTokener(texto).nextValue();
                if (codigo >= 400) {
                    String msg = json instanceof JSONObject
                            ? ((JSONObject) json).optString("error", "Error " + codigo) : "Error " + codigo;
                    PRINCIPAL.post(() -> r.error(msg, codigo));
                } else {
                    PRINCIPAL.post(() -> r.exito(json));
                }
            } catch (IOException e) {
                PRINCIPAL.post(() -> r.error("No se pudo conectar con la computadora de la ferretería ("
                        + servidor + ").\n\nRevise que el programa esté abierto y que el celular "
                        + "esté conectado al mismo Wi-Fi.", 0));
            } catch (JSONException | ClassCastException e) {
                PRINCIPAL.post(() -> r.error("La respuesta del servidor no es válida", 0));
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        });
    }

    private static String leer(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        in.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
