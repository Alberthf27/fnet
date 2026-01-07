package servicio;

import DAO.ClienteDAO;
import DAO.PagoDAO;
import DAO.YapeConfigDAO;
import bd.Conexion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Procesador de archivos Excel de Yape para registrar pagos automáticamente.
 * Lee el Excel, extrae DNI del mensaje, valida cliente y registra pago.
 */
public class YapePagoProcessor {

    private final ClienteDAO clienteDAO;
    private final PagoDAO pagoDAO;
    private final YapeConfigDAO yapeConfigDAO;
    private final WhatsappService whatsappService;
    private final BoletaPDFService pdfService;
    private final int idUsuarioSistema = 1; // Usuario "Sistema" para pagos automáticos
    private Date ultimaFechaProcesada;
    private Date nuevaUltimaFecha;

    public YapePagoProcessor() {
        this.clienteDAO = new ClienteDAO();
        this.pagoDAO = new PagoDAO();
        this.yapeConfigDAO = new YapeConfigDAO();
        this.whatsappService = new WhatsappService();
        this.pdfService = new BoletaPDFService();
    }

    /**
     * Procesa un archivo Excel de Yape y registra los pagos.
     * 
     * @param archivoExcel Archivo Excel descargado
     * @return Resumen del procesamiento
     */
    public ResumenProcesamiento procesarExcel(File archivoExcel) {
        ResumenProcesamiento resumen = new ResumenProcesamiento();

        System.out.println("\n📊 Procesando Excel: " + archivoExcel.getName());

        // Obtener última fecha procesada para evitar duplicados
        ultimaFechaProcesada = yapeConfigDAO.obtenerUltimaFechaProcesada();
        nuevaUltimaFecha = ultimaFechaProcesada;

        if (ultimaFechaProcesada != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            System.out.println("📅 Última fecha procesada: " + sdf.format(ultimaFechaProcesada));
            System.out.println("   Solo se procesarán transacciones posteriores a esta fecha.");
        } else {
            System.out.println("📅 Primera vez procesando - se procesarán todas las transacciones");
        }

        try (FileInputStream fis = new FileInputStream(archivoExcel);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Primera hoja

            // Saltar encabezado (asumiendo que empieza en fila 5 según la imagen)
            int filaInicio = 5;
            int totalFilas = sheet.getLastRowNum();

            System.out.println("📋 Total de filas a procesar: " + (totalFilas - filaInicio + 1));

            for (int i = filaInicio; i <= totalFilas; i++) {
                Row fila = sheet.getRow(i);
                if (fila == null)
                    continue;

                try {
                    procesarFila(fila, resumen);
                } catch (Exception e) {
                    System.err.println("⚠️ Error en fila " + (i + 1) + ": " + e.getMessage());
                    resumen.errores++;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error leyendo Excel: " + e.getMessage());
            e.printStackTrace();
        }

        // Actualizar última fecha procesada si hubo transacciones nuevas
        if (nuevaUltimaFecha != null
                && (ultimaFechaProcesada == null || nuevaUltimaFecha.after(ultimaFechaProcesada))) {
            yapeConfigDAO.actualizarUltimaFechaProcesada(nuevaUltimaFecha);
        }

        mostrarResumen(resumen);
        return resumen;
    }

    /**
     * Procesa una fila del Excel.
     */
    private void procesarFila(Row fila, ResumenProcesamiento resumen) {
        // Columnas según la imagen:
        // A: Tipo de Transacción
        // B: Origen
        // C: Destino
        // D: Monto
        // E: Mensaje
        // F: Fecha de operación

        String tipoTransaccion = getCellValueAsString(fila.getCell(0));
        String origen = getCellValueAsString(fila.getCell(1));
        String destino = getCellValueAsString(fila.getCell(2));
        double monto = getCellValueAsDouble(fila.getCell(3));
        String mensaje = getCellValueAsString(fila.getCell(4));
        Date fechaOperacion = getCellValueAsDate(fila.getCell(5));

        resumen.totalFilas++;

        // Filtrar transacciones ya procesadas
        if (ultimaFechaProcesada != null && !fechaOperacion.after(ultimaFechaProcesada)) {
            resumen.yaProcesados++;
            return; // Ya fue procesada anteriormente
        }

        // Actualizar la fecha más reciente encontrada
        if (nuevaUltimaFecha == null || fechaOperacion.after(nuevaUltimaFecha)) {
            nuevaUltimaFecha = fechaOperacion;
        }

        // Solo procesar pagos recibidos (TE PAGÓ o PAGASTE)
        String tipo = tipoTransaccion.toUpperCase().trim();
        if (!tipo.contains("PAGO") && !tipo.contains("PAGASTE")) {
            resumen.ignorados++;
            return;
        }

        // Extraer DNI del mensaje
        String dni = extraerDNI(mensaje);
        if (dni == null) {
            System.out.println("   ℹ️ Sin DNI en mensaje: \"" + mensaje + "\" - Ignorando");
            resumen.sinDNI++;
            return;
        }

        // Buscar cliente por DNI
        Map<String, Object> cliente = clienteDAO.buscarPorDNI(dni);
        if (cliente == null) {
            System.out.println("   ⚠️ DNI " + dni + " no encontrado en BD - Ignorando");
            resumen.dniNoEncontrado++;
            return;
        }

        // Registrar pago
        int idCliente = (int) cliente.get("id_cliente");
        String nombreCliente = cliente.get("nombres") + " " + cliente.get("apellidos");

        boolean exito = registrarPago(idCliente, nombreCliente, dni, monto, fechaOperacion);

        if (exito) {
            resumen.pagosRegistrados++;
            System.out.println("   ✅ Pago registrado: " + nombreCliente + " (DNI: " + dni + ") - S/. " + monto);
        } else {
            resumen.errores++;
        }
    }

    /**
     * Extrae el DNI del mensaje (busca 8 dígitos consecutivos).
     */
    private String extraerDNI(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return null;
        }

        // DEBUG: Ver qué mensaje estamos procesando
        System.out.println("🔍 DEBUG - Mensaje: '" + mensaje + "'");

        // Buscar 8 dígitos consecutivos
        Pattern pattern = Pattern.compile("\\b\\d{8}\\b");
        Matcher matcher = pattern.matcher(mensaje);

        if (matcher.find()) {
            String dni = matcher.group();
            System.out.println("✅ DEBUG - DNI encontrado: " + dni);
            return dni;
        }

        System.out.println("❌ DEBUG - NO se encontró DNI en: '" + mensaje + "'");
        return null;
    }

    /**
     * Registra un pago automáticamente distribuyéndolo entre múltiples facturas.
     */
    private boolean registrarPago(int idCliente, String nombreCliente, String dni,
            double monto, Date fechaOperacion) {
        try {
            // Buscar facturas pendientes del cliente (ordenadas por antigüedad)
            List<Object[]> deudasList = pagoDAO.buscarDeudasPorCliente(dni);

            if (deudasList == null || deudasList.isEmpty()) {
                System.out.println("   ℹ️ Cliente " + nombreCliente + " no tiene deudas pendientes");
                return false;
            }

            double montoRestante = monto;
            int facturasPagadas = 0;
            StringBuilder resumen = new StringBuilder();
            resumen.append("\n   💰 Distribuyendo pago de S/. ").append(String.format("%.2f", monto)).append(":\n");

            // Distribuir el pago entre las facturas pendientes (de más antigua a más
            // reciente)
            for (Object[] deuda : deudasList) {
                if (montoRestante <= 0) {
                    break; // Ya se distribuyó todo el monto
                }

                int idFactura = (int) deuda[0];
                String periodoMes = String.valueOf(deuda[3]); // índice 3, no 4

                // Obtener monto total (índice 4, no 6)
                double montoTotal = deuda[4] instanceof Integer
                        ? ((Integer) deuda[4]).doubleValue()
                        : (double) deuda[4];

                // buscarDeudasPorCliente NO devuelve monto_pagado
                // Necesitamos obtenerlo de la factura directamente
                double montoPagado = obtenerMontoPagado(idFactura);

                // Calcular monto PENDIENTE de esta factura
                double montoPendiente = montoTotal - montoPagado;

                if (montoPendiente <= 0) {
                    continue; // Esta factura ya está pagada completamente
                }

                // Calcular cuánto pagar de esta factura (lo que quede pendiente o el monto
                // restante)
                double montoPagar = Math.min(montoRestante, montoPendiente);

                // Realizar cobro parcial o total con método YAPE
                boolean exito = pagoDAO.realizarCobro(idFactura, montoPagar, idUsuarioSistema, "YAPE");

                if (exito) {
                    facturasPagadas++;
                    montoRestante -= montoPagar;

                    String estado = (montoPagar >= montoPendiente) ? "PAGADA COMPLETA" : "PAGO PARCIAL";
                    resumen.append(String.format("      ✅ %s - %s: S/. %.2f (Pendiente: S/. %.2f)\n",
                            periodoMes, estado, montoPagar, montoPendiente));
                } else {
                    resumen.append(String.format("      ❌ Error pagando %s\n", periodoMes));
                }
            }

            // Mostrar resumen
            System.out.println(resumen.toString());

            if (montoRestante > 0) {
                System.out.println(
                        String.format("   ℹ️ Sobrante de S/. %.2f (todas las deudas fueron pagadas)", montoRestante));
            }

            if (facturasPagadas > 0) {
                // Crear notificación de pago procesado
                crearNotificacionPago(nombreCliente, dni, monto - montoRestante, fechaOperacion);

                // Enviar notificación WhatsApp con PDF
                enviarNotificacionWhatsApp(dni, nombreCliente, facturasPagadas, monto - montoRestante);

                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Error registrando pago: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el monto ya pagado de una factura desde la base de datos.
     */
    private double obtenerMontoPagado(int idFactura) {
        try (java.sql.Connection conn = bd.Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(monto_pagado, 0) as monto_pagado FROM factura WHERE id_factura = ?")) {
            ps.setInt(1, idFactura);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("monto_pagado");
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo monto pagado: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Lógica simple de distribución (cuando no hay monto mensual).
     */
    private boolean registrarPagoSimple(int idCliente, String nombreCliente, String dni,
            double monto, Date fechaOperacion) {
        try {
            List<Object[]> deudasList = pagoDAO.buscarDeudasPorCliente(dni);

            if (deudasList == null || deudasList.isEmpty()) {
                System.out.println("   ℹ️ Cliente " + nombreCliente + " no tiene deudas pendientes");
                return false;
            }

            double montoRestante = monto;
            int facturasPagadas = 0;
            StringBuilder resumen = new StringBuilder();
            resumen.append("\\n   💰 Distribuyendo pago de S/. ").append(String.format("%.2f", monto)).append(":\\n");

            for (Object[] deuda : deudasList) {
                if (montoRestante <= 0)
                    break;

                int idFactura = (int) deuda[0];
                String periodoMes = (String) deuda[4];
                double montoFactura = deuda[6] instanceof Integer
                        ? ((Integer) deuda[6]).doubleValue()
                        : (double) deuda[6];

                double montoPagar = Math.min(montoRestante, montoFactura);
                boolean exito = pagoDAO.realizarCobro(idFactura, montoPagar, idUsuarioSistema, "YAPE");

                if (exito) {
                    facturasPagadas++;
                    montoRestante -= montoPagar;
                    String estado = (montoPagar >= montoFactura) ? "PAGADA" : "PAGO PARCIAL";
                    resumen.append(String.format("      ✅ %s - %s: S/. %.2f\\n",
                            periodoMes, estado, montoPagar));
                }
            }

            System.out.println(resumen.toString());
            return facturasPagadas > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el ID de suscripción activa del cliente.
     */
    private Integer obtenerIdSuscripcion(String dni) {
        try {
            Map<String, Object> cliente = clienteDAO.buscarPorDNI(dni);
            if (cliente != null && cliente.get("id_suscripcion") != null) {
                return (Integer) cliente.get("id_suscripcion");
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo id_suscripcion: " + e.getMessage());
        }
        return null;
    }

    /**
     * Genera una factura adelantada y la marca como pagada.
     */
    private boolean generarFacturaAdelantada(int idSuscripcion, double monto, int mesesAdelante) {
        try {
            // Calcular período futuro
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, mesesAdelante);

            String periodoMes = new SimpleDateFormat("MMMM yyyy", new java.util.Locale("es", "ES"))
                    .format(cal.getTime());

            java.sql.Date fechaVencimiento = new java.sql.Date(cal.getTimeInMillis());

            // Crear factura adelantada y marcarla como pagada
            return pagoDAO.crearFacturaManual(
                    idSuscripcion,
                    periodoMes,
                    monto,
                    2, // Estado PAGADO
                    fechaVencimiento,
                    true, // Registrar en caja
                    idUsuarioSistema);

        } catch (Exception e) {
            System.err.println("Error generando factura adelantada: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea una notificación de pago Yape procesado.
     */
    private void crearNotificacionPago(String nombreCliente, String dni,
            double monto, Date fechaOperacion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String fechaStr = sdf.format(fechaOperacion);

            String mensaje = String.format(
                    "💰 Pago Yape procesado automáticamente:\n" +
                            "Cliente: %s (DNI: %s)\n" +
                            "Monto: S/. %.2f\n" +
                            "Fecha: %s",
                    nombreCliente, dni, monto, fechaStr);

            // Aquí podrías crear una alerta en la BD si tienes una tabla para eso
            System.out.println("   📢 " + mensaje);

        } catch (Exception e) {
            System.err.println("⚠️ Error creando notificación: " + e.getMessage());
        }
    }

    /**
     * Envía notificación WhatsApp al cliente con confirmación de pago y PDF de
     * boletas.
     */
    private void enviarNotificacionWhatsApp(String dni, String nombreCliente, int facturasPagadas, double montoTotal) {
        try {
            if (!whatsappService.estaHabilitado()) {
                System.out.println("   ℹ️ WhatsApp no configurado - omitiendo notificación");
                return;
            }

            // Obtener teléfono del cliente
            Map<String, Object> cliente = clienteDAO.buscarPorDNI(dni);
            if (cliente == null || cliente.get("telefono") == null) {
                System.out.println("   ⚠️ Cliente sin teléfono registrado - no se puede enviar WhatsApp");
                return;
            }

            String telefono = (String) cliente.get("telefono");

            // Construir mensaje de confirmación
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String mensaje = String.format(
                    "✅ *PAGO REGISTRADO EXITOSAMENTE*\\n\\n" +
                            "Hola *%s*,\\n\\n" +
                            "Tu pago por Yape ha sido procesado automáticamente:\\n\\n" +
                            "💰 *Monto:* S/. %.2f\\n" +
                            "📅 *Fecha:* %s\\n" +
                            "📄 *Facturas pagadas:* %d\\n\\n" +
                            "📎 Adjuntamos las boletas de pago en PDF.\\n\\n" +
                            "Gracias por tu pago puntual! 🎉",
                    nombreCliente,
                    montoTotal,
                    sdf.format(new Date()),
                    facturasPagadas);

            // Enviar mensaje
            boolean enviado = whatsappService.enviarMensaje(telefono, mensaje);

            if (enviado) {
                System.out.println("   ✅ Notificación WhatsApp enviada a " + telefono);

                // Generar y enviar PDFs de las facturas pagadas
                enviarBoletasPDF(dni, telefono);
            } else {
                System.out.println("   ⚠️ No se pudo enviar notificación WhatsApp");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error enviando notificación WhatsApp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera y envía los PDFs de las boletas de las facturas pagadas recientemente.
     */
    private void enviarBoletasPDF(String dni, String telefono) {
        try {
            // Obtener facturas PAGADAS del cliente (no pendientes)
            String sql = "SELECT f.id_factura FROM factura f " +
                    "INNER JOIN cliente c ON f.id_cliente = c.id_cliente " +
                    "WHERE c.dni = ? AND f.id_estado = 2 " + // 2 = PAGADO
                    "ORDER BY f.fecha_pago DESC LIMIT 3";

            List<Integer> facturasPagadas = new ArrayList<>();

            try (Connection conn = Conexion.getConexion();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dni);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    facturasPagadas.add(rs.getInt("id_factura"));
                }
            }

            if (facturasPagadas.isEmpty()) {
                System.out.println("      ℹ️ No hay facturas pagadas para enviar PDF");
                return;
            }

            // Generar y enviar PDFs
            int pdfEnviados = 0;
            for (int idFactura : facturasPagadas) {
                // Regenerar PDF de la boleta
                String rutaPDF = pdfService.regenerarBoletaDesdeFactura(idFactura);

                if (rutaPDF != null) {
                    System.out.println("      📄 PDF generado: " + rutaPDF);

                    // Enviar PDF por WhatsApp
                    boolean enviado = enviarPDFPorWhatsApp(telefono, rutaPDF);
                    if (enviado) {
                        pdfEnviados++;
                        System.out.println("      ✅ PDF enviado por WhatsApp");
                    } else {
                        System.out.println("      ⚠️ No se pudo enviar PDF por WhatsApp");
                    }
                } else {
                    System.out.println("      ⚠️ No se pudo generar PDF para factura #" + idFactura);
                }
            }

            if (pdfEnviados > 0) {
                System.out.println(String.format("      ✅ %d boleta(s) PDF enviada(s) por WhatsApp", pdfEnviados));
            } else {
                System.out.println("      ⚠️ No se pudo enviar ningún PDF");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error generando/enviando boletas PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía un archivo PDF por WhatsApp usando WhatsappService.
     */
    private boolean enviarPDFPorWhatsApp(String telefono, String rutaPDF) {
        try {
            // Delegar al WhatsappService que ya tiene la configuración correcta
            return whatsappService.enviarArchivo(telefono, rutaPDF, "📄 Boleta de Pago");
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando PDF por WhatsApp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Muestra el resumen del procesamiento.
     */
    private void mostrarResumen(ResumenProcesamiento resumen) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 RESUMEN DE PROCESAMIENTO YAPE");
        System.out.println("=".repeat(60));
        System.out.println("   📋 Total de filas: " + resumen.totalFilas);
        System.out.println("   ✅ Pagos registrados: " + resumen.pagosRegistrados);
        System.out.println("   🔄 Ya procesados (duplicados): " + resumen.yaProcesados);
        System.out.println("   ⏭️ Ignorados (no son pagos): " + resumen.ignorados);
        System.out.println("   📝 Sin DNI en mensaje: " + resumen.sinDNI);
        System.out.println("   ⚠️ DNI no encontrado: " + resumen.dniNoEncontrado);
        System.out.println("   ❌ Errores: " + resumen.errores);
        System.out.println("=".repeat(60) + "\n");
    }

    // Métodos auxiliares para leer celdas

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null)
            return 0.0;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String valor = cell.getStringCellValue().replace(",", ".");
                return Double.parseDouble(valor);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error leyendo monto: " + e.getMessage());
        }

        return 0.0;
    }

    private Date getCellValueAsDate(Cell cell) {
        if (cell == null)
            return new Date();

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                return sdf.parse(cell.getStringCellValue());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error leyendo fecha: " + e.getMessage());
        }

        return new Date();
    }

    /**
     * Clase para almacenar el resumen del procesamiento.
     */
    public static class ResumenProcesamiento {
        public int totalFilas = 0;
        public int pagosRegistrados = 0;
        public int ignorados = 0;
        public int sinDNI = 0;
        public int dniNoEncontrado = 0;
        public int errores = 0;
        public int yaProcesados = 0; // Transacciones ya procesadas anteriormente
    }
}
