package com.ferreteria.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formato {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    // Ejemplo: 1234.5 -> "Q 1,234.50"
    public static String dinero(double monto) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        if (monto < 0) {
            return "-Q " + df.format(-monto);
        }
        return "Q " + df.format(monto);
    }

    // Ejemplo: 2.5 -> "2.5"   3.0 -> "3"
    public static String cantidad(double cantidad) {
        DecimalFormat df = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(cantidad);
    }

    // Ejemplo: 25/10/2026
    public static String fecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA);
    }

    // Ejemplo: 25/10/2026 14:30
    public static String fechaHora(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA_HORA);
    }

    // Redondea a 2 decimales (útil para totales y saldos)
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
