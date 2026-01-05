package DAO;

import bd.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO para gestión de usuarios, roles y permisos.
 */
public class PermisoDAO {

    /**
     * Obtiene los códigos de permiso para un rol específico.
     */
    public Set<String> obtenerPermisosDeRol(long idRol) {
        Set<String> permisos = new HashSet<>();
        String sql = "SELECT p.codigo FROM rol_permiso rp " +
                "JOIN permiso p ON rp.id_permiso = p.id_permiso " +
                "WHERE rp.id_rol = ?";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                permisos.add(rs.getString("codigo"));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo permisos: " + e.getMessage());
        }
        return permisos;
    }

    /**
     * Verifica si un rol tiene un permiso específico.
     */
    public boolean tienePermiso(long idRol, String codigoPermiso) {
        String sql = "SELECT 1 FROM rol_permiso rp " +
                "JOIN permiso p ON rp.id_permiso = p.id_permiso " +
                "WHERE rp.id_rol = ? AND p.codigo = ?";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            ps.setString(2, codigoPermiso);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("Error verificando permiso: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos los usuarios del sistema.
     */
    public List<Object[]> listarUsuarios() {
        List<Object[]> usuarios = new ArrayList<>();
        String sql = "SELECT u.id_usuario, e.id_empleado, e.nombres, e.apellidos, e.dni, " +
                "u.codigo_acceso, r.nombre AS rol, u.id_rol, u.id_estado " +
                "FROM usuario u " +
                "JOIN empleado e ON u.id_empleado = e.id_empleado " +
                "JOIN rol r ON u.id_rol = r.id_rol " +
                "ORDER BY e.nombres";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(new Object[] {
                        rs.getInt("id_usuario"),
                        rs.getInt("id_empleado"),
                        rs.getString("nombres") + " " + rs.getString("apellidos"),
                        rs.getString("dni"),
                        rs.getString("codigo_acceso"),
                        rs.getString("rol"),
                        rs.getInt("id_rol"),
                        rs.getInt("id_estado") == 1 ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (SQLException e) {
            System.err.println("Error listando usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Lista todos los roles disponibles.
     */
    public List<Object[]> listarRoles() {
        List<Object[]> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre FROM rol ORDER BY id_rol";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Object[] {
                        rs.getInt("id_rol"),
                        rs.getString("nombre")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error listando roles: " + e.getMessage());
        }
        return roles;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     */
    public boolean crearUsuario(String nombres, String apellidos, String dni,
            String codigoAcceso, String contrasena, int idRol) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            // 1. Crear empleado
            String sqlEmpleado = "INSERT INTO empleado (nombres, apellidos, dni) VALUES (?, ?, ?)";
            int idEmpleado = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombres);
                ps.setString(2, apellidos);
                ps.setString(3, dni);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    idEmpleado = rs.getInt(1);
                }
            }

            // 2. Crear usuario
            String sqlUsuario = "INSERT INTO usuario (id_empleado, codigo_acceso, contrasena, id_rol, id_estado) " +
                    "VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario)) {
                ps.setInt(1, idEmpleado);
                ps.setString(2, codigoAcceso);
                ps.setString(3, contrasena);
                ps.setInt(4, idRol);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Usuario creado: " + nombres + " " + apellidos);
            return true;

        } catch (SQLException e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Actualiza el rol de un usuario.
     */
    public boolean actualizarRolUsuario(int idUsuario, int nuevoRol) {
        String sql = "UPDATE usuario SET id_rol = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoRol);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando rol: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia el estado de un usuario (activar/desactivar).
     */
    public boolean cambiarEstadoUsuario(int idUsuario, boolean activo) {
        String sql = "UPDATE usuario SET id_estado = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cambiando estado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resetea la contraseña de un usuario.
     */
    public boolean resetearContrasena(int idUsuario, String nuevaContrasena) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevaContrasena);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error reseteando contrasena: " + e.getMessage());
            return false;
        }
    }
}
