package DAO;

import bd.Conexion;
import java.sql.*;

public class SuscripcionDAO {
    
    public boolean crearSuscripcionPorDefecto(Long idCliente, Long idInstalacion) {
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean resultado = false;

        try {
            conn = conexion.conectar();
            String sql = "INSERT INTO SUSCRIPCION (FECHA_INICIO, SERVICIO_ID_SERVICIO, INSTALACION_ID_INSTALACION, CLIENTE_ID_CLIENTE, ESTSUSC_ID_ESTADO) " +
                        "VALUES (SYSDATE, 1, ?, ?, 1)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idInstalacion);
            stmt.setLong(2, idCliente);

            int filasAfectadas = stmt.executeUpdate();
            resultado = filasAfectadas > 0;
            
            if (resultado) {
                System.out.println("Suscripción por defecto creada para cliente ID: " + idCliente);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear suscripción: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conexion.desconectar();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return resultado;
    }
    
    // Método para obtener una instalación disponible (puedes modificarlo según tu lógica)
    public Long obtenerInstalacionDisponible() {
        // Por simplicidad, retornamos 1 como valor por defecto
        // En un sistema real, buscarías una instalación disponible
        return 1L;
    }
}