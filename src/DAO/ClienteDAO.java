package DAO;

import bd.Conexion;
import modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ----------------------------------------------------------------------------------
    // 🚀 MÉTODOS OPTIMIZADOS PARA VELOCIDAD (Paginación)
    // ----------------------------------------------------------------------------------

    /**
     * Obtiene una página de clientes activos, usando LIMIT y OFFSET para velocidad.
     * @param limit Cantidad máxima de registros a devolver (e.g., 50)
     * @param offset Posición de inicio (página * limit)
     * @return Lista de objetos Cliente
     */
    public List<Cliente> obtenerClientesPaginados(int limit, int offset) {
        List<Cliente> clientes = new ArrayList<>();
        // ✅ Corregido: Uso de minúsculas y guiones bajos en SQL
        String sql = "SELECT id_cliente, dni_cliente, nombres, apellidos, direccion, correo, fecha_registro, activo, deuda "
                    + "FROM cliente "
                    + "WHERE activo = 1 "
                    + "ORDER BY apellidos, nombres "
                    + "LIMIT ? OFFSET ?";
        
        // Uso del try-with-resources para el manejo automático de la conexión y recursos (Mejor práctica en Java)
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en obtenerClientesPaginados: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }
    
    /**
     * Obtiene el número total de clientes activos.
     * Es necesario para calcular el número total de páginas.
     */
    public int obtenerTotalClientesActivos() {
        String sql = "SELECT COUNT(id_cliente) FROM cliente WHERE activo = 1";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener total de clientes: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * ✅ MEJORADO: Búsqueda de clientes (Activo=1)
     * Utiliza los índices creados (dni_cliente, apellidos, nombres)
     */
    public List<Cliente> buscarClientes(String criterio) {
        List<Cliente> clientes = new ArrayList<>();
        // ✅ Corregido: Uso de minúsculas y guiones bajos en SQL
        String sql = "SELECT * FROM cliente WHERE (nombres LIKE ? OR apellidos LIKE ? OR dni_cliente LIKE ?) AND activo = 1 ORDER BY apellidos, nombres";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String likeCriterio = "%" + criterio + "%";
            stmt.setString(1, likeCriterio);
            stmt.setString(2, likeCriterio);
            stmt.setString(3, likeCriterio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar clientes: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }


    // ----------------------------------------------------------------------------------
    // ⚙️ MÉTODOS CRUD (Corregidos y con Try-with-resources)
    // ----------------------------------------------------------------------------------

    public Long insertarCliente(Cliente cliente) {
        Long generatedId = null;
        // ✅ Corregido: Uso de minúsculas y la función NOW()
        String sql = "INSERT INTO cliente (dni_cliente, nombres, apellidos, direccion, correo, fecha_registro, activo, deuda) "
                   + "VALUES (?, ?, ?, ?, ?, NOW(), 1, ?)";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cliente.getDniCliente());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getDireccion());
            stmt.setString(5, cliente.getCorreo());
            stmt.setDouble(6, cliente.getDeuda() != null ? cliente.getDeuda() : 0.0);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getLong(1);
                        System.out.println("Cliente insertado con ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al insertar cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId;
    }
    
    public boolean actualizarCliente(Cliente cliente) {
        // ✅ Corregido: Uso de minúsculas y guiones bajos en SQL
        String sql = "UPDATE cliente SET dni_cliente = ?, nombres = ?, apellidos = ?, direccion = ?, correo = ? WHERE id_cliente = ?";
        boolean resultado = false;

        if (cliente == null || cliente.getIdCliente() == null) { return false; }

        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getDniCliente());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getDireccion());
            stmt.setString(5, cliente.getCorreo());
            stmt.setLong(6, cliente.getIdCliente());

            resultado = stmt.executeUpdate() > 0;
            if (resultado) {
                System.out.println("Cliente actualizado - ID: " + cliente.getIdCliente());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return resultado;
    }

    public boolean eliminarCliente(Long idCliente) {
        // En tu BD, eliminar es poner ACTIVO = 0
        String sql = "UPDATE cliente SET activo = 0 WHERE id_cliente = ?";
        boolean resultado = false;

        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            resultado = stmt.executeUpdate() > 0;
            
            if (resultado) {
                System.out.println("Cliente eliminado (ACTIVO=0) - ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar (desactivar) cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return resultado;
    }

    public boolean actualizarDeuda(Long idCliente, Double nuevaDeuda) {
        String sql = "UPDATE cliente SET deuda = ? WHERE id_cliente = ?";
        boolean resultado = false;

        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, nuevaDeuda);
            stmt.setLong(2, idCliente);
            resultado = stmt.executeUpdate() > 0;
            
            if (resultado) {
                System.out.println("Deuda actualizada correctamente para cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error SQL al actualizar deuda: " + e.getMessage());
            e.printStackTrace();
        }
        return resultado;
    }
    
    public boolean agregarDeuda(Long idCliente, Double montoAAgregar) {
        String sql = "UPDATE cliente SET deuda = deuda + ? WHERE id_cliente = ?";
        boolean resultado = false;

        if (montoAAgregar == null || montoAAgregar <= 0) { return false; }
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, montoAAgregar);
            stmt.setLong(2, idCliente);
            resultado = stmt.executeUpdate() > 0;

            if (resultado) {
                System.out.println("Se agregó " + montoAAgregar + " a la deuda del cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error SQL al agregar deuda: " + e.getMessage());
            e.printStackTrace();
        }
        return resultado;
    }

    // ----------------------------------------------------------------------------------
    // 🧰 HELPER (Ayudante)
    // ----------------------------------------------------------------------------------

    /** Mapea un ResultSet a un objeto Cliente (Evita repetir código) */
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        // ✅ Uso de nombres de columna exactos: id_cliente, dni_cliente, fecha_registro, activo, deuda
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setDniCliente(rs.getString("dni_cliente"));
        cliente.setNombres(rs.getString("nombres"));
        cliente.setApellidos(rs.getString("apellidos"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setFechaRegistro(rs.getTimestamp("fecha_registro")); // Es datetime, mejor usar Timestamp o Date
        cliente.setActivo(rs.getInt("activo"));
        cliente.setDeuda(rs.getDouble("deuda"));
        return cliente;
    }
    
    /**
     * NOTA: Este método original ha sido reemplazado por obtenerClientesPaginados()
     * para mejorar el rendimiento. Se mantiene comentado si necesitas el patrón antiguo.
     */
    /*
    public List<Cliente> obtenerTodosClientes() {
         return obtenerClientesPaginados(1000000, 0); // Limitarlo a un número gigante, pero forzar el LIMIT/OFFSET
    }
    */
}