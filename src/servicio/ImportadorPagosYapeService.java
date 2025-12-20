package servicio;

import DAO.PagoDAO;
import DAO.ConfiguracionDAO;
import bd.Conexion;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

/**
 * Servicio para importar pagos desde Excel/CSV de Yape.
 * Procesa el reporte de pagos y registra automáticamente en el sistema.
 */
public class ImportadorPagosYapeService {

    private final PagoDAO pagoDAO;
    private final CobrosAutomaticoService cobrosService;

    // Estadísticas de importación
    private int pagosEncontrados = 0;
    private int pagosRegistrados = 0;
    private int pagosNoCoinciden = 0;
    private List<String> errores = new ArrayList<>();

    public ImportadorPagosYapeService() {
        this.pagoDAO = new PagoDAO();
        this.cobrosService = new CobrosAutomaticoService();
    }

    /**
     * Procesa un archivo CSV exportado de Yape.
     * 
     * @param rutaArchivo Ruta al archivo CSV
     * @return Resumen de la importación
     */
    public String procesarArchivoYape(String rutaArchivo) {
        resetEstadisticas();

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📥 IMPORTANDO PAGOS DESDE YAPE");
        System.out.println("📄 Archivo: " + rutaArchivo);
        System.out.println("═══════════════════════════════════════════════════");

        // Cargar mapa de clientes para búsqueda rápida
        Map<String, DatosCliente> mapaClientes = cargarMapaClientes();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            String separador = ",";

            while ((linea = br.readLine()) != null) {
                numeroLinea++;

                // Detectar separador en primera línea de datos
                if (numeroLinea == 2 && linea.contains(";")) {
                    separador = ";";
                }

                // Saltar cabeceras (primeras 2 líneas típicamente)
                if (numeroLinea < 2)
                    continue;

                try {
                    procesarLineaYape(linea, separador, mapaClientes);
                } catch (Exception e) {
                    errores.add("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            errores.add("Error leyendo archivo: " + e.getMessage());
        }

        return generarResumen();
    }

    /**
     * Procesa una línea del CSV de Yape.
     * Formato esperado: Fecha, Nombre/Descripción, Monto, ...
     */
    private void procesarLineaYape(String linea, String separador, Map<String, DatosCliente> mapaClientes) {
        // Split preservando campos vacíos y contenido entre comillas
        String[] cols = linea.split(separador + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (cols.length < 3)
            return;

        // Extraer datos (ajustar índices según formato real de Yape)
        String fecha = limpiar(cols[0]);
        String descripcion = limpiar(cols.length > 1 ? cols[1] : "");
        String montoStr = limpiar(cols.length > 2 ? cols[2] : "0");

        // Limpiar monto
        double monto = parsearMonto(montoStr);
        if (monto <= 0)
            return;

        pagosEncontrados++;

        // Buscar nombre del cliente en la descripción
        String nombreEncontrado = buscarNombreEnDescripcion(descripcion, mapaClientes.keySet());

        if (nombreEncontrado != null) {
            DatosCliente datos = mapaClientes.get(nombreEncontrado);

            if (datos != null) {
                // Registrar el pago
                boolean registrado = registrarPago(datos, monto, fecha);

                if (registrado) {
                    pagosRegistrados++;
                    System.out.println("✅ Pago registrado: " + nombreEncontrado + " - S/ " + monto);
                } else {
                    errores.add("No se pudo registrar pago de: " + nombreEncontrado);
                }
            }
        } else {
            pagosNoCoinciden++;
            System.out.println("⚠️ No coincide: " + descripcion.substring(0, Math.min(50, descripcion.length())));
        }
    }

    /**
     * Busca un nombre de cliente en la descripción del pago.
     */
    private String buscarNombreEnDescripcion(String descripcion, Set<String> nombresClientes) {
        String descNorm = normalizar(descripcion);

        for (String nombre : nombresClientes) {
            if (descNorm.contains(nombre)) {
                return nombre;
            }
        }

        // Intentar match parcial (apellido primero o nombre primero)
        for (String nombre : nombresClientes) {
            String[] partes = nombre.split(" ");
            if (partes.length >= 2) {
                // Buscar apellido
                if (descNorm.contains(partes[partes.length - 1])) {
                    return nombre;
                }
            }
        }

        return null;
    }

    /**
     * Registra un pago en el sistema.
     */
    private boolean registrarPago(DatosCliente datos, double monto, String fecha) {
        try {
            // Buscar factura pendiente más antigua
            String sql = "SELECT id_factura, monto_total FROM factura " +
                    "WHERE id_suscripcion = ? AND id_estado = 1 " +
                    "ORDER BY fecha_vencimiento ASC LIMIT 1";

            try (Connection conn = Conexion.getConexion();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, datos.idSuscripcion);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int idFactura = rs.getInt("id_factura");

                    // Realizar el cobro usando el DAO existente
                    boolean cobrado = pagoDAO.realizarCobro(idFactura, monto, 1); // 1 = usuario sistema

                    if (cobrado) {
                        // Procesar reconexión si aplica
                        cobrosService.procesarPago(datos.idSuscripcion, monto);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            errores.add("Error SQL para " + datos.nombreCliente + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Carga el mapa de clientes activos.
     */
    private Map<String, DatosCliente> cargarMapaClientes() {
        Map<String, DatosCliente> mapa = new HashMap<>();

        String sql = "SELECT c.id_cliente, c.nombres, c.apellidos, c.telefono, " +
                "s.id_suscripcion, s.codigo_contrato " +
                "FROM suscripcion s " +
                "JOIN cliente c ON s.id_cliente = c.id_cliente " +
                "WHERE s.activo = 1 OR s.activo = 0"; // Incluir cortados para reconexión

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombres = rs.getString("nombres");
                String apellidos = rs.getString("apellidos");

                DatosCliente datos = new DatosCliente();
                datos.idCliente = rs.getInt("id_cliente");
                datos.idSuscripcion = rs.getInt("id_suscripcion");
                datos.nombreCliente = nombres + " " + apellidos;
                datos.telefono = rs.getString("telefono");

                // Guardar con ambas combinaciones de nombre
                String key1 = normalizar(nombres + " " + apellidos);
                String key2 = normalizar(apellidos + " " + nombres);

                mapa.put(key1, datos);
                mapa.put(key2, datos);

                // También buscar por apellido solo (para matches parciales)
                if (apellidos != null && !apellidos.isEmpty()) {
                    mapa.put(normalizar(apellidos), datos);
                }
            }

            System.out.println("📋 " + (mapa.size() / 2) + " clientes cargados para búsqueda.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mapa;
    }

    private double parsearMonto(String montoStr) {
        try {
            // Quitar símbolos de moneda y espacios
            String limpio = montoStr
                    .replace("S/", "")
                    .replace("s/", "")
                    .replace(",", ".")
                    .replaceAll("[^0-9.]", "")
                    .trim();
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String limpiar(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }

    private String normalizar(String s) {
        return s == null ? "" : s.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private void resetEstadisticas() {
        pagosEncontrados = 0;
        pagosRegistrados = 0;
        pagosNoCoinciden = 0;
        errores.clear();
    }

    private String generarResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════\n");
        sb.append("📊 RESUMEN DE IMPORTACIÓN\n");
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("📥 Pagos encontrados: ").append(pagosEncontrados).append("\n");
        sb.append("✅ Pagos registrados: ").append(pagosRegistrados).append("\n");
        sb.append("⚠️ No coincidentes: ").append(pagosNoCoinciden).append("\n");
        sb.append("❌ Errores: ").append(errores.size()).append("\n");

        if (!errores.isEmpty()) {
            sb.append("\nDetalles de errores:\n");
            for (int i = 0; i < Math.min(5, errores.size()); i++) {
                sb.append("  - ").append(errores.get(i)).append("\n");
            }
            if (errores.size() > 5) {
                sb.append("  ... y ").append(errores.size() - 5).append(" más\n");
            }
        }

        sb.append("═══════════════════════════════════════════════════\n");

        String resumen = sb.toString();
        System.out.println(resumen);
        return resumen;
    }

    // Getters para estadísticas
    public int getPagosEncontrados() {
        return pagosEncontrados;
    }

    public int getPagosRegistrados() {
        return pagosRegistrados;
    }

    public int getPagosNoCoinciden() {
        return pagosNoCoinciden;
    }

    public List<String> getErrores() {
        return errores;
    }

    /**
     * Clase interna para datos del cliente.
     */
    private static class DatosCliente {
        int idCliente;
        int idSuscripcion;
        String nombreCliente;
        String telefono;
    }
}
