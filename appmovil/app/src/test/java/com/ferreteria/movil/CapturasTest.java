package com.ferreteria.movil;

import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.function.BooleanSupplier;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;

/**
 * Genera imágenes PNG de cada pantalla (opcional).
 * Se activa con: gradlew test -Pcapturas=/carpeta/destino
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w393dp-h852dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CapturasTest {

    private static void esperar(BooleanSupplier c) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            shadowOf(Looper.getMainLooper()).idle();
            if (c.getAsBoolean()) {
                return;
            }
            Thread.sleep(100);
        }
    }

    private static void guardar(View raiz, String nombre) throws Exception {
        String dir = System.getProperty("capturas");
        raiz.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(2340, View.MeasureSpec.EXACTLY));
        raiz.layout(0, 0, 1080, 2340);
        Bitmap b = Bitmap.createBitmap(1080, 2340, Bitmap.Config.ARGB_8888);
        raiz.draw(new Canvas(b));
        try (FileOutputStream out = new FileOutputStream(new File(dir, nombre + ".png"))) {
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }

    private static void guardar(Activity a, String nombre) throws Exception {
        guardar(a.getWindow().getDecorView(), nombre);
    }

    @Test
    public void capturas() throws Exception {
        Assume.assumeTrue(System.getProperty("capturas") != null);
        Context ctx = ApplicationProvider.getApplicationContext();
        // Ícono de la app sobre fondo blanco, recortado en círculo como en el celular
        Bitmap icono = Bitmap.createBitmap(432, 432, Bitmap.Config.ARGB_8888);
        Canvas lienzo = new Canvas(icono);
        android.graphics.Paint pincel = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        pincel.setColor(0xFFFFFFFF);
        lienzo.drawColor(0xFFDDE3EA);
        lienzo.drawCircle(216, 216, 200, pincel);
        android.graphics.drawable.Drawable d0 = androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.ic_launcher_foreground);
        d0.setBounds(-48, -48, 480, 480);
        d0.draw(lienzo);
        try (FileOutputStream out = new FileOutputStream(new File(System.getProperty("capturas"), "0_icono_app.png"))) {
            icono.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        Sesion.cerrar(ctx);
        Sesion.guardarServidor(ctx, "192.168.1.10:8080");
        LoginActivity login = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ((EditText) login.findViewById(R.id.edtUsuario)).setText("maria");
        guardar(login, "1_login");

        ((EditText) login.findViewById(R.id.edtServidor)).setText("localhost:8080");
        ((EditText) login.findViewById(R.id.edtUsuario)).setText("admin");
        ((EditText) login.findViewById(R.id.edtContrasena)).setText("admin123");
        login.findViewById(R.id.btnEntrar).performClick();
        esperar(() -> Sesion.activa(ctx));
        // Para la captura se muestra la IP de ejemplo en lugar de localhost

        MainActivity main = Robolectric.buildActivity(MainActivity.class).setup().get();
        TextView vendido = main.findViewById(R.id.txtVendido);
        esperar(() -> !"—".contentEquals(vendido.getText()));
        guardar(main, "2_inicio");

        ProductosActivity prod = Robolectric.buildActivity(ProductosActivity.class,
                new Intent(ctx, ProductosActivity.class)).setup().get();
        TextView r = prod.findViewById(R.id.txtResumen);
        esperar(() -> r.getText().length() > 0);
        guardar(prod, "3_productos");
        ListView lista = prod.findViewById(R.id.lista);
        lista.performItemClick(lista.getAdapter().getView(3, null, lista), 3, 3);
        shadowOf(Looper.getMainLooper()).idle();
        Dialog d = ShadowDialog.getLatestDialog();
        guardar(d.getWindow().getDecorView().getRootView(), "4_detalle_producto");
        d.dismiss();

        CreditosActivity cred = Robolectric.buildActivity(CreditosActivity.class,
                new Intent(ctx, CreditosActivity.class)).setup().get();
        TextView rc = cred.findViewById(R.id.txtResumen);
        esperar(() -> rc.getText().length() > 0);
        guardar(cred, "5_creditos");
    }
}
