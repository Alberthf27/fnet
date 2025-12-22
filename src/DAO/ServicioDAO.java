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
     * Obtiene solo los servicios que están ACTIVOS (1) para mostrarlos en los
     * ComboBox.
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

        // Opción B: Desactivación Lógica (Más segura) -> "UPDATE servicio SET activo =
        // 0 WHERE id_servicio = ?"

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

    // ============================================================
    // MÉTODOS PARA GESTIÓN DE ELIMINACIÓN DE PLANES CON CLIENTES
    // ============================================================

    /**
     * Cuenta cuántas suscripciones ACTIVAS tiene un servicio.
     * Útil para validar antes de eliminar un plan.
     */
    public int contarSuscripcionesPorServicio(int idServicio) {
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE id_servicio = ? AND activo = 1 AND fecha_cancelacion IS NULL";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtiene la lista de suscripciones (clientes) afiliados a un servicio.
     * Retorna: [id_suscripcion, codigo_contrato, nombre_cliente, direccion, activo]
     */
    public List<Object[]> obtenerSuscripcionesPorServicio(int idServicio) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT s.id_suscripcion, s.codigo_contrato, " +
                "CONCAT(c.nombres, ' ', c.apellidos) as nombre_cliente, " +
                "s.direccion_instalacion, s.activo " +
                "FROM suscripcion s " +
                "JOIN cliente c ON s.id_cliente = c.id_cliente " +
                "WHERE s.id_servicio = ? AND s.fecha_cancelacion IS NULL " +
                "ORDER BY c.apellidos, c.nombres";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Object[] {
                        rs.getInt("id_suscripcion"),
                        rs.getString("codigo_contrato"),
                        rs.getString("nombre_cliente"),
                        rs.getString("direccion_instalacion"),
                        rs.getInt("activo") == 1 ? "ACTIVO" : "CORTADO"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Migra todas las suscripciones de un servicio a otro.
     * Usado cuando se quiere eliminar un plan y mover los clientes a otro.
     * 
     * @return Cantidad de suscripciones migradas
     */
    public int migrarSuscripciones(int idServicioOrigen, int idServicioDestino) {
        String sql = "UPDATE suscripcion SET id_servicio = ? WHERE id_servicio = ? AND fecha_cancelacion IS NULL";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicioDestino);
            ps.setInt(2, idServicioOrigen);

            int migradas = ps.executeUpdate();
            System.out.println("✅ " + migradas + " suscripciones migradas del plan " + idServicioOrigen + " al plan "
                    + idServicioDestino);
            return migradas;

        } catch (SQLException e) {
            System.err.println("Error al migrar suscripciones: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Desactiva un servicio (soft delete).
     * El servicio ya no aparece en los ComboBox pero los contratos existentes se
     * mantienen.
     */
    public boolean desactivarServicio(int idServicio) {
        String sql = "UPDATE servicio SET activo = 0 WHERE id_servicio = ?";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            boolean desactivado = ps.executeUpdate() > 0;

            if (desactivado) {
                System.out.println("✅ Servicio ID " + idServicio + " desactivado (suscripciones mantienen el plan)");
            }
            return desactivado;

        } catch (SQLException e) {
            System.err.println("Error al desactivar servicio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un servicio después de verificar que no tiene suscripciones.
     * Si tiene suscripciones, retorna false sin eliminar.
     */
    public boolean eliminarSeguro(int idServicio) {
        // Primero verificar que no tenga suscripciones activas
        int suscripciones = contarSuscripcionesPorServicio(idServicio);

        if (suscripciones > 0) {
            System.err.println("❌ No se puede eliminar: El servicio tiene " + suscripciones + " suscripciones activas");
            return false;
        }

        return eliminar(idServicio);
    }
}