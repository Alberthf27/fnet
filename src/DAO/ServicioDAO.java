package DAO;

import bd.Conexion;
import modelo.Servicio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAO {

    // LISTAR (Para llenar la tabla)
    public List<Servicio> listar(String busqueda) {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT * FROM servicio WHERE descripcion LIKE ? ORDER BY id_servicio DESC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + busqueda + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Servicio s = new Servicio();
                    s.setIdServicio(rs.getInt("id_servicio"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setMensualidad(rs.getDouble("mensualidad"));
                    
                    // Asegúrate que tu BD tenga estas columnas, si no, darán error:
                    s.setVelocidadMb(rs.getInt("velocidad_mb")); 
                    s.setActivo(rs.getInt("activo"));
                    
                    lista.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    // --- AGREGAR ESTO EN TU CLASE ServicioDAO ---

    /**
     * Obtiene solo los servicios que están ACTIVOS (1) para mostrarlos en los ComboBox.
     */
    public List<Servicio> obtenerServiciosActivos() {
        List<Servicio> lista = new ArrayList<>();
        // Ordenamos por descripción para que aparezcan alfabéticamente
        String sql = "SELECT * FROM servicio WHERE activo = 1 ORDER BY descripcion ASC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Servicio s = new Servicio();
                s.setIdServicio(rs.getInt("id_servicio"));
                s.setDescripcion(rs.getString("descripcion"));
                s.setMensualidad(rs.getDouble("mensualidad"));
                
                // Mapeamos las columnas nuevas (Si tu BD ya las tiene)
                // Si te da error aquí, es porque no ejecutaste el ALTER TABLE que te di antes
                s.setVelocidadMb(rs.getInt("velocidad_mb"));
                s.setActivo(rs.getInt("activo"));

                lista.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // INSERTAR (Para el botón Nuevo)
    public boolean insertar(Servicio s) {
        String sql = "INSERT INTO servicio (descripcion, mensualidad, velocidad_mb, activo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, s.getDescripcion());
            ps.setDouble(2, s.getMensualidad());
            ps.setInt(3, s.getVelocidadMb());
            ps.setInt(4, s.getActivo());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al insertar servicio: " + e.getMessage());
            return false;
        }
    }

    // ACTUALIZAR (Para el botón Editar)
    public boolean actualizar(Servicio s) {
        String sql = "UPDATE servicio SET descripcion=?, mensualidad=?, velocidad_mb=?, activo=? WHERE id_servicio=?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, s.getDescripcion());
            ps.setDouble(2, s.getMensualidad());
            ps.setInt(3, s.getVelocidadMb());
            ps.setInt(4, s.getActivo());
            ps.setInt(5, s.getIdServicio());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar servicio: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR (Para el botón Eliminar)
    public boolean eliminar(int id) {
        // Opción A: Eliminación Física (Solo si no tiene contratos)
        String sql = "DELETE FROM servicio WHERE id_servicio = ?";
        
        // Opción B: Desactivación Lógica (Más segura) -> "UPDATE servicio SET activo = 0 WHERE id_servicio = ?"
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // Error común: Integridad referencial (Tiene clientes asociados)
            System.err.println("No se puede eliminar: " + e.getMessage());
            return false;
        }
    }
}