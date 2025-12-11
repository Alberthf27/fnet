package DAO;

import bd.Conexion;
import modelo.Suscripcion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuscripcionDAO {

public List<Suscripcion> listarPaginado(int limit, int offset) {
        List<Suscripcion> lista = new ArrayList<>();
        
        // --- CORRECCIÓN AQUÍ ---
        // Antes tenías: s.direccion_i
        // Ahora es:     s.direccion_instalacion
        String sql = "SELECT s.id_suscripcion, s.codigo_contrato, s.direccion_instalacion, s.fecha_inicio, s.activo, " +
                     "c.nombres, c.apellidos, sv.descripcion, sv.mensualidad " +
                     "FROM suscripcion s " +
                     "INNER JOIN cliente c ON s.id_cliente = c.id_cliente " +
                     "INNER JOIN servicio sv ON s.id_servicio = sv.id_servicio " +
                     "WHERE s.activo > 0 " + // Ocultar los dados de baja (0)
                     "ORDER BY s.id_suscripcion DESC " +
                     "LIMIT ? OFFSET ?";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    Suscripcion sus = new Suscripcion();
                    sus.setIdSuscripcion(rs.getInt("id_suscripcion"));
                    sus.setCodigoContrato(rs.getString("codigo_contrato"));
                    
                    // Asegúrate de leer la columna correcta aquí también
                    sus.setDireccionInstalacion(rs.getString("direccion_instalacion"));
                    
                    sus.setFechaInicio(rs.getDate("fecha_inicio"));
                    sus.setActivo(rs.getInt("activo"));
                    
                    // Datos Extras del JOIN
                    sus.setNombreCliente(rs.getString("nombres") + " " + rs.getString("apellidos"));
                    sus.setNombreServicio(rs.getString("descripcion"));
                    sus.setPrecioServicio(rs.getDouble("mensualidad"));
                    
                    lista.add(sus);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return lista;
    }

    public boolean actualizarContrato(int idSuscripcion, int idNuevoServicio, String nuevaDireccion) {
        String sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ? WHERE id_suscripcion = ?";
        
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idNuevoServicio);
            ps.setString(2, nuevaDireccion);
            ps.setInt(3, idSuscripcion);
            
            return ps.executeUpdate() > 0;
            
        } catch (java.sql.SQLException e) {
            System.err.println("Error actualizando contrato: " + e.getMessage());
            return false;
        }
    }
    
    // Cambiar estado (Cortar o Reconectar servicio)
    public boolean cambiarEstado(int idSuscripcion, int nuevoEstado) {
        String sql = "UPDATE suscripcion SET activo = ? WHERE id_suscripcion = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, idSuscripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crea una suscripción rápida para un cliente nuevo.
     * @param idCliente ID del cliente (Long para compatibilidad)
     * @param idServicio ID del servicio/plan (Long)
     */
    public boolean crearSuscripcionPorDefecto(Long idCliente, Long idServicio) {
        String sql = "INSERT INTO suscripcion (id_cliente, id_servicio, codigo_contrato, fecha_inicio, direccion_instalacion, activo) " +
                     "VALUES (?, ?, ?, NOW(), ?, 1)";
        
        // Obtenemos la dirección del cliente para ponerla en la instalación por defecto
        String direccion = "Dirección Principal"; // Valor por defecto
        
        // (Opcional) Consultar dirección real del cliente
        // String sqlDir = "SELECT direccion FROM cliente WHERE id_cliente = ?"; ...
        
        // Generar un código de contrato simple
        String codigo = "CNT-" + System.currentTimeMillis(); 

        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, idCliente);
            ps.setLong(2, idServicio); // MySQL lo convertirá a INT automáticamente
            ps.setString(3, codigo);
            ps.setString(4, direccion); 
            
            return ps.executeUpdate() > 0;
            
        } catch (java.sql.SQLException e) {
            System.err.println("Error al crear suscripción por defecto: " + e.getMessage());
            return false;
        }
    }
    
    
    /**
     * Busca el ID y Nombre de un cliente dado su DNI (Para cambio de titular).
     * Retorna un arreglo: [0]=ID, [1]=NombreCompleto. Retorna null si no existe.
     */
    public String[] buscarClientePorDni(String dni) {
        String sql = "SELECT id_cliente, nombres, apellidos FROM cliente WHERE dni_cliente = ? AND activo = 1";
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dni);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("id_cliente")),
                        rs.getString("nombres") + " " + rs.getString("apellidos")
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * ACTUALIZACIÓN MAESTRA: Plan, Dirección y Titular.
     */
    public boolean actualizarContratoCompleto(int idSuscripcion, int idServicio, String direccion, int idNuevoCliente) {
        // Actualizamos todo de una vez
        String sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ?, id_cliente = ? WHERE id_suscripcion = ?";
        
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idServicio);
            ps.setString(2, direccion);
            ps.setInt(3, idNuevoCliente); // Nuevo dueño
            ps.setInt(4, idSuscripcion);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.err.println("Error actualizando contrato: " + e.getMessage());
            return false;
        }
    }
    
    public int obtenerIdClienteDeContrato(int idSuscripcion) {
        String sql = "SELECT id_cliente FROM suscripcion WHERE id_suscripcion = ?";
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSuscripcion);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {}
        return 0;
    }
    
    
    
}