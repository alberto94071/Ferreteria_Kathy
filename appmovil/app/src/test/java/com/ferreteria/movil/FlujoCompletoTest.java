package com.ferreteria.movil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import java.util.function.BooleanSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

/**
 * Prueba de punta a punta contra la API real.
 * Requiere el programa de escritorio (o la API) corriendo en localhost:8080
 * con el usuario admin / admin123. Si no está corriendo, la prueba se omite.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class FlujoCompletoTest {

    private static void esperar(BooleanSupplier condicion) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            shadowOf(Looper.getMainLooper()).idle();
            if (condicion.getAsBoolean()) {
                return;
            }
            Thread.sleep(100);
        }
    }

    private static boolean servidorDisponible() {
        try (java.net.Socket s = new java.net.Socket("localhost", 8080)) {
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    @Test
    public void flujoCompleto() throws Exception {
        org.junit.Assume.assumeTrue("API no disponible en localhost:8080", servidorDisponible());
        Context ctx = ApplicationProvider.getApplicationContext();
        Sesion.cerrar(ctx);
        Sesion.guardarServidor(ctx, "localhost:8080");

        // 1. Login
        LoginActivity login = Robolectric.buildActivity(LoginActivity.class).setup().get();
        ((EditText) login.findViewById(R.id.edtUsuario)).setText("admin");
        ((EditText) login.findViewById(R.id.edtContrasena)).setText("admin123");
        login.findViewById(R.id.btnEntrar).performClick();
        esperar(() -> Sesion.activa(ctx));
        assertTrue("debió iniciar sesión", Sesion.activa(ctx));
        assertEquals(MainActivity.class.getName(),
                shadowOf(login).getNextStartedActivity().getComponent().getClassName());
        System.out.println("1. Login OK como " + Sesion.nombre(ctx));

        // 2. Resumen
        MainActivity main = Robolectric.buildActivity(MainActivity.class).setup().get();
        TextView vendido = main.findViewById(R.id.txtVendido);
        esperar(() -> !"—".contentEquals(vendido.getText()));
        System.out.println("2. Resumen: vendido " + vendido.getText()
                + " | ventas " + ((TextView) main.findViewById(R.id.txtVentas)).getText()
                + " | " + ((TextView) main.findViewById(R.id.txtPorCobrar)).getText());
        assertTrue(vendido.getText().toString().startsWith("Q "));

        // 3. Búsqueda de productos
        Intent ip = new Intent(ctx, ProductosActivity.class).putExtra(ProductosActivity.EXTRA_BAJO_STOCK, false);
        ProductosActivity prod = Robolectric.buildActivity(ProductosActivity.class, ip).setup().get();
        TextView resumen = prod.findViewById(R.id.txtResumen);
        ListView lista = prod.findViewById(R.id.lista);
        esperar(() -> resumen.getText().length() > 0);
        System.out.println("3. Productos: " + resumen.getText());
        ((EditText) prod.findViewById(R.id.edtBuscar)).setText("tubo");
        // El buscador espera medio segundo después de escribir; en la prueba se adelanta el reloj
        shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(600));
        esperar(() -> lista.getAdapter().getCount() == 1);
        assertEquals(1, lista.getAdapter().getCount());
        lista.performItemClick(lista.getAdapter().getView(0, null, lista), 0, 0);
        shadowOf(Looper.getMainLooper()).idle();
        Dialog d = ShadowDialog.getLatestDialog();
        assertNotNull(d);
        System.out.println("   Detalle: " + ((TextView) d.findViewById(R.id.txtNombre)).getText()
                + " | " + ((TextView) d.findViewById(R.id.txtPrecio)).getText()
                + " | " + ((TextView) d.findViewById(R.id.txtExistencia)).getText());
        d.dismiss();

        // 4. Código de barras (lo que hace el escáner después de leer)
        Escaner.buscar(prod, "MAR-16");
        esperar(() -> ShadowDialog.getLatestDialog() != d && ShadowDialog.getLatestDialog() != null);
        Dialog d2 = ShadowDialog.getLatestDialog();
        System.out.println("4. Escaneo MAR-16: " + ((TextView) d2.findViewById(R.id.txtNombre)).getText());
        d2.dismiss();

        // 5. Bajo stock
        Intent ib = new Intent(ctx, ProductosActivity.class).putExtra(ProductosActivity.EXTRA_BAJO_STOCK, true);
        ProductosActivity bajo = Robolectric.buildActivity(ProductosActivity.class, ib).setup().get();
        TextView resumenBajo = bajo.findViewById(R.id.txtResumen);
        esperar(() -> resumenBajo.getText().length() > 0);
        System.out.println("5. Bajo stock: " + resumenBajo.getText());

        // 6. Créditos vencidos y abono
        Intent ic = new Intent(ctx, CreditosActivity.class).putExtra(CreditosActivity.EXTRA_VENCIDOS, true);
        CreditosActivity cred = Robolectric.buildActivity(CreditosActivity.class, ic).setup().get();
        TextView resumenCred = cred.findViewById(R.id.txtResumen);
        ListView listaCred = cred.findViewById(R.id.lista);
        esperar(() -> resumenCred.getText().length() > 0);
        System.out.println("6. Créditos: " + resumenCred.getText());
        if (listaCred.getAdapter().getCount() > 0) {
            listaCred.performItemClick(listaCred.getAdapter().getView(0, null, listaCred), 0, 0);
            shadowOf(Looper.getMainLooper()).idle();
            androidx.appcompat.app.AlertDialog dc = (androidx.appcompat.app.AlertDialog) ShadowDialog.getLatestDialog();
            System.out.println("   " + ((TextView) dc.findViewById(R.id.txtCliente)).getText()
                    + " | " + ((TextView) dc.findViewById(R.id.txtSaldo)).getText());
            ((EditText) dc.findViewById(R.id.edtMonto)).setText("10");
            dc.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick();
            esperar(() -> ShadowToast.getTextOfLatestToast() != null);
            System.out.println("   " + ShadowToast.getTextOfLatestToast());
            assertNotNull(ShadowToast.getTextOfLatestToast());
        }

        // 7. Sesión vencida -> regresa al login
        Sesion.iniciar(ctx, "token-invalido", "x", "VENDEDOR", "x");
        MainActivity vencida = Robolectric.buildActivity(MainActivity.class).setup().get();
        esperar(() -> vencida.isFinishing());
        Intent siguiente = shadowOf(vencida).getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(), siguiente.getComponent().getClassName());
        System.out.println("7. Sesión vencida -> " + siguiente.getStringExtra("mensaje"));
    }
}
