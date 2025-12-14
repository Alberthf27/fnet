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
        String sql = "SELECT s.id_suscripcion, s.codigo_contrato, s.direccion_instalacion, s.fecha_inicio, s.activo, "
                + "c.nombres, c.apellidos, sv.descripcion, sv.mensualidad "
                + "FROM suscripcion s "
                + "INNER JOIN cliente c ON s.id_cliente = c.id_cliente "
                + "INNER JOIN servicio sv ON s.id_servicio = sv.id_servicio "
                + "WHERE s.activo > 0 "
                + // Ocultar los dados de baja (0)
                "ORDER BY s.id_suscripcion DESC "
                + "LIMIT ? OFFSET ?";

        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    sus.setMontoMensual(rs.getDouble("mensualidad"));
                    lista.add(sus);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    /**
     * Guarda un contrato NUEVO o ACTUALIZA uno existente con todos los campos.
     * Soporta cambio de titular, fecha inicio y día de pago.
     */
    public boolean guardarOActualizarContrato(int idSuscripcion, int idServicio, String direccion, int idCliente, java.util.Date fechaInicio, int diaPago) {
        String sql;
        boolean esNuevo = (idSuscripcion == -1); // Si es -1, insertamos

        if (esNuevo) {
            // INSERTAR NUEVO
            sql = "INSERT INTO suscripcion (id_servicio, direccion_instalacion, id_cliente, fecha_inicio, dia_pago, codigo_contrato, activo) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        } else {
            // ACTUALIZAR EXISTENTE
            sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ?, id_cliente = ?, fecha_inicio = ?, dia_pago = ? "
                    + "WHERE id_suscripcion = ?";
        }

        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            ps.setString(2, direccion);
            ps.setInt(3, idCliente);

            // Convertir java.util.Date a java.sql.Date
            java.sql.Date sqlFecha = new java.sql.Date(fechaInicio.getTime());
            ps.setDate(4, sqlFecha);

            ps.setInt(5, diaPago);

            if (esNuevo) {
                // Generar código simple para nuevo contrato
                String codigo = "CNT-" + System.currentTimeMillis();
                ps.setString(6, codigo);
            } else {
                // Si es update, el último parámetro es el ID del WHERE
                ps.setInt(6, idSuscripcion);
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error al guardar/actualizar contrato: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarContrato(int idSuscripcion, int idNuevoServicio, String nuevaDireccion) {
        String sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ? WHERE id_suscripcion = ?";

        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

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
        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
     *
     * @param idCliente ID del cliente (Long para compatibilidad)
     * @param idServicio ID del servicio/plan (Long)
     */
    public boolean crearSuscripcionPorDefecto(Long idCliente, Long idServicio) {
        String sql = "INSERT INTO suscripcion (id_cliente, id_servicio, codigo_contrato, fecha_inicio, direccion_instalacion, activo) "
                + "VALUES (?, ?, ?, NOW(), ?, 1)";

        // Obtenemos la dirección del cliente para ponerla en la instalación por defecto
        String direccion = "Dirección Principal"; // Valor por defecto

        // (Opcional) Consultar dirección real del cliente
        // String sqlDir = "SELECT direccion FROM cliente WHERE id_cliente = ?"; ...
        // Generar un código de contrato simple
        String codigo = "CNT-" + System.currentTimeMillis();

        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Retorna un arreglo: [0]=ID, [1]=NombreCompleto. Retorna null si no
     * existe.
     */
    public String[] buscarClientePorDni(String dni) {
        String sql = "SELECT id_cliente, nombres, apellidos FROM cliente WHERE dni_cliente = ? AND activo = 1";
        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dni);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("id_cliente")),
                        rs.getString("nombres") + " " + rs.getString("apellidos")
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ACTUALIZACIÓN MAESTRA: Plan, Dirección y Titular.
     */
    public boolean actualizarContratoCompleto(int idSuscripcion, int idServicio, String direccion, int idNuevoCliente) {
        // Actualizamos todo de una vez
        String sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ?, id_cliente = ? WHERE id_suscripcion = ?";

        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

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

    public List<Suscripcion> listarTodo(String busqueda, String orden) {
        List<Suscripcion> lista = new ArrayList<>();

        // SQL Base
        String sql = "SELECT s.id_suscripcion, s.codigo_contrato, s.direccion_instalacion, s.fecha_inicio, s.activo, s.sector, s.dia_pago, "
                + "c.nombres, c.apellidos, sv.descripcion, sv.mensualidad, "
                + "(SELECT COUNT(*) FROM factura f WHERE f.id_suscripcion = s.id_suscripcion AND f.id_estado = 1) as f_pend "
                + "FROM suscripcion s "
                + "INNER JOIN cliente c ON s.id_cliente = c.id_cliente "
                + "INNER JOIN servicio sv ON s.id_servicio = sv.id_servicio "
                + "WHERE s.activo > 0 "
                + "AND (c.nombres LIKE ? OR c.apellidos LIKE ? OR s.codigo_contrato LIKE ?) ";

        // LÓGICA DE ORDENAMIENTO CORREGIDA
        if (orden != null) {
            switch (orden) {
                case "DIA DE PAGO": // <--- NUEVO
                    sql += " ORDER BY s.dia_pago ASC";
                    break;

                case "MÁS RECIENTES":

                    sql += " ORDER BY s.fecha_inicio DESC";
                    break;
                case "MÁS ANTIGUOS":
                    sql += " ORDER BY s.fecha_inicio ASC";
                    break;
                case "NOMBRE (A-Z)":
                    // CORRECCIÓN: Ordenar por Nombre primero, luego Apellido.
                    // Esto arregla el problema visual cuando el apellido es NULL.
                    sql += " ORDER BY c.nombres ASC, c.apellidos ASC";
                    break;
                case "DEUDORES":
                    sql += " ORDER BY f_pend DESC, s.id_suscripcion DESC";
                    break;
                default:
                    sql += " ORDER BY s.id_suscripcion DESC";
                    break;
            }
        } else {
            sql += " ORDER BY s.id_suscripcion DESC";
        }

        // Obtener conexión con protección
        java.sql.Connection conn = bd.Conexion.getConexion();
        if (conn == null) {
            return lista;
        }

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + busqueda + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Suscripcion sus = new Suscripcion();
                    sus.setIdSuscripcion(rs.getInt("id_suscripcion"));
                    sus.setCodigoContrato(rs.getString("codigo_contrato"));
                    sus.setDireccionInstalacion(rs.getString("direccion_instalacion"));
                    sus.setFechaInicio(rs.getDate("fecha_inicio"));
                    sus.setActivo(rs.getInt("activo"));
                    sus.setSector(rs.getString("sector"));
                    sus.setDiaPago(rs.getInt("dia_pago"));

                    String nom = rs.getString("nombres");
                    String ape = rs.getString("apellidos");
                    String nombreCompleto = ((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim();
                    sus.setNombreCliente(nombreCompleto.toUpperCase());

                    sus.setNombreServicio(rs.getString("descripcion").toUpperCase());
                    sus.setMontoMensual(rs.getDouble("mensualidad"));

                    int pendientes = rs.getInt("f_pend");
                    sus.setFacturasPendientes(pendientes);

                    // Historial visual (Izquierda a Derecha)
                    StringBuilder sb = new StringBuilder();
                    for (int i = 5; i >= 0; i--) {
                        if (i < pendientes) {
                            sb.append("0");
                        } else {
                            sb.append("1");
                        }
                    }
                    sus.setHistorialPagos(sb.toString());

                    lista.add(sus);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
        return lista;
    }
    
    // Método para determinar a qué MES corresponde el inicio del cobro
    public String calcularMesFacturacion(java.util.Date fechaInicio) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fechaInicio);
        
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
        
        // LÓGICA DEL NEGOCIO: 
        // Si entra después del 16, el mes de servicio cuenta desde el SIGUIENTE.
        if (dia > 16) {
            cal.add(java.util.Calendar.MONTH, 1); // Sumar 1 mes
        }
        
        // Formatear mes (Ej: "JULIO 2025")
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", new java.util.Locale("es", "ES"));
        return sdf.format(cal.getTime()).toUpperCase();
    }

    public int obtenerIdClienteDeContrato(int idSuscripcion) {
        String sql = "SELECT id_cliente FROM suscripcion WHERE id_suscripcion = ?";
        try (java.sql.Connection conn = bd.Conexion.getConexion(); java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSuscripcion);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

}
