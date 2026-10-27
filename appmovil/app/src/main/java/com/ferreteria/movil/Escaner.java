package com.ferreteria.movil;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import org.json.JSONObject;

/**
 * Escaneo de códigos de barras con la cámara y ventana con los datos del producto.
 * Usa el escáner de Google Play Services: no hace falta pedir permiso de cámara.
 */
public final class Escaner {

    private Escaner() {
    }

    // Abre la cámara, busca el código en el servidor y muestra el producto
    public static void escanearYMostrar(BaseActivity a) {
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(a);
        scanner.startScan()
                .addOnSuccessListener(codigo -> buscar(a, codigo.getRawValue()))
                .addOnFailureListener(e -> a.mostrarError(
                        "No se pudo abrir el escáner. Si es la primera vez, el celular necesita "
                                + "internet un momento para descargarlo.\n\n" + e.getMessage(), -1));
    }

    public static void buscar(BaseActivity a, String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return;
        }
        Api.get(a, "/api/productos/codigo?valor=" + Api.codificar(codigo.trim()), new Api.Respuesta() {
            @Override
            public void exito(Object json) {
                mostrarProducto(a, (JSONObject) json);
            }

            @Override
            public void error(String mensaje, int codigo) {
                a.mostrarError(mensaje, codigo);
            }
        });
    }

    public static void mostrarProducto(Activity a, JSONObject p) {
        View v = a.getLayoutInflater().inflate(R.layout.dialogo_producto, null);
        ((TextView) v.findViewById(R.id.txtNombre)).setText(p.optString("nombre"));

        StringBuilder detalle = new StringBuilder();
        agregar(detalle, p.isNull("codigo") ? "Sin código" : p.optString("codigo"));
        if (!p.isNull("marca")) {
            agregar(detalle, p.optString("marca"));
        }
        if (!p.isNull("categoria")) {
            agregar(detalle, p.optString("categoria"));
        }
        if (p.optBoolean("esKit")) {
            agregar(detalle, "KIT");
        }
        ((TextView) v.findViewById(R.id.txtDetalle)).setText(detalle.toString());
        ((TextView) v.findViewById(R.id.txtPrecio)).setText(Formato.dinero(p.optDouble("precioVenta")));

        TextView existencia = v.findViewById(R.id.txtExistencia);
        existencia.setText("Existencia: " + Formato.cantidad(p.optDouble("existencia")) + " "
                + p.optString("unidad") + "   (mínimo " + Formato.cantidad(p.optDouble("minimo")) + ")");
        if (p.optBoolean("bajoStock")) {
            existencia.setTextColor(Color.parseColor("#B91C1C"));
            existencia.append("\nProducto por agotarse");
        }
        if (p.has("precioCompra")) {
            TextView costo = v.findViewById(R.id.txtCosto);
            costo.setText("Costo: " + Formato.dinero(p.optDouble("precioCompra"))
                    + "   Ganancia: " + Formato.dinero(p.optDouble("precioVenta") - p.optDouble("precioCompra")));
            costo.setVisibility(View.VISIBLE);
        }
        new AlertDialog.Builder(a)
                .setView(v)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private static void agregar(StringBuilder sb, String texto) {
        if (sb.length() > 0) {
            sb.append("  ·  ");
        }
        sb.append(texto);
    }
}
