package DAO;

import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PagoDAO {

    // 1. Buscar deudas (Actualizado para traer id_suscripcion)
    public List<Object[]> buscarDeudasPorCliente(String textoBusqueda) {
        List<Object[]> lista = new ArrayList<>();
        // AGREGAMOS: sus.id_suscripcion (índice 7 en el array resultante)
        String sql = "SELECT f.id_factura, c.nombres, c.apellidos, s.descripcion, f.periodo_mes, f.monto_total, f.fecha_vencimiento, sus.id_suscripcion " +
                     "FROM factura f " +
                     "JOIN suscripcion sus ON f.id_suscripcion = sus.id_suscripcion " +
                     "JOIN cliente c ON sus.id_cliente = c.id_cliente " +
                     "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                     "WHERE f.id_estado = 1 " + // PENDIENTE
                     "AND (c.dni_cliente LIKE ? OR c.apellidos LIKE ? OR c.nombres LIKE ?) " +
                     "ORDER BY f.fecha_vencimiento ASC"; // IMPORTANTE: Ordenar por antigüedad

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String parametro = "%" + textoBusqueda + "%";
            ps.setString(1, parametro);
            ps.setString(2, parametro);
            ps.setString(3, parametro);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_factura"),
                    rs.getString("nombres") + " " + rs.getString("apellidos"),
                    rs.getString("descripcion"), 
                    rs.getString("periodo_mes"),
                    rs.getDouble("monto_total"), 
                    rs.getDate("fecha_vencimiento"),
                    rs.getInt("id_suscripcion") // DATO NUEVO
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // 2. Realizar Cobro (Igual que antes, lo mantienes)
    public boolean realizarCobro(int idFactura, double monto, int idUsuario) {
        // ... (Tu código actual de cobrar está bien) ...
        // Solo asegúrate de que use id_estado = 2 para PAGADO
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);
            
            String sql1 = "UPDATE factura SET id_estado = 2, monto_pagado = ? WHERE id_factura = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setDouble(1, monto);
                ps1.setInt(2, idFactura);
                ps1.executeUpdate();
            }
            
            String sql2 = "INSERT INTO movimiento_caja (fecha, monto, descripcion, id_categoria, id_usuario) VALUES (NOW(), ?, CONCAT('Cobro Factura #', ?), 1, ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setDouble(1, monto);
                ps2.setInt(2, idFactura);
                ps2.setInt(3, idUsuario);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
            return false;
        } finally {
            try { if(conn!=null) conn.close(); } catch(Exception ex){}
        }
    }

    // 3. NUEVO: ADELANTAR MES (Generar siguiente factura)
    public boolean generarSiguienteFactura(int idSuscripcion) {
        // Lógica: Buscar la última fecha de vencimiento de este contrato y sumarle 1 mes
        String sqlUltima = "SELECT fecha_vencimiento FROM factura WHERE id_suscripcion = ? ORDER BY fecha_vencimiento DESC LIMIT 1";
        LocalDate nuevaFecha = LocalDate.now();
        
        try (Connection conn = Conexion.getConexion()) {
            // A. Obtener última fecha
            try (PreparedStatement ps = conn.prepareStatement(sqlUltima)) {
                ps.setInt(1, idSuscripcion);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Date fechaSql = rs.getDate(1);
                    if (fechaSql != null) {
                        nuevaFecha = fechaSql.toLocalDate().plusMonths(1);
                    }
                }
            }
            
            // B. Calcular nombre del mes (Ej: "Enero 2026")
            // Usamos Locale español para que salga "Enero" y no "January"
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
            String nuevoPeriodo = nuevaFecha.format(fmt);
            // Capitalizar primera letra (enero -> Enero)
            nuevoPeriodo = nuevoPeriodo.substring(0, 1).toUpperCase() + nuevoPeriodo.substring(1);

            // C. Insertar nueva factura PENDIENTE
            // Obtenemos el precio del plan directamente en el INSERT con subconsulta
            String sqlInsert = "INSERT INTO factura (id_suscripcion, fecha_emision, fecha_vencimiento, monto_total, monto_pagado, id_estado, codigo_factura, periodo_mes) " +
                               "SELECT ?, NOW(), ?, s.mensualidad, 0.00, 1, CONCAT('F-', FLOOR(RAND()*100000)), ? " +
                               "FROM suscripcion sus JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                               "WHERE sus.id_suscripcion = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, idSuscripcion);
                ps.setDate(2, java.sql.Date.valueOf(nuevaFecha));
                ps.setString(3, nuevoPeriodo);
                ps.setInt(4, idSuscripcion);
                return ps.executeUpdate() > 0;
            }
            
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false;
        }
    }
    
    // En src/DAO/PagoDAO.java
    public List<Object[]> obtenerHistorialCompleto(int idSuscripcion) {
        List<Object[]> lista = new ArrayList<>();
        // Traemos todo: Pagados (2), Pendientes (1), Anulados (0)
        String sql = "SELECT periodo_mes, fecha_vencimiento, monto_total, monto_pagado, fecha_pago, id_estado " +
                     "FROM factura WHERE id_suscripcion = ? ORDER BY fecha_vencimiento DESC";
                     
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSuscripcion);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String estado = "DESCONOCIDO";
                int idEst = rs.getInt("id_estado");
                if(idEst == 1) estado = "PENDIENTE";
                else if(idEst == 2) estado = "PAGADO";
                else if(idEst == 0) estado = "ANULADO";
                
                lista.add(new Object[]{
                    rs.getString("periodo_mes"),
                    rs.getDate("fecha_vencimiento"),
                    rs.getDouble("monto_total"),
                    estado,
                    rs.getDate("fecha_pago") // Puede ser null
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
    
    
    // --- AGREGAR ESTO EN PagoDAO.java ---

    // Obtener los últimos N pagos registrados (Para el Dashboard)
    public List<Object[]> obtenerUltimosPagos(int limite) {
        List<Object[]> lista = new ArrayList<>();
        // Unimos Factura -> Suscripción -> Cliente para tener el nombre
        // Filtramos por id_estado = 2 (Pagado)
        String sql = "SELECT f.fecha_pago, c.nombres, c.apellidos, f.monto_pagado " +
                     "FROM factura f " +
                     "JOIN suscripcion s ON f.id_suscripcion = s.id_suscripcion " +
                     "JOIN cliente c ON s.id_cliente = c.id_cliente " +
                     "WHERE f.id_estado = 2 " + 
                     "ORDER BY f.fecha_pago DESC LIMIT ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            
            // Formato de hora simple
            java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
            
            while(rs.next()) {
                java.sql.Timestamp fechaPago = rs.getTimestamp("fecha_pago");
                String hora = (fechaPago != null) ? sdfHora.format(fechaPago) : "--:--";
                
                lista.add(new Object[]{
                    hora,                                               // Col 1: Hora
                    rs.getString("nombres") + " " + rs.getString("apellidos"), // Col 2: Cliente
                    "S/. " + rs.getDouble("monto_pagado"),              // Col 3: Monto
                    "Efectivo"                                          // Col 4: Método (Hardcodeado por ahora)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
    
    // En src/DAO/PagoDAO.java
    public List<Object[]> buscarDeudasPorSuscripcion(int idSuscripcion) {
        List<Object[]> lista = new ArrayList<>();
        // Solo facturas pendientes (id_estado = 1)
        String sql = "SELECT id_factura, periodo_mes, monto_total, fecha_vencimiento " +
                     "FROM factura WHERE id_suscripcion = ? AND id_estado = 1 ORDER BY fecha_vencimiento ASC";
        
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSuscripcion);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("id_factura"),
                        rs.getString("periodo_mes"),
                        rs.getDouble("monto_total"),
                        rs.getDate("fecha_vencimiento")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}