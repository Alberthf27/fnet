package DAO;

import bd.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import modelo.CategoriaMovimiento;

public class FinanzasDAO {

    /**
     * MÉTODO OPTIMIZADO: Obtiene TODOS los datos del Dashboard en UNA SOLA
     * conexión.
     * Retorna: Object[] con {ingresosHoy, deudaTotal, clientesActivos,
     * clientesCortados,
     * nuevosDelMes, bajasDelMes, historialMRR, ultimosPagos}
     */
    public Object[] obtenerDashboardData() {
        Object[] resultado = new Object[8];

        try (Connection conn = Conexion.getConexion()) {
            // 1. Ingresos de Hoy (Filtrado desde las 11:00 AM)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(monto), 0) FROM movimiento_caja WHERE fecha >= '2025-12-30 11:00:00' AND monto > 0");
                    ResultSet rs = ps.executeQuery()) {
                resultado[0] = rs.next() ? rs.getDouble(1) : 0.0;
            }

            // 2. Deuda Total
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(deuda), 0) FROM cliente WHERE activo = 1");
                    ResultSet rs = ps.executeQuery()) {
                resultado[1] = rs.next() ? rs.getDouble(1) : 0.0;
            }

            // 3. Clientes Activos
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM suscripcion WHERE activo = 1");
                    ResultSet rs = ps.executeQuery()) {
                resultado[2] = rs.next() ? rs.getInt(1) : 0;
            }

            // 4. Clientes Cortados
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM suscripcion WHERE activo = 0 AND fecha_cancelacion IS NULL");
                    ResultSet rs = ps.executeQuery()) {
                resultado[3] = rs.next() ? rs.getInt(1) : 0;
            }

            // 5. Nuevos del Mes
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM suscripcion WHERE MONTH(fecha_inicio) = MONTH(CURRENT_DATE()) AND YEAR(fecha_inicio) = YEAR(CURRENT_DATE())");
                    ResultSet rs = ps.executeQuery()) {
                resultado[4] = rs.next() ? rs.getInt(1) : 0;
            }

            // 6. Bajas del Mes
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM suscripcion WHERE activo = 0 AND fecha_cancelacion IS NOT NULL AND MONTH(fecha_cancelacion) = MONTH(CURRENT_DATE())");
                    ResultSet rs = ps.executeQuery()) {
                resultado[5] = rs.next() ? rs.getInt(1) : 0;
            }

            // 7. Historial MRR (últimos 6 meses)
            List<Object[]> historial = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DATE_FORMAT(fecha_inicio, '%Y-%m') as mes, SUM(s.mensualidad) " +
                            "FROM suscripcion sus JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                            "WHERE sus.activo = 1 GROUP BY mes ORDER BY mes ASC LIMIT 6");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(new Object[] { rs.getString(1), rs.getDouble(2) });
                }
            }
            resultado[6] = historial;

            // 8. Últimos Pagos (Filtrado desde las 11:00 AM)
            List<Object[]> pagos = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT mc.id_movimiento, DATE_FORMAT(mc.fecha, '%d/%m %H:%i'), " +
                            "COALESCE(mc.descripcion, 'Pago'), mc.monto, COALESCE(cm.nombre, 'Efectivo') " +
                            "FROM movimiento_caja mc " +
                            "LEFT JOIN categoria_movimiento cm ON mc.id_categoria = cm.id_categoria " +
                            "WHERE mc.monto > 0 AND mc.fecha >= '2025-12-30 11:00:00' " +
                            "ORDER BY mc.fecha DESC LIMIT 10");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(new Object[] {
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getDouble(4), rs.getString(5)
                    });
                }
            }
            resultado[7] = pagos;

        } catch (Exception e) {
            System.err.println("Error en obtenerDashboardData: " + e.getMessage());
            e.printStackTrace();
            // Valores por defecto en caso de error
            resultado[0] = 0.0;
            resultado[1] = 0.0;
            resultado[2] = 0;
            resultado[3] = 0;
            resultado[4] = 0;
            resultado[5] = 0;
            resultado[6] = new ArrayList<>();
            resultado[7] = new ArrayList<>();
        }

        return resultado;
    }

    /**
     * MÉTODO OPTIMIZADO para PanelFinanzas: Obtiene TODOS los datos financieros en
     * UNA SOLA conexión.
     * Retorna: Object[] con {mrr, balanceMes, deudaTotal, historialMRR,
     * distribucionPlanes,
     * altasBajas, flujo7Dias}
     */
    public Object[] obtenerFinanzasData() {
        Object[] resultado = new Object[7];

        try (Connection conn = Conexion.getConexion()) {
            // 1. MRR (Monthly Recurring Revenue)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(s.mensualidad), 0) FROM suscripcion sus " +
                            "JOIN servicio s ON sus.id_servicio = s.id_servicio WHERE sus.activo = 1");
                    ResultSet rs = ps.executeQuery()) {
                resultado[0] = rs.next() ? rs.getDouble(1) : 0.0;
            }

            // 2. Balance del Mes (Filtrado desde las 11:00 AM hoy)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(monto), 0) FROM movimiento_caja " +
                            "WHERE fecha >= '2025-12-30 11:00:00'");
                    ResultSet rs = ps.executeQuery()) {
                resultado[1] = rs.next() ? rs.getDouble(1) : 0.0;
            }

            // 3. Deuda Total
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(deuda), 0) FROM cliente WHERE activo = 1");
                    ResultSet rs = ps.executeQuery()) {
                resultado[2] = rs.next() ? rs.getDouble(1) : 0.0;
            }

            // 4. Historial MRR
            List<Object[]> historial = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DATE_FORMAT(fecha_inicio, '%Y-%m') as mes, SUM(s.mensualidad) " +
                            "FROM suscripcion sus JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                            "WHERE sus.activo = 1 GROUP BY mes ORDER BY mes ASC LIMIT 6");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(new Object[] { rs.getString(1), rs.getDouble(2) });
                }
            }
            resultado[3] = historial;

            // 5. Distribución de Planes
            List<Object[]> distribucion = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT s.descripcion, COUNT(*) as cantidad FROM suscripcion sus " +
                            "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                            "WHERE sus.activo = 1 GROUP BY s.descripcion");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    distribucion.add(new Object[] { rs.getString(1), rs.getDouble(2) });
                }
            }
            resultado[4] = distribucion;

            // 6. Altas vs Bajas (simplificado - datos estáticos por ahora)
            List<double[]> altasBajas = new ArrayList<>();
            altasBajas.add(new double[] { 5, 0 });
            altasBajas.add(new double[] { 8, 1 });
            altasBajas.add(new double[] { 4, 2 });
            altasBajas.add(new double[] { 6, 0 });
            resultado[5] = altasBajas;

            // 7. Flujo últimos 7 días (Filtrado desde hoy 11 AM)
            List<double[]> flujo = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DATE(fecha) as dia, " +
                            "SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END) as ingresos, " +
                            "SUM(CASE WHEN monto < 0 THEN ABS(monto) ELSE 0 END) as egresos " +
                            "FROM movimiento_caja WHERE fecha >= '2025-12-30 11:00:00' " +
                            "GROUP BY DATE(fecha) ORDER BY dia ASC");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flujo.add(new double[] { rs.getDouble("ingresos"), rs.getDouble("egresos") });
                }
            }
            resultado[6] = flujo;

        } catch (Exception e) {
            System.err.println("Error en obtenerFinanzasData: " + e.getMessage());
            e.printStackTrace();
            resultado[0] = 0.0;
            resultado[1] = 0.0;
            resultado[2] = 0.0;
            resultado[3] = new ArrayList<>();
            resultado[4] = new ArrayList<>();
            resultado[5] = new ArrayList<>();
            resultado[6] = new ArrayList<>();
        }

        return resultado;
    }

    // --- SECCIÓN KPI (Tarjetas Superiores) ---
    public double calcularMRR() {
        String sql = "SELECT SUM(s.mensualidad) FROM suscripcion sus "
                + "JOIN servicio s ON sus.id_servicio = s.id_servicio "
                + "WHERE sus.activo = 1";
        return ejecutarScalarDouble(sql);
    }

    public double obtenerBalanceMes() {
        String sql = "SELECT SUM(monto) FROM movimiento_caja "
                + "WHERE MONTH(fecha) = MONTH(CURRENT_DATE()) AND YEAR(fecha) = YEAR(CURRENT_DATE())";
        return ejecutarScalarDouble(sql);
    }

    public double calcularDeudaTotal() {
        // Asumiendo que existe columna 'deuda' en cliente, o sumando facturas
        // pendientes
        String sql = "SELECT SUM(deuda) FROM cliente WHERE activo = 1";
        return ejecutarScalarDouble(sql);
    }

    // --- SECCIÓN GRÁFICOS Y TABLAS ---
    public List<double[]> obtenerFlujoUltimos7Dias() {
        List<double[]> datos = new ArrayList<>();
        String sql = "SELECT DATE(fecha) as dia, "
                + "SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END) as ingresos, "
                + "SUM(CASE WHEN monto < 0 THEN ABS(monto) ELSE 0 END) as egresos "
                + "FROM movimiento_caja "
                + "WHERE fecha >= DATE_SUB(NOW(), INTERVAL 7 DAY) "
                + "GROUP BY DATE(fecha) ORDER BY dia ASC";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                datos.add(new double[] { rs.getDouble("ingresos"), rs.getDouble("egresos") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datos;
    }

    public List<Object[]> obtenerHistorialMRR() {
        List<Object[]> datos = new ArrayList<>();
        // Optimización: Usamos DATE_FORMAT para agrupar por mes 'YYYY-MM'
        String sql = "SELECT DATE_FORMAT(fecha_inicio, '%Y-%m') as mes, SUM(s.mensualidad) "
                + "FROM suscripcion sus "
                + "JOIN servicio s ON sus.id_servicio = s.id_servicio "
                + "WHERE sus.activo = 1 "
                + "GROUP BY mes ORDER BY mes ASC LIMIT 6";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                datos.add(new Object[] { rs.getString(1), rs.getDouble(2) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datos;
    }

    public List<Object[]> obtenerDistribucionPlanes() {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT s.descripcion, COUNT(*) as cantidad "
                + "FROM suscripcion sus "
                + "JOIN servicio s ON sus.id_servicio = s.id_servicio "
                + "WHERE sus.activo = 1 "
                + "GROUP BY s.descripcion";

        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                datos.add(new Object[] { rs.getString(1), rs.getDouble(2) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datos;
    }

    // Método corregido para datos reales (Simulado por ahora si no tienes tabla de
    // histórico de estados)
    public List<double[]> obtenerAltasBajasUltimosMeses() {
        List<double[]> datos = new ArrayList<>();
        // Simulación lógica para evitar errores SQL si faltan tablas de auditoría
        datos.add(new double[] { 5, 0 });
        datos.add(new double[] { 8, 1 });
        datos.add(new double[] { 4, 2 });
        datos.add(new double[] { 6, 0 });
        return datos;
    }

    // --- NUEVO MÉTODO QUE FALTABA PARA panel_Gerente ---
    // Retorna: [Nuevos (Mes), Bajas (Mes), Cortes (Totales o Mes)]
    public int[] obtenerResumenClientesMes() {
        int[] resultados = new int[3];
        // 1. Nuevos
        String sqlNuevos = "SELECT COUNT(*) FROM cliente WHERE MONTH(fecha_registro) = MONTH(CURRENT_DATE())";
        // 2. Bajas (Inactivos este mes) - Asumiendo lógica simple
        String sqlBajas = "SELECT COUNT(*) FROM cliente WHERE activo = 0";
        // 3. Cortes (Suspendidos) - Asumiendo estado 'SUSPENDIDO' o similar
        String sqlCortes = "SELECT COUNT(*) FROM suscripcion WHERE estado = 'SUSPENDIDO'";

        try (Connection conn = Conexion.getConexion()) {
            // Ejecutamos las 3 en una sola conexión para ser eficientes
            try (PreparedStatement ps = conn.prepareStatement(sqlNuevos); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultados[0] = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlBajas); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultados[1] = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlCortes); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultados[2] = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultados;
    }

    // 1. Cuenta cuántos clientes tienen fecha de inicio en el MES ACTUAL
    public int contarNuevosClientesMes() {
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE MONTH(fecha_inicio) = MONTH(CURRENT_DATE()) AND YEAR(fecha_inicio) = YEAR(CURRENT_DATE())";
        try (java.sql.Connection conn = bd.Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2. Cuenta cuántos se dieron de baja (activo=0 y con fecha fin) en el MES
    // ACTUAL
    public int contarBajasMes() {
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE activo = 0 AND fecha_cancelacion IS NOT NULL AND MONTH(fecha_cancelacion) = MONTH(CURRENT_DATE()) AND YEAR(fecha_cancelacion) = YEAR(CURRENT_DATE())";
        try (java.sql.Connection conn = bd.Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 3. Contar cortes (suspendidos) ocurridos recientemente
    // Nota: Esto cuenta cuántos están en estado '0' (suspendido) pero SIN fecha de
    // cancelación definitiva
    public int contarCortesMes() {
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE activo = 0 AND fecha_cancelacion IS NULL";
        try (java.sql.Connection conn = bd.Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- MÉTODOS AUXILIARES ---
    private double ejecutarScalarDouble(String sql) {
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("Error en consulta: " + sql);
            e.printStackTrace();
        }
        return 0.0;
    }

    // --- MÉTODO RECUPERADO PARA DIALOGO MOVIMIENTO ---
    public List<CategoriaMovimiento> listarCategorias(String tipoFiltro) {
        List<CategoriaMovimiento> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre, tipo FROM categoria_movimiento WHERE tipo = ?";

        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipoFiltro);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Asegúrate de que el constructor de CategoriaMovimiento coincida
                lista.add(new CategoriaMovimiento(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre"),
                        rs.getString("tipo")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 1. KPI: Ingresos de HOY (Suma de movimientos de tipo INGRESO del día)
    public double obtenerIngresosHoy() {
        // Asumiendo que 'monto > 0' son ingresos. Ajusta si usas una columna 'tipo'.
        String sql = "SELECT SUM(monto) FROM movimiento_caja WHERE DATE(fecha) = CURRENT_DATE() AND monto > 0";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // 2. KPI: Clientes Activos (Total de suscripciones activas)
    public int contarClientesActivos() {
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE activo = 1";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int contarClientesCortados() {
        // IMPORTANTE: "fecha_cancelacion IS NULL" diferencia a los cortados de las
        // bajas definitivas
        String sql = "SELECT COUNT(*) FROM suscripcion WHERE activo = 0 AND fecha_cancelacion IS NULL";

        try (java.sql.Connection conn = bd.Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean registrarMovimiento(double monto, String descripcion, int idCategoria, int idUsuario) {
        String sql = "INSERT INTO movimiento_caja (fecha, monto, descripcion, id_categoria, id_usuario) VALUES (NOW(), ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, monto);
            ps.setString(2, descripcion);
            ps.setInt(3, idCategoria);
            ps.setInt(4, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
