package DAO;

import bd.Conexion;
import java.sql.*;

public class FacturacionDAO {

    // Este método se debe llamar el día 1 de cada mes
    public void generarDeudaMensual() {
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmtInsertarFactura = null;
        PreparedStatement stmtActualizarCliente = null;
        
        try {
            conn = conexion.conectar();
            conn.setAutoCommit(false); // Importante: Todo o nada (Transacción)

            // 1. Seleccionar todas las suscripciones activas (Estado 1)
            // Unimos con SERVICIO para saber cuánto cobrar
            String sqlSuscripciones = "SELECT s.ID_SUSCRIPCION, s.CLIENTE_ID_CLIENTE, s.SERVICIO_ID_SERVICIO, srv.MENSUALIDAD " +
                                      "FROM SUSCRIPCION s " +
                                      "JOIN SERVICIO srv ON s.SERVICIO_ID_SERVICIO = srv.ID_SERVICIO " +
                                      "WHERE s.ESTSUSC_ID_ESTADO = 1"; // 1 = Activo
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlSuscripciones);

            String sqlFactura = "INSERT INTO FACTURA (MONTO, FECHA_EMISION, FECHA_VENCIMIENTO, ID_SUSCRIPCION, ID_ESTADO, MONTO_PENDIENTE) " +
                                "VALUES (?, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), ?, 1, ?)"; // Estado 1 = Pendiente
            
            String sqlUpdateCliente = "UPDATE CLIENTE SET DEUDA = DEUDA + ? WHERE ID_CLIENTE = ?";

            stmtInsertarFactura = conn.prepareStatement(sqlFactura);
            stmtActualizarCliente = conn.prepareStatement(sqlUpdateCliente);

            int facturasGeneradas = 0;

            while (rs.next()) {
                double mensualidad = rs.getDouble("MENSUALIDAD");
                long idSuscripcion = rs.getLong("ID_SUSCRIPCION");
                long idCliente = rs.getLong("CLIENTE_ID_CLIENTE");

                // A. Crear la factura en BD
                stmtInsertarFactura.setDouble(1, mensualidad);
                stmtInsertarFactura.setLong(2, idSuscripcion);
                stmtInsertarFactura.setDouble(3, mensualidad); // Al inicio, todo está pendiente
                stmtInsertarFactura.addBatch(); // Añadir al paquete

                // B. Aumentar la deuda global del cliente
                stmtActualizarCliente.setDouble(1, mensualidad);
                stmtActualizarCliente.setLong(2, idCliente);
                stmtActualizarCliente.addBatch();
                
                facturasGeneradas++;
            }

            // Ejecutar todos los cambios de golpe
            stmtInsertarFactura.executeBatch();
            stmtActualizarCliente.executeBatch();
            
            conn.commit(); // Confirmar cambios
            System.out.println("✅ Se generaron " + facturasGeneradas + " facturas nuevas este mes.");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Si falla algo, deshacer todo
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Cerrar recursos... (omito por brevedad, pero debes ponerlo)
        }
    }
}