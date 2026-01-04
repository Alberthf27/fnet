package servicio;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servicio para generar boletas de pago en PDF formato A6.
 */
public class BoletaPDFService {

    // Colores corporativos
    private static final BaseColor COLOR_PRIMARIO = new BaseColor(37, 99, 235); // Azul
    private static final BaseColor COLOR_SECUNDARIO = new BaseColor(15, 23, 42); // Gris oscuro
    private static final BaseColor COLOR_LINEA = new BaseColor(226, 232, 240); // Gris claro

    // Fuentes
    private Font fuenteTitulo;
    private Font fuenteSubtitulo;
    private Font fuenteNormal;
    private Font fuenteNegrita;
    private Font fuenteGrande;
    private Font fuentePequena;

    // Datos de la empresa
    private static final String EMPRESA_NOMBRE = "FIBRANET";
    private static final String EMPRESA_RUC = "20XXXXXXXXX";
    private static final String EMPRESA_DIRECCION = "Av. Principal 123, Ciudad";
    private static final String EMPRESA_TELEFONO = "Tel: 987 654 321";

    // Ruta del logo
    private static final String LOGO_PATH = "src/img/Sin título (1)_1.png";

    // Carpeta donde se guardan las boletas
    private static final String BOLETAS_DIR = "boletas";

    // Archivo contador de boletas
    private static final String CONTADOR_FILE = BOLETAS_DIR + "/contador.txt";

    public BoletaPDFService() {
        inicializarFuentes();
        crearDirectorioBoletas();
    }

    private void inicializarFuentes() {
        try {
            // Fuentes aumentadas +1pt para mejor legibilidad
            fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
            fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_SECUNDARIO);
            fuenteNormal = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
            fuenteNegrita = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
            fuenteGrande = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO);
            fuentePequena = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void crearDirectorioBoletas() {
        File dir = new File(BOLETAS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Obtiene el siguiente número de boleta, incrementando el contador.
     * El contador se guarda en un archivo para persistencia.
     * Empieza desde 1 y aumenta con cada boleta.
     */
    private synchronized int obtenerSiguienteNumeroBoleta() {
        int numero = 1;
        File contadorFile = new File(CONTADOR_FILE);

        // Leer número actual
        if (contadorFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(contadorFile))) {
                String linea = reader.readLine();
                if (linea != null && !linea.isEmpty()) {
                    numero = Integer.parseInt(linea.trim()) + 1;
                }
            } catch (Exception e) {
                System.err.println("Error leyendo contador: " + e.getMessage());
            }
        }

        // Guardar nuevo número
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(contadorFile))) {
            writer.println(numero);
        } catch (Exception e) {
            System.err.println("Error guardando contador: " + e.getMessage());
        }

        return numero;
    }

    /**
     * Genera una boleta de pago en formato PDF A6.
     *
     * @param numeroBoleta   Número único de la boleta
     * @param nombreCliente  Nombre completo del cliente
     * @param codigoContrato Código del contrato
     * @param direccion      Dirección del cliente
     * @param concepto       Concepto del pago (ej: "Servicio Diciembre 2025")
     * @param plan           Nombre del plan (ej: "FIBRA 50MB")
     * @param tipoPlan       Tipo de plan (PREPAGO / FIN DE MES)
     * @param periodoDesde   Fecha inicio del periodo (ej: "01/12/2025")
     * @param periodoHasta   Fecha fin del periodo (ej: "31/12/2025")
     * @param monto          Monto pagado
     * @param formaPago      Forma de pago (Efectivo, Yape, etc.)
     * @param atendidoPor    Nombre del usuario que registró el pago
     * @return Ruta del archivo PDF generado, o null si hubo error
     */
    public String generarBoleta(
            String numeroBoleta,
            String nombreCliente,
            String codigoContrato,
            String direccion,
            String concepto,
            String plan,
            String tipoPlan,
            String periodoDesde,
            String periodoHasta,
            double monto,
            String formaPago,
            String atendidoPor) {

        // Formato A5 HORIZONTAL (landscape): 210mm ancho x 148mm alto
        // Contenido A6 VERTICAL centrado: 105mm ancho x 148mm alto
        // Margen horizontal para centrar: (210 - 105) / 2 = 52.5mm cada lado
        // Margen vertical: 10mm arriba y abajo (mínimo)
        Rectangle tamanoA5Landscape = new Rectangle(
                Utilities.millimetersToPoints(210), // ancho A5 landscape
                Utilities.millimetersToPoints(148)); // alto A5 landscape

        // Márgenes para centrar contenido A6 en página A5 horizontal
        float margenHorizontal = Utilities.millimetersToPoints(52.5f); // centra 105mm en 210mm
        float margenVertical = Utilities.millimetersToPoints(10); // mínimo arriba/abajo

        Document documento = new Document(tamanoA5Landscape, margenHorizontal, margenHorizontal, margenVertical,
                margenVertical);

        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String nombreArchivo = BOLETAS_DIR + "/boleta_" + numeroBoleta + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            // --- ENCABEZADO CON LOGO ---
            PdfPTable tablaEncabezado = new PdfPTable(2);
            tablaEncabezado.setWidthPercentage(100);
            tablaEncabezado.setWidths(new float[] { 30, 70 });

            // Logo (más grande: 70x70)
            try {
                Image logo = Image.getInstance(LOGO_PATH);
                logo.scaleToFit(70, 70);
                PdfPCell celdaLogo = new PdfPCell(logo);
                celdaLogo.setBorder(Rectangle.NO_BORDER);
                celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
                tablaEncabezado.addCell(celdaLogo);
            } catch (Exception e) {
                // Si no hay logo, celda vacía
                PdfPCell celdaVacia = new PdfPCell(new Phrase(""));
                celdaVacia.setBorder(Rectangle.NO_BORDER);
                tablaEncabezado.addCell(celdaVacia);
            }

            // Datos empresa
            PdfPCell celdaEmpresa = new PdfPCell();
            celdaEmpresa.setBorder(Rectangle.NO_BORDER);
            celdaEmpresa.addElement(new Paragraph(EMPRESA_NOMBRE, fuenteTitulo));
            celdaEmpresa.addElement(new Paragraph("RUC: " + EMPRESA_RUC, fuentePequena));
            celdaEmpresa.addElement(new Paragraph(EMPRESA_TELEFONO, fuentePequena));
            tablaEncabezado.addCell(celdaEmpresa);

            documento.add(tablaEncabezado);

            // --- TÍTULO BOLETA ---
            Paragraph tituloBoleta = new Paragraph("BOLETA DE PAGO", fuenteGrande);
            tituloBoleta.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloBoleta);

            Paragraph numBoleta = new Paragraph("N° " + numeroBoleta, fuenteSubtitulo);
            numBoleta.setAlignment(Element.ALIGN_CENTER);
            documento.add(numBoleta);

            Paragraph fecha = new Paragraph("Fecha: " + fechaHoy, fuentePequena);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(5);
            documento.add(fecha);

            // --- LÍNEA SEPARADORA ---
            agregarLineaSeparadora(documento);

            // --- DATOS DEL CLIENTE ---
            documento.add(new Paragraph("DATOS DEL CLIENTE", fuenteSubtitulo));
            documento.add(new Paragraph(" "));

            PdfPTable tablaCliente = new PdfPTable(2);
            tablaCliente.setWidthPercentage(100);
            tablaCliente.setWidths(new float[] { 35, 65 });

            agregarFilaTabla(tablaCliente, "Cliente:", nombreCliente);
            agregarFilaTabla(tablaCliente, "Contrato:", codigoContrato);
            if (direccion != null && !direccion.isEmpty()) {
                agregarFilaTabla(tablaCliente, "Dirección:", direccion);
            }
            documento.add(tablaCliente);

            // --- LÍNEA SEPARADORA ---
            agregarLineaSeparadora(documento);

            // --- DETALLE DEL SERVICIO ---
            documento.add(new Paragraph("DETALLE DEL SERVICIO", fuenteSubtitulo));

            PdfPTable tablaServicio = new PdfPTable(2);
            tablaServicio.setWidthPercentage(100);
            tablaServicio.setWidths(new float[] { 35, 65 });

            agregarFilaTabla(tablaServicio, "Concepto:", concepto);
            agregarFilaTabla(tablaServicio, "Plan:", plan);
            agregarFilaTabla(tablaServicio, "Modalidad:", tipoPlan);
            if (periodoDesde != null && periodoHasta != null) {
                agregarFilaTabla(tablaServicio, "Periodo:", periodoDesde + " al " + periodoHasta);
            }
            documento.add(tablaServicio);

            // --- LÍNEA SEPARADORA ---
            agregarLineaSeparadora(documento);

            // --- MONTO TOTAL ---
            PdfPTable tablaMonto = new PdfPTable(2);
            tablaMonto.setWidthPercentage(100);
            tablaMonto.setWidths(new float[] { 50, 50 });

            PdfPCell celdaLabel = new PdfPCell(new Phrase("MONTO PAGADO:", fuenteNegrita));
            celdaLabel.setBorder(Rectangle.NO_BORDER);
            celdaLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaLabel.setPaddingTop(2);
            celdaLabel.setPaddingBottom(2);
            tablaMonto.addCell(celdaLabel);

            Font fuenteMonto = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
            PdfPCell celdaMonto = new PdfPCell(new Phrase("S/. " + String.format("%.2f", monto), fuenteMonto));
            celdaMonto.setBorder(Rectangle.NO_BORDER);
            celdaMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaMonto.setPaddingTop(2);
            celdaMonto.setPaddingBottom(2);
            tablaMonto.addCell(celdaMonto);

            documento.add(tablaMonto);

            // --- MONTO EN LETRAS ---
            Paragraph montoLetras = new Paragraph("SON: " + convertirMontoALetras(monto), fuentePequena);
            montoLetras.setAlignment(Element.ALIGN_CENTER);
            montoLetras.setSpacingAfter(3);
            documento.add(montoLetras);

            // --- LÍNEA SEPARADORA ---
            agregarLineaSeparadora(documento);

            // --- INFORMACIÓN ADICIONAL ---
            PdfPTable tablaAdicional = new PdfPTable(2);
            tablaAdicional.setWidthPercentage(100);
            tablaAdicional.setWidths(new float[] { 40, 60 });

            agregarFilaTabla(tablaAdicional, "Forma de pago:", formaPago);
            if (atendidoPor != null && !atendidoPor.isEmpty()) {
                agregarFilaTabla(tablaAdicional, "Atendido por:", atendidoPor);
            }
            documento.add(tablaAdicional);

            // --- MENSAJE FINAL (compacto para caber en 1 hoja) ---
            Paragraph footer = new Paragraph("¡Gracias! Conserve esta boleta como comprobante de pago", fuentePequena);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(5f); // Espacio mínimo
            documento.add(footer);

            documento.close();

            System.out.println("✅ Boleta generada: " + nombreArchivo);
            return nombreArchivo;

        } catch (Exception e) {
            System.err.println("❌ Error generando boleta PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void agregarFilaTabla(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuenteNegrita));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPaddingBottom(3);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuenteNormal));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPaddingBottom(3);
        tabla.addCell(celdaValor);
    }

    private void agregarLineaSeparadora(Document documento) throws DocumentException {
        LineSeparator linea = new LineSeparator();
        linea.setLineColor(COLOR_LINEA);
        linea.setLineWidth(1);
        documento.add(new Chunk(linea));
    }

    /**
     * Convierte un monto numérico a texto en español.
     * Ejemplo: 50.00 -> "CINCUENTA CON 00/100 SOLES"
     */
    private String convertirMontoALetras(double monto) {
        String[] unidades = { "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE" };
        String[] decenas = { "", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA",
                "SESENTA", "SETENTA", "OCHENTA", "NOVENTA" };
        String[] especiales = { "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
                "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE" };
        String[] centenas = { "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS",
                "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS" };

        int entero = (int) monto;
        int centavos = (int) Math.round((monto - entero) * 100);

        StringBuilder resultado = new StringBuilder();

        if (entero == 0) {
            resultado.append("CERO");
        } else if (entero == 100) {
            resultado.append("CIEN");
        } else if (entero < 10) {
            resultado.append(unidades[entero]);
        } else if (entero <= 20) {
            resultado.append(especiales[entero - 10]);
        } else if (entero < 30) {
            resultado.append("VEINTI").append(unidades[entero % 10]);
        } else if (entero < 100) {
            int d = entero / 10;
            int u = entero % 10;
            resultado.append(decenas[d]);
            if (u > 0)
                resultado.append(" Y ").append(unidades[u]);
        } else if (entero < 1000) {
            int c = entero / 100;
            int resto = entero % 100;
            resultado.append(entero == 100 ? "CIEN" : centenas[c]);
            if (resto > 0) {
                resultado.append(" ");
                if (resto < 10)
                    resultado.append(unidades[resto]);
                else if (resto <= 20)
                    resultado.append(especiales[resto - 10]);
                else if (resto < 30)
                    resultado.append("VEINTI").append(unidades[resto % 10]);
                else {
                    resultado.append(decenas[resto / 10]);
                    if (resto % 10 > 0)
                        resultado.append(" Y ").append(unidades[resto % 10]);
                }
            }
        } else {
            resultado.append(String.valueOf(entero)); // Para montos muy grandes
        }

        resultado.append(" CON ").append(String.format("%02d", centavos)).append("/100 SOLES");
        return resultado.toString();
    }

    /**
     * Abre el PDF generado con la aplicación predeterminada del sistema.
     */
    public void abrirPDF(String rutaPDF) {
        try {
            File archivo = new File(rutaPDF);
            if (archivo.exists()) {
                java.awt.Desktop.getDesktop().open(archivo);
            }
        } catch (Exception e) {
            System.err.println("Error abriendo PDF: " + e.getMessage());
        }
    }

    /**
     * Elimina todas las boletas PDF asociadas a una factura.
     * Busca archivos que empiecen con "boleta_XXXXXX" donde XXXXXX es el número de
     * factura.
     * 
     * @param idFactura ID de la factura
     * @return Cantidad de archivos eliminados
     */
    public int eliminarBoletasDeFactura(int idFactura) {
        String prefijo = "boleta_" + String.format("%06d", idFactura);
        File dir = new File(BOLETAS_DIR);
        int eliminados = 0;

        if (dir.exists() && dir.isDirectory()) {
            File[] archivos = dir.listFiles((d, name) -> name.startsWith(prefijo) && name.endsWith(".pdf"));
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.delete()) {
                        eliminados++;
                        System.out.println("✅ Boleta eliminada: " + archivo.getName());
                    }
                }
            }
        }
        return eliminados;
    }

    /**
     * Genera la boleta y la abre automáticamente.
     */
    public String generarYAbrirBoleta(
            String numeroBoleta,
            String nombreCliente,
            String codigoContrato,
            String direccion,
            String concepto,
            String plan,
            String tipoPlan,
            String periodoDesde,
            String periodoHasta,
            double monto,
            String formaPago,
            String atendidoPor) {

        String ruta = generarBoleta(numeroBoleta, nombreCliente, codigoContrato,
                direccion, concepto, plan, tipoPlan, periodoDesde, periodoHasta,
                monto, formaPago, atendidoPor);

        if (ruta != null) {
            abrirPDF(ruta);
        }

        return ruta;
    }

    /**
     * Regenera una boleta PDF desde los datos almacenados en la base de datos.
     * Siempre genera la misma boleta para el mismo ID de factura.
     * 
     * @param idFactura ID de la factura en la BD
     * @return Ruta del PDF generado, o null si hubo error
     */
    public String regenerarBoletaDesdeFactura(int idFactura) {
        DAO.PagoDAO pagoDAO = new DAO.PagoDAO();
        java.util.Map<String, Object> datos = pagoDAO.obtenerDatosParaBoleta(idFactura);

        if (datos == null) {
            System.err.println("No se encontró la factura con ID: " + idFactura);
            return null;
        }

        // Obtener siguiente número secuencial de boleta
        String numeroBoleta = String.format("%06d", obtenerSiguienteNumeroBoleta());
        String nombreCliente = (String) datos.get("nombreCliente");
        String codigoContrato = (String) datos.get("codigoContrato");
        String direccion = datos.get("direccion") != null ? (String) datos.get("direccion") : "";
        String concepto = (String) datos.get("concepto");
        String planServicio = (String) datos.get("planServicio");
        double monto = (Double) datos.get("monto");

        // Calcular periodo desde fecha_vencimiento
        java.sql.Date fechaVenc = (java.sql.Date) datos.get("fechaVencimiento");
        String periodoDesde = "";
        String periodoHasta = "";
        if (fechaVenc != null) {
            java.time.LocalDate fecha = fechaVenc.toLocalDate();
            java.time.YearMonth ym = java.time.YearMonth.from(fecha);
            periodoDesde = "01/" + String.format("%02d", ym.getMonthValue()) + "/" + ym.getYear();
            periodoHasta = ym.lengthOfMonth() + "/" + String.format("%02d", ym.getMonthValue()) + "/" + ym.getYear();
        }

        // Determinar estado (para mostrar "PAGADO" o forma de pago)
        int idEstado = (Integer) datos.get("idEstado");
        String formaPago = idEstado == 2 ? "PAGADO" : "PENDIENTE";

        return generarYAbrirBoleta(
                numeroBoleta,
                nombreCliente != null ? nombreCliente : "---",
                codigoContrato != null ? codigoContrato : "---",
                direccion,
                concepto != null ? concepto : "---",
                planServicio != null ? planServicio : "---",
                "MENSUAL",
                periodoDesde,
                periodoHasta,
                monto,
                formaPago,
                "Sistema");
    }
}
