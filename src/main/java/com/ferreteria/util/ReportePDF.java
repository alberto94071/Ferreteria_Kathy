package com.ferreteria.util;

import com.ferreteria.modelo.Credito;
import com.ferreteria.modelo.DetalleVenta;
import com.ferreteria.modelo.Producto;
import com.ferreteria.modelo.Venta;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Genera los documentos PDF del sistema con la librería OpenPDF:
 * el comprobante de una venta y el reporte de productos con bajo stock.
 */
public class ReportePDF {

    private static final String NEGOCIO = "Ferretería Kathy";
    private static final String DIRECCION = "San Pedro Sacatepéquez, San Marcos";

    private static final Color AZUL = new Color(0x0B4983);
    private static final Color AMARILLO = new Color(0xF6B238);
    private static final Color GRIS = new Color(0x64748B);
    private static final Color FONDO_FILA = new Color(0xF1F5F9);
    private static final Color ROJO = new Color(0xB91C1C);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, GRIS);
    private static final Font NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font ENCABEZADO = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font TOTAL = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 8, Font.ITALIC, GRIS);
    private static final Font PELIGRO = new Font(Font.HELVETICA, 10, Font.BOLD, ROJO);

    private ReportePDF() {
    }

    // ---------- Comprobante de venta ----------

    /**
     * Comprobante de una venta ya registrada.
     * Si la venta es al crédito se manda el crédito para mostrar vencimiento y saldo.
     */
    public static void comprobanteVenta(Venta venta, Credito credito, File archivo) throws IOException {
        Document doc = new Document(PageSize.LETTER, 50, 50, 40, 40);
        try (OutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(doc, salida);
            doc.open();
            encabezado(doc, "Comprobante de venta No. " + venta.getIdVenta());

            String cliente = venta.getCliente() == null ? "Consumidor final" : venta.getCliente().getNombre();
            PdfPTable datos = new PdfPTable(new float[]{1.2f, 3f, 1.2f, 2f});
            datos.setWidthPercentage(100);
            datos.setSpacingAfter(14);
            dato(datos, "Fecha:", Formato.fechaHora(venta.getFecha()));
            dato(datos, "Tipo:", venta.isEsCredito() ? "Crédito" : "Contado");
            dato(datos, "Cliente:", cliente);
            dato(datos, "Atendió:", venta.getUsuario().getNombreUsuario());
            if (venta.getCliente() != null && venta.getCliente().getDireccion() != null
                    && !venta.getCliente().getDireccion().isEmpty()) {
                dato(datos, "Dirección:", venta.getCliente().getDireccion());
                dato(datos, "Celular:", venta.getCliente().getCelular() == null ? "" : venta.getCliente().getCelular());
            }
            doc.add(datos);

            PdfPTable tabla = new PdfPTable(new float[]{1f, 4.5f, 1.5f, 1.6f});
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            titulo(tabla, "Cant.", Element.ALIGN_CENTER);
            titulo(tabla, "Producto", Element.ALIGN_LEFT);
            titulo(tabla, "Precio", Element.ALIGN_RIGHT);
            titulo(tabla, "Subtotal", Element.ALIGN_RIGHT);
            int fila = 0;
            for (DetalleVenta d : venta.getDetalles()) {
                Color fondo = fila % 2 == 0 ? Color.WHITE : FONDO_FILA;
                celda(tabla, Formato.cantidad(d.getCantidad()), Element.ALIGN_CENTER, fondo, NORMAL);
                celda(tabla, d.getProducto().toString(), Element.ALIGN_LEFT, fondo, NORMAL);
                celda(tabla, Formato.dinero(d.getPrecioFinal()), Element.ALIGN_RIGHT, fondo, NORMAL);
                celda(tabla, Formato.dinero(d.getSubtotal()), Element.ALIGN_RIGHT, fondo, NORMAL);
                fila++;
            }
            PdfPCell etiquetaTotal = new PdfPCell(new Phrase("TOTAL", TOTAL));
            etiquetaTotal.setColspan(3);
            etiquetaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            etiquetaTotal.setBorder(Rectangle.TOP);
            etiquetaTotal.setBorderColor(AZUL);
            etiquetaTotal.setPaddingTop(8);
            tabla.addCell(etiquetaTotal);
            PdfPCell total = new PdfPCell(new Phrase(Formato.dinero(venta.getTotal()), TOTAL));
            total.setHorizontalAlignment(Element.ALIGN_RIGHT);
            total.setBorder(Rectangle.TOP);
            total.setBorderColor(AZUL);
            total.setPaddingTop(8);
            tabla.addCell(total);
            doc.add(tabla);

            if (venta.isEsCredito() && credito != null) {
                PdfPTable cr = new PdfPTable(new float[]{2f, 2f});
                cr.setWidthPercentage(55);
                cr.setHorizontalAlignment(Element.ALIGN_LEFT);
                cr.setSpacingBefore(18);
                dato(cr, "Fecha de vencimiento:", Formato.fecha(credito.getFechaVencimiento()));
                dato(cr, "Abonado:", Formato.dinero(credito.getTotalAbonado()));
                dato(cr, "Saldo pendiente:", Formato.dinero(credito.getSaldo()));
                doc.add(cr);

                Paragraph firma = new Paragraph("\n\n\n______________________________\nFirma del cliente", NORMAL);
                firma.setAlignment(Element.ALIGN_CENTER);
                doc.add(firma);
            }

            Paragraph gracias = new Paragraph("¡Gracias por su compra!", NEGRITA);
            gracias.setAlignment(Element.ALIGN_CENTER);
            gracias.setSpacingBefore(24);
            doc.add(gracias);
            pie(doc, "Comprobante interno del negocio, no sustituye a la factura.");
            doc.close();
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    // ---------- Reporte de bajo stock ----------

    /** Lista de productos con existencia igual o menor al mínimo, con lo que falta para reponer. */
    public static void reporteBajoStock(List<Producto> productos, String usuario, File archivo) throws IOException {
        Document doc = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (OutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(doc, salida);
            doc.open();
            encabezado(doc, "Reporte de productos con bajo stock");

            Paragraph info = new Paragraph("Generado el " + Formato.fechaHora(LocalDateTime.now())
                    + " por " + usuario + "     ·     Productos en la lista: " + productos.size(), SUBTITULO);
            info.setSpacingAfter(12);
            doc.add(info);

            PdfPTable tabla = new PdfPTable(new float[]{1.3f, 3.6f, 1.4f, 1.6f, 1f, 1f, 1.1f});
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            titulo(tabla, "Código", Element.ALIGN_LEFT);
            titulo(tabla, "Producto", Element.ALIGN_LEFT);
            titulo(tabla, "Marca", Element.ALIGN_LEFT);
            titulo(tabla, "Categoría", Element.ALIGN_LEFT);
            titulo(tabla, "Exist.", Element.ALIGN_CENTER);
            titulo(tabla, "Mín.", Element.ALIGN_CENTER);
            titulo(tabla, "Pedir", Element.ALIGN_CENTER);
            int fila = 0;
            double costoReponer = 0;
            for (Producto p : productos) {
                Color fondo = fila % 2 == 0 ? Color.WHITE : FONDO_FILA;
                // Se sugiere pedir lo necesario para llegar al doble del mínimo
                double pedir = Math.max(1, Math.ceil(p.getExistenciaMinima() * 2 - p.getCantidadActual()));
                costoReponer += pedir * p.getPrecioCompra();
                celda(tabla, p.getCodigo() == null ? "" : p.getCodigo(), Element.ALIGN_LEFT, fondo, NORMAL);
                celda(tabla, p.toString(), Element.ALIGN_LEFT, fondo, NORMAL);
                celda(tabla, p.getMarca() == null ? "" : p.getMarca().toString(), Element.ALIGN_LEFT, fondo, NORMAL);
                celda(tabla, p.getCategoria() == null ? "" : p.getCategoria().toString(), Element.ALIGN_LEFT, fondo, NORMAL);
                celda(tabla, Formato.cantidad(p.getCantidadActual()), Element.ALIGN_CENTER, fondo,
                        p.getCantidadActual() <= 0 ? PELIGRO : NEGRITA);
                celda(tabla, Formato.cantidad(p.getExistenciaMinima()), Element.ALIGN_CENTER, fondo, NORMAL);
                celda(tabla, Formato.cantidad(pedir), Element.ALIGN_CENTER, fondo, NORMAL);
                fila++;
            }
            if (productos.isEmpty()) {
                PdfPCell vacio = new PdfPCell(new Phrase("No hay productos con bajo stock.", NORMAL));
                vacio.setColspan(7);
                vacio.setPadding(10);
                vacio.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(vacio);
            }
            doc.add(tabla);

            Paragraph resumen = new Paragraph();
            resumen.setSpacingBefore(14);
            resumen.add(new Chunk("Costo aproximado para reponer: ", NORMAL));
            resumen.add(new Chunk(Formato.dinero(costoReponer), TOTAL));
            doc.add(resumen);
            Paragraph nota = new Paragraph("La columna \"Pedir\" sugiere la cantidad para llegar al doble de la existencia mínima. "
                    + "El costo usa el precio de compra registrado de cada producto.", PEQUENA);
            nota.setSpacingBefore(6);
            doc.add(nota);
            pie(doc, NEGOCIO + " · Reporte generado por el sistema de inventario");
            doc.close();
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    // ---------- Partes comunes ----------

    private static void encabezado(Document doc, String titulo) throws DocumentException, IOException {
        PdfPTable cabeza = new PdfPTable(new float[]{1.3f, 3f});
        cabeza.setWidthPercentage(100);
        Image logo = cargarLogo();
        PdfPCell celdaLogo = logo == null ? new PdfPCell(new Phrase(NEGOCIO, TITULO)) : new PdfPCell(logo, true);
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cabeza.addCell(celdaLogo);

        PdfPCell texto = new PdfPCell();
        texto.setBorder(Rectangle.NO_BORDER);
        texto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        texto.setPaddingLeft(14);
        Paragraph nombre = new Paragraph(NEGOCIO, new Font(Font.HELVETICA, 20, Font.BOLD, AZUL));
        texto.addElement(nombre);
        texto.addElement(new Paragraph(DIRECCION, SUBTITULO));
        Paragraph t = new Paragraph(titulo, new Font(Font.HELVETICA, 13, Font.BOLD, Color.BLACK));
        t.setSpacingBefore(6);
        texto.addElement(t);
        cabeza.addCell(texto);
        doc.add(cabeza);

        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(8);
        linea.setSpacingAfter(12);
        PdfPCell barra = new PdfPCell();
        barra.setFixedHeight(4);
        barra.setBackgroundColor(AMARILLO);
        barra.setBorder(Rectangle.NO_BORDER);
        linea.addCell(barra);
        doc.add(linea);
    }

    private static void pie(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, PEQUENA);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(20);
        doc.add(p);
    }

    private static Image cargarLogo() {
        try (InputStream in = ReportePDF.class.getResourceAsStream("/imagenes/logo_ferreteria.png")) {
            if (in == null) {
                return null;
            }
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                bytes.write(buffer, 0, n);
            }
            return Image.getInstance(bytes.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    private static void dato(PdfPTable t, String etiqueta, String valor) {
        PdfPCell a = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        a.setBorder(Rectangle.NO_BORDER);
        a.setPaddingBottom(4);
        t.addCell(a);
        PdfPCell b = new PdfPCell(new Phrase(valor, NORMAL));
        b.setBorder(Rectangle.NO_BORDER);
        b.setPaddingBottom(4);
        t.addCell(b);
    }

    private static void titulo(PdfPTable t, String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, ENCABEZADO));
        c.setBackgroundColor(AZUL);
        c.setHorizontalAlignment(alineacion);
        c.setPadding(6);
        c.setBorderColor(AZUL);
        t.addCell(c);
    }

    private static void celda(PdfPTable t, String texto, int alineacion, Color fondo, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setBackgroundColor(fondo);
        c.setPadding(5);
        c.setBorderColor(new Color(0xE2E8F0));
        t.addCell(c);
    }
}
