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
    private static final String EMPRESA_NOMBRE = "FNET INTERNET";
    private static final String EMPRESA_RUC = "20XXXXXXXXX";
    private static final String EMPRESA_DIRECCION = "Av. Principal 123, Ciudad";
    private static final String EMPRESA_TELEFONO = "Tel: 987 654 321";

    // Ruta del logo
    private static final String LOGO_PATH = "src/img/Sin título (1)_1.png";

    // Carpeta donde se guardan las boletas
    private static final String BOLETAS_DIR = "boletas";

    public BoletaPDFService() {
        inicializarFuentes();
        crearDirectorioBoletas();
    }

    private void inicializarFuentes() {
        try {
            // Fuentes más pequeñas para caber en una página A6
            fuenteTitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_PRIMARIO);
            fuenteSubtitulo = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, COLOR_SECUNDARIO);
            fuenteNormal = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
            fuenteNegrita = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
            fuenteGrande = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
            fuentePequena = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.GRAY);
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

        // Formato A6: 105mm x 148mm
        Rectangle tamanoA6 = new Rectangle(
                Utilities.millimetersToPoints(105),
                Utilities.millimetersToPoints(148));

        Document documento = new Document(tamanoA6, 15, 15, 15, 15); // márgenes de 15pt

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

            // Logo
            try {
                Image logo = Image.getInstance(LOGO_PATH);
                logo.scaleToFit(50, 50);
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
            documento.add(new Paragraph(" ")); // Espaciador

            // --- TÍTULO BOLETA ---
            Paragraph tituloBoleta = new Paragraph("BOLETA DE PAGO", fuenteGrande);
            tituloBoleta.setAlignment(Element.ALIGN_CENTER);
            documento.add(tituloBoleta);

            Paragraph numBoleta = new Paragraph("N° " + numeroBoleta, fuenteSubtitulo);
            numBoleta.setAlignment(Element.ALIGN_CENTER);
            documento.add(numBoleta);

            Paragraph fecha = new Paragraph("Fecha: " + fechaHoy, fuentePequena);
            fecha.setAlignment(Element.ALIGN_CENTER);
            documento.add(fecha);
            documento.add(new Paragraph(" "));

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
            documento.add(new Paragraph(" "));

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

            Font fuenteMonto = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(22, 163, 74));
            PdfPCell celdaMonto = new PdfPCell(new Phrase("S/. " + String.format("%.2f", monto), fuenteMonto));
            celdaMonto.setBorder(Rectangle.NO_BORDER);
            celdaMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaMonto.setPaddingTop(2);
            celdaMonto.setPaddingBottom(2);
            tablaMonto.addCell(celdaMonto);

            documento.add(tablaMonto);

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

            // --- MENSAJE FINAL ---
            Paragraph gracias = new Paragraph("¡Gracias por su preferencia!", fuenteSubtitulo);
            gracias.setAlignment(Element.ALIGN_CENTER);
            documento.add(gracias);

            Paragraph conservar = new Paragraph("Conserve esta boleta como comprobante de pago", fuentePequena);
            conservar.setAlignment(Element.ALIGN_CENTER);
            documento.add(conservar);

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
        documento.add(new Paragraph(" "));
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
}
