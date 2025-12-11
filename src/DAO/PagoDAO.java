package DAO;

import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    // 1. Buscar facturas pendientes de un cliente
public List<Object[]> buscarDeudasPorCliente(String textoBusqueda) {
        List<Object[]> lista = new ArrayList<>();
        
        // CORRECCIÓN: JOINs ajustados y nombres de columnas reales
        // Usamos f.monto_total en lugar de f.monto
        // Filtramos por f.id_estado = 1 (Pendiente)
        String sql = "SELECT f.id_factura, c.nombres, c.apellidos, s.descripcion, f.periodo_mes, f.monto_total, f.fecha_vencimiento " +
                     "FROM factura f " +
                     "JOIN suscripcion sus ON f.id_suscripcion = sus.id_suscripcion " +
                     "JOIN cliente c ON sus.id_cliente = c.id_cliente " +  // <--- ASÍ LLEGAMOS AL CLIENTE
                     "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                     "WHERE f.id_estado = 1 " + // 1 = PENDIENTE
                     "AND (c.dni_cliente LIKE ? OR c.apellidos LIKE ? OR c.nombres LIKE ?) " +
                     "ORDER BY f.fecha_vencimiento ASC"; // Ordenar: deudas viejas primero

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + textoBusqueda + "%");
            ps.setString(2, "%" + textoBusqueda + "%");
            ps.setString(3, "%" + textoBusqueda + "%"); // Agregué búsqueda por nombre también
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id_factura"),
                    rs.getString("nombres") + " " + rs.getString("apellidos"),
                    rs.getString("descripcion"), 
                    rs.getString("periodo_mes"),
                    rs.getDouble("monto_total"), // Nombre correcto en tu BD
                    rs.getDate("fecha_vencimiento")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // 2. REGISTRAR PAGO (Transacción Maestra)
    // Esto hace 3 cosas: Marca la factura pagada, crea el movimiento en caja y actualiza deuda cliente
    public boolean realizarCobro(int idFactura, double monto, int idUsuario) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false); 

            // 1. Marcar Factura como PAGADA (Estado 2) y actualizar monto pagado
            String sql1 = "UPDATE factura SET id_estado = 2, monto_pagado = ? WHERE id_factura = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setDouble(1, monto);
                ps1.setInt(2, idFactura);
                ps1.executeUpdate();
            }

            // 2. Registrar en CAJA
            String sql2 = "INSERT INTO movimiento_caja (fecha, monto, descripcion, id_categoria, id_usuario) " +
                          "VALUES (NOW(), ?, CONCAT('Cobro Factura #', ?), 1, ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setDouble(1, monto);
                ps2.setInt(2, idFactura);
                ps2.setInt(3, idUsuario);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}