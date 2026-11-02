package com.ferreteria.movil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Formato {

    private Formato() {
    }

    // 1234.5 -> "Q 1,234.50"
    public static String dinero(double monto) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return (monto < 0 ? "-Q " : "Q ") + df.format(Math.abs(monto));
    }

    // 3.0 -> "3", 2.5 -> "2.5"
    public static String cantidad(double valor) {
        DecimalFormat df = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(valor);
    }
}
