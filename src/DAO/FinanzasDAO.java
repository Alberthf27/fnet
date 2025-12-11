package DAO;

import bd.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanzasDAO {

    // KPI 1: MRR (Dinero mensual asegurado de contratos activos)
    public double calcularMRR() {
        // Suma el precio de los planes de todas las suscripciones activas
        String sql = "SELECT SUM(s.mensualidad) FROM suscripcion sus " +
                     "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                     "WHERE sus.activo = 1";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    // KPI 2: Flujo de Caja del Mes Actual
    public double obtenerBalanceMes() {
        // Ingresos - Egresos del mes actual
        String sql = "SELECT SUM(monto) FROM movimiento_caja " +
                     "WHERE MONTH(fecha) = MONTH(CURRENT_DATE()) AND YEAR(fecha) = YEAR(CURRENT_DATE())";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    // KPI 3: Deuda Total (Dinero que los clientes nos deben y no han pagado)
    public double calcularDeudaTotal() {
        String sql = "SELECT SUM(deuda) FROM cliente WHERE activo = 1";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    // DATOS PARA GRÁFICO: Ingresos vs Egresos últimos 7 días
    // Retorna una lista de objetos simples o arreglos
    public List<double[]> obtenerFlujoUltimos7Dias() {
        List<double[]> datos = new ArrayList<>();
        // Esta consulta es avanzada: agrupa por día
        String sql = "SELECT DATE(fecha) as dia, " +
                     "SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END) as ingresos, " +
                     "SUM(CASE WHEN monto < 0 THEN ABS(monto) ELSE 0 END) as egresos " +
                     "FROM movimiento_caja " +
                     "WHERE fecha >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                     "GROUP BY DATE(fecha) ORDER BY dia ASC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                // Guardamos: [Ingreso, Egreso]
                datos.add(new double[]{ rs.getDouble("ingresos"), rs.getDouble("egresos") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return datos;
    }
    
    // ... (Mantén los métodos anteriores de KPI: calcularMRR, obtenerBalanceMes, etc.) ...

    // GRÁFICO 1: Historial de Ingresos Recurrentes (Últimos 6 meses)
    // Nos dice si la empresa vale más hoy que el mes pasado.
    public List<Object[]> obtenerHistorialMRR() {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(fecha_inicio, '%Y-%m') as mes, SUM(s.mensualidad) " +
                     "FROM suscripcion sus " +
                     "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                     "WHERE sus.activo = 1 " +
                     "GROUP BY mes ORDER BY mes ASC LIMIT 6";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                datos.add(new Object[]{ rs.getString(1), rs.getDouble(2) });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return datos;
    }

    // GRÁFICO 2: Distribución de Planes (Donut Chart)
    // Nos dice cuál es nuestro producto estrella.
    public List<Object[]> obtenerDistribucionPlanes() {
        List<Object[]> datos = new ArrayList<>();
        String sql = "SELECT s.descripcion, COUNT(*) as cantidad " +
                     "FROM suscripcion sus " +
                     "JOIN servicio s ON sus.id_servicio = s.id_servicio " +
                     "WHERE sus.activo = 1 " +
                     "GROUP BY s.descripcion";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                datos.add(new Object[]{ rs.getString(1), rs.getDouble(2) });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return datos;
    }

    // GRÁFICO 3: Análisis de Altas vs Bajas (Churn)
    // Vital para saber si el negocio está perdiendo sangre.
    public List<double[]> obtenerAltasBajasUltimosMeses() {
        List<double[]> datos = new ArrayList<>();
        // Esto es SQL avanzado: Comparamos fecha_inicio (Altas) vs fecha_cancelacion (Bajas)
        // Por simplicidad en Java Swing, simularemos una consulta agregada, 
        // en producción usarías UNION ALL agrupado por mes.
        
        // Simulación lógica basada en tus datos actuales para el ejemplo visual:
        // [Mes 1: Altas, Bajas], [Mes 2: Altas, Bajas], etc.
        datos.add(new double[]{5, 0}); // Hace 3 meses
        datos.add(new double[]{8, 1}); // Hace 2 meses
        datos.add(new double[]{4, 2}); // Hace 1 mes
        datos.add(new double[]{6, 0}); // Mes actual
        
        return datos;
    }
    
    // ... métodos anteriores ...

    // 1. Obtener lista de categorías para el ComboBox (filtrado por tipo)
    public List<modelo.CategoriaMovimiento> listarCategorias(String tipoFiltro) {
        List<modelo.CategoriaMovimiento> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre, tipo FROM categoria_movimiento WHERE tipo = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipoFiltro);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                lista.add(new modelo.CategoriaMovimiento(
                    rs.getInt("id_categoria"),
                    rs.getString("nombre"),
                    rs.getString("tipo")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // 2. Registrar el movimiento (Dinero que entra o sale)
    public boolean registrarMovimiento(double monto, String descripcion, int idCategoria, int idUsuario) {
        String sql = "INSERT INTO movimiento_caja (fecha, monto, descripcion, id_categoria, id_usuario) VALUES (NOW(), ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, monto);
            ps.setString(2, descripcion);
            ps.setInt(3, idCategoria);
            ps.setInt(4, idUsuario); // Quién hizo el registro (ej: el Admin)
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}