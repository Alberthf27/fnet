package DAO;

import bd.Conexion;
import modelo.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Valida acceso usando CÓDIGO (String "0001") y CONTRASEÑA.
     */
    public Empleado login(String codigoAcceso, String password) {
        Empleado empleado = null;

        // SQL: Traemos id_empleado explícitamente para evitar confusiones
        String sql = "SELECT e.id_empleado, e.nombres, e.apellidos, r.nombre AS rol_nombre "
                + "FROM usuario u "
                + "INNER JOIN empleado e ON u.id_empleado = e.id_empleado "
                + "INNER JOIN rol r ON e.id_rol = r.id_rol "
                + "WHERE u.codigo_acceso = ? AND u.contrasena = ? AND u.id_estado = 1";

        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoAcceso);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empleado = new Empleado();

                    // 1. ID Corregido (Long)
                    empleado.setIdEmpleado(rs.getLong("id_empleado"));

                    // 2. Datos básicos
                    empleado.setNombres(rs.getString("nombres"));
                    empleado.setApellidos(rs.getString("apellidos"));

                    // 3. ¡DESCOMENTA ESTO! (Para que salga el rol en el Dashboard)
                    // Asegúrate de que tu consulta SQL tenga: "r.nombre AS rol_nombre"
                    empleado.setCargo(rs.getString("rol_nombre"));

                    System.out.println("✅ Login exitoso: " + empleado.getNombres());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en Login DAO: " + e.getMessage());
            e.printStackTrace();
        }
        return empleado;
    }
}
