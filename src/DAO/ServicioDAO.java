package DAO;

import bd.Conexion;
import modelo.Servicio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAO {
    
    public Servicio obtenerServicioPorId(Long idServicio) {
        Servicio servicio = null;
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = conexion.conectar();
            String sql = "SELECT * FROM SERVICIO WHERE ID_SERVICIO = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idServicio);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                servicio = new Servicio();
                servicio.setIdServicio(rs.getLong("ID_SERVICIO"));
                servicio.setDescripcion(rs.getString("DESCRIPCION"));
                servicio.setMensualidad(rs.getBigDecimal("MENSUALIDAD"));
                servicio.setAnchoBanda(rs.getBigDecimal("ANCHO_BANDA"));
                servicio.setCanales(rs.getLong("CANALES"));
                servicio.setUsuarioIdUsuario(rs.getLong("USUARIO_ID_USUARIO"));
                servicio.setProveedorIdProveedor(rs.getLong("PROVEEDOR_ID_PROVEEDOR"));
                servicio.setTiposervicioIdTipoServicio(rs.getLong("TIPOSERVICIO_ID_TIPO_SERVICIO"));
                servicio.setNombre(rs.getString("NOMBRE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conexion.desconectar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return servicio;
    }
    
    public List<Servicio> obtenerTodosServicios() {
        List<Servicio> servicios = new ArrayList<>();
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = conexion.conectar();
            String sql = "SELECT * FROM SERVICIO ORDER BY NOMBRE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Servicio servicio = new Servicio();
                servicio.setIdServicio(rs.getLong("ID_SERVICIO"));
                servicio.setDescripcion(rs.getString("DESCRIPCION"));
                servicio.setMensualidad(rs.getBigDecimal("MENSUALIDAD"));
                servicio.setAnchoBanda(rs.getBigDecimal("ANCHO_BANDA"));
                servicio.setCanales(rs.getLong("CANALES"));
                servicio.setUsuarioIdUsuario(rs.getLong("USUARIO_ID_USUARIO"));
                servicio.setProveedorIdProveedor(rs.getLong("PROVEEDOR_ID_PROVEEDOR"));
                servicio.setTiposervicioIdTipoServicio(rs.getLong("TIPOSERVICIO_ID_TIPO_SERVICIO"));
                servicio.setNombre(rs.getString("NOMBRE"));
                servicios.add(servicio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conexion.desconectar();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return servicios;
    }
}