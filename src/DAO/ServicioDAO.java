package DAO;

import bd.Conexion;
import modelo.Servicio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAO {
    
    public List<Servicio> obtenerServiciosActivos() {
        List<Servicio> lista = new ArrayList<>();
        // Ajustado a los nombres reales de tu BD (ver imagen anterior: id_servicio, descripcion, mensualidad)
        String sql = "SELECT id_servicio, descripcion, mensualidad FROM servicio"; 
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while(rs.next()) {
                Servicio s = new Servicio();
                // AQUÍ ESTABA EL ERROR: Usamos getInt, no getLong
                s.setIdServicio(rs.getInt("id_servicio")); 
                s.setDescripcion(rs.getString("descripcion"));
                s.setMensualidad(rs.getDouble("mensualidad"));
                lista.add(s);
            }
        } catch (Exception e) { 
            System.err.println("Error cargando servicios: " + e.getMessage());
        }
        return lista;
    }
    
    
    
    
}