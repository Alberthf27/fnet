package util;

import bd.Conexion;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ImportadorExcel {

    // Clase interna para guardar datos clave del cliente en memoria
    class DatosCliente {
        int idCliente;
        int idSuscripcion;
        double precioPlan;
        java.sql.Date fechaInicio;

        public DatosCliente(int idC, int idS, double precio, java.sql.Date fInicio) {
            this.idCliente = idC;
            this.idSuscripcion = idS;
            this.precioPlan = precio;
            this.fechaInicio = fInicio;
        }
    }

    // Cache: Nombre -> Datos
    private Map<String, DatosCliente> mapaClientes = new HashMap<>();

    public void procesarArchivo(String rutaArchivo) {
        if (rutaArchivo.endsWith(".xlsx") || rutaArchivo.endsWith(".xls")) {
            System.err.println("❌ ERROR: Debes guardar el archivo como .CSV (Delimitado por comas) primero.");
            return;
        }

        System.out.println(">>> Iniciando importación inteligente (Datos + Pagos 2025)...");
        cargarMapaClientes();

        int procesados = 0;
        int pagosRegistrados = 0;
        int deudasRegistradas = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            int numeroLinea = 0;
            String separador = ","; 

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                
                // Detección automática de separador (coma o punto y coma)
                if (numeroLinea == 2 && linea.contains(";")) separador = ";";
                if (numeroLinea < 3) continue; // Saltar cabeceras

                // Split preservando vacíos
                String[] cols = linea.split(separador + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (cols.length < 10) continue;

                try {
                    // --- 1. DATOS DEL CLIENTE ---
                    // Indices: 2=Garantia, 3=Inicio, 6=Nombre, 9=DiaPago
                    String rawGarantia = cols.length > 2 ? limpiar(cols[2]) : "";
                    String rawFecha = cols.length > 3 ? limpiar(cols[3]) : "";
                    String rawNombre = cols.length > 6 ? limpiar(cols[6]) : "";
                    String rawDia = cols.length > 9 ? limpiar(cols[9]) : "";

                    if (rawNombre.isEmpty()) continue;

                    String nombreNorm = normalizar(rawNombre);
                    DatosCliente datos = mapaClientes.get(nombreNorm);

                    if (datos != null) {
                        // A. Actualizar Datos Maestros
                        actualizarSuscripcion(datos.idCliente, rawGarantia, rawFecha, rawDia);
                        procesados++;

                        // B. PROCESAR PAGOS (Columnas W a AH -> Índices 22 a 33)
                        // Asumimos que son ENE-DIC del año 2025 según tu CSV
                        int año = 2025; 
                        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

                        for (int i = 0; i < 12; i++) {
                            int colIndex = 22 + i; // W es la 22
                            if (colIndex < cols.length) {
                                String valorCelda = limpiar(cols[colIndex]);
                                int resultado = procesarPagoMensual(datos, i + 1, año, meses[i], valorCelda);
                                
                                if (resultado == 1) pagosRegistrados++;
                                if (resultado == 2) deudasRegistradas++;
                            }
                        }
                        System.out.println("✅ " + nombreNorm + " -> Procesado.");
                    } else {
                        // System.out.println("⚠️ Cliente nuevo (no en BD): " + nombreNorm);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error línea " + numeroLinea + ": " + e.getMessage());
                }
            }

            System.out.println("\n=== REPORTE FINAL ===");
            System.out.println("Clientes Actualizados: " + procesados);
            System.out.println("Pagos Registrados (Caja): " + pagosRegistrados);
            System.out.println("Deudas Generadas: " + deudasRegistradas);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Analiza la celda del Excel y decide si crear factura Pagada o Pendiente.
     * Retorna: 0=Nada, 1=Pago, 2=Deuda
     */
    private int procesarPagoMensual(DatosCliente datos, int mes, int anio, String nombreMes, String valorCelda) {
        double montoPagado = 0.0;
        boolean esPago = false;
        boolean esDeuda = false;

        // Limpieza de moneda "S/ "
        String valor = valorCelda.replace("S/", "").replace("s/", "").trim();

        if (valor.isEmpty()) return 0;

        try {
            // Caso 1: Tiene un número positivo -> ES PAGO
            // Caso 2: Es "0", "-", "0.00" -> ES DEUDA (Si ya pasó la fecha de inicio)
            if (valor.matches(".*[1-9].*")) { 
                montoPagado = Double.parseDouble(valor);
                esPago = true;
            } else if (valor.equals("-") || valor.equals("0") || valor.equals("0.00")) {
                esDeuda = true;
            }
        } catch (Exception e) { return 0; }

        // Validar fecha de inicio para no cobrar meses anteriores al contrato
        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (datos.fechaInicio != null) {
            cal.setTime(datos.fechaInicio);
            int mesInicio = cal.get(java.util.Calendar.MONTH) + 1; // 1-12
            int anioInicio = cal.get(java.util.Calendar.YEAR);
            
            // Si el mes es anterior al contrato, ignoramos la deuda (el pago sí se respeta por si acaso)
            if (esDeuda) {
                if (anio < anioInicio || (anio == anioInicio && mes < mesInicio)) return 0;
            }
        }

        String periodo = nombreMes + " " + anio;
        
        // --- LOGICA BD ---
        if (esPago) {
            return registrarFactura(datos, periodo, datos.precioPlan, montoPagado, 2); // 2 = PAGADO
        } else if (esDeuda) {
            return registrarFactura(datos, periodo, datos.precioPlan, 0.0, 1); // 1 = PENDIENTE
        }
        
        return 0;
    }

    /**
     * Inserta la factura si no existe.
     * Estado: 1=Pendiente, 2=Pagado
     */
    private int registrarFactura(DatosCliente d, String periodo, double total, double pagado, int estado) {
        // 1. Verificar si ya existe para no duplicar
        String check = "SELECT id_factura FROM factura WHERE id_suscripcion = ? AND periodo_mes = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement psCheck = conn.prepareStatement(check)) {
            
            psCheck.setInt(1, d.idSuscripcion);
            psCheck.setString(2, periodo);
            
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return 0; // Ya existe, no hacemos nada
            }

            // 2. Insertar Factura
            String sql = "INSERT INTO factura (id_suscripcion, fecha_emision, fecha_vencimiento, monto_total, monto_pagado, id_estado, periodo_mes, codigo_factura) "
                       + "VALUES (?, NOW(), NOW(), ?, ?, ?, ?, ?)";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // Generamos un código corto: F + ID_SUSCRIPCION + - + NUMERO_ALEATORIO
                // Ejemplo: F105-8493 (Máximo 10-12 caracteres)
                String codigoCorto = "F" + d.idSuscripcion + "-" + (System.currentTimeMillis() % 100000);

                ps.setInt(1, d.idSuscripcion);
                ps.setDouble(2, total);
                ps.setDouble(3, pagado);
                ps.setInt(4, estado);
                ps.setString(5, periodo);
                ps.setString(6, codigoCorto); // <--- AQUI ESTABA EL ERROR ANTES
                
                ps.executeUpdate();
                return (estado == 2) ? 1 : 2;
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar factura: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

   private void cargarMapaClientes() {
        System.out.print("Cargando caché de suscripciones activas... ");
        
        // Esta consulta busca SUSCRIPCIONES (s.id_suscripcion) usando los nombres del cliente asociado
        String sql = "SELECT c.id_cliente, c.nombres, c.apellidos, s.id_suscripcion, s.fecha_inicio, serv.mensualidad " +
                     "FROM suscripcion s " + // <--- PARTIMOS DE LA TABLA SUSCRIPCION
                     "JOIN cliente c ON s.id_cliente = c.id_cliente " +
                     "JOIN servicio serv ON s.id_servicio = serv.id_servicio " +
                     "WHERE s.activo = 1"; // Solo nos interesan contratos activos
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int countReal = 0;
            while (rs.next()) {
                countReal++;
                String nom = rs.getString("nombres");
                String ape = rs.getString("apellidos");
                
                DatosCliente datos = new DatosCliente(
                    rs.getInt("id_cliente"),
                    rs.getInt("id_suscripcion"), // <--- ESTE ES EL DATO IMPORTANTE QUE USAMOS
                    rs.getDouble("mensualidad"),
                    rs.getDate("fecha_inicio")
                );
                
                // Guardamos las dos combinaciones para asegurar que el Excel coincida
                String key1 = normalizar(nom + " " + ape);
                String key2 = normalizar(ape + " " + nom);
                
                mapaClientes.put(key1, datos);
                mapaClientes.put(key2, datos);
            }
            System.out.println(countReal + " suscripciones encontradas (" + mapaClientes.size() + " índices de búsqueda).");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void actualizarSuscripcion(int idCliente, String garantia, String fecha, String dia) {
        int mesAdel = 1, equipos = 1;
        double garMonto = 0.0;

        if (garantia.equals("MA")) { mesAdel=1; equipos=1; }
        else if (garantia.equals("SEMA")) { mesAdel=1; equipos=0; }
        else if (garantia.equals("FM") || garantia.equals("SEFM")) { mesAdel=0; equipos=0; }
        else if (garantia.equals("SIFM")) { mesAdel=0; equipos=1; }
        else { try { garMonto = Double.parseDouble(garantia); } catch(Exception e){} }

        java.sql.Date sqlFecha = null;
        try {
            if (fecha.contains("/")) { // Formato Excel DD/MM/YYYY
                String[] p = fecha.split("/");
                sqlFecha = java.sql.Date.valueOf(p[2] + "-" + p[1] + "-" + p[0]);
            } else if (fecha.contains("-")) {
                sqlFecha = java.sql.Date.valueOf(fecha);
            }
        } catch(Exception e) {}

        int diaPago = 0;
        try { diaPago = Integer.parseInt(dia); } catch(Exception e){}

        StringBuilder sql = new StringBuilder("UPDATE suscripcion SET mes_adelantado=?, equipos_prestados=?, garantia=? ");
        if (sqlFecha != null) sql.append(", fecha_inicio=? ");
        if (diaPago > 0) sql.append(", dia_pago=? ");
        sql.append("WHERE id_cliente=? AND activo=1");

        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setInt(i++, mesAdel);
            ps.setInt(i++, equipos);
            ps.setDouble(i++, garMonto);
            if (sqlFecha != null) ps.setDate(i++, sqlFecha);
            if (diaPago > 0) ps.setInt(i++, diaPago);
            ps.setInt(i++, idCliente);
            ps.executeUpdate();
        } catch(SQLException e) { e.printStackTrace(); }
    }

    // Utils
    private String limpiar(String s) { return s.replace("\"", "").trim(); }
    private String normalizar(String s) { return s.trim().toUpperCase().replaceAll("\\s+", " "); }

    public static void main(String[] args) {
        ImportadorExcel imp = new ImportadorExcel();
        // ⚠️ RECUERDA: Ruta al .csv (no al .xlsx)
        imp.procesarArchivo("E:\\ALBERTH ALM\\DESCARGAS\\Libro1.csv");
    }
}