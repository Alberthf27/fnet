package DAO;

import bd.Conexion;
import modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> obtenerTodosClientes() {
        List<Cliente> clientes = new ArrayList<>();
        Conexion conexion = new Conexion(); // Crear instancia
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = conexion.conectar(); // Usar método de instancia
            String sql = "SELECT ID_CLIENTE, DNI_CLIENTE, NOMBRES, APELLIDOS, DIRECCION, CORREO, FECHA_REGISTRO, ACTIVO, DEUDA FROM CLIENTE WHERE ACTIVO = 1 ORDER BY APELLIDOS, NOMBRES";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            int count = 0;

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getLong("ID_CLIENTE"));
                cliente.setDniCliente(rs.getString("DNI_CLIENTE"));
                cliente.setNombres(rs.getString("NOMBRES"));
                cliente.setApellidos(rs.getString("APELLIDOS"));
                cliente.setDireccion(rs.getString("DIRECCION"));
                cliente.setCorreo(rs.getString("CORREO"));
                cliente.setFechaRegistro(rs.getDate("FECHA_REGISTRO"));
                cliente.setActivo(rs.getInt("ACTIVO"));
                clientes.add(cliente);
                cliente.setDeuda(rs.getDouble("DEUDA"));
                System.out.println("Cliente " + count + ": " + cliente.getNombres() + " " + cliente.getApellidos());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error en obtenerTodosClientes: " + e.getMessage());
        } finally {
            // Cerrar recursos en orden inverso
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar(); // Usar método de instancia
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return clientes;
    }

    public Long insertarCliente(Cliente cliente) {
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Long generatedId = null;

        try {
            conn = conexion.conectar();
            String sql = "INSERT INTO CLIENTE (DNI_CLIENTE, NOMBRES, APELLIDOS, DIRECCION, CORREO, FECHA_REGISTRO, ACTIVO, DEUDA) "
                    + "VALUES (?, ?, ?, ?, ?, NOW(), 1, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, cliente.getDniCliente());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getDireccion());
            stmt.setString(5, cliente.getCorreo());
            stmt.setDouble(6, cliente.getDeuda() != null ? cliente.getDeuda() : 0.0);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getLong(1);
                    System.out.println("Cliente insertado con ID: " + generatedId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return generatedId;
    }

    public List<Cliente> buscarClientes(String criterio) {
        List<Cliente> clientes = new ArrayList<>();
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = conexion.conectar();
            String sql = "SELECT * FROM CLIENTE WHERE (NOMBRES LIKE ? OR APELLIDOS LIKE ? OR DNI_CLIENTE LIKE ?) AND ACTIVO = 1 ORDER BY APELLIDOS, NOMBRES";
            stmt = conn.prepareStatement(sql);
            String likeCriterio = "%" + criterio + "%";
            stmt.setString(1, likeCriterio);
            stmt.setString(2, likeCriterio);
            stmt.setString(3, likeCriterio);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getLong("ID_CLIENTE"));
                cliente.setDniCliente(rs.getString("DNI_CLIENTE"));
                cliente.setNombres(rs.getString("NOMBRES"));
                cliente.setApellidos(rs.getString("APELLIDOS"));
                cliente.setDireccion(rs.getString("DIRECCION"));
                cliente.setCorreo(rs.getString("CORREO"));
                cliente.setFechaRegistro(rs.getDate("FECHA_REGISTRO"));
                cliente.setActivo(rs.getInt("ACTIVO"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return clientes;
    }

    public boolean actualizarDeuda(Long idCliente, Double nuevaDeuda) {
        if (idCliente == null || nuevaDeuda == null) {
            System.err.println("Error: ID cliente o deuda no pueden ser nulos");
            return false;
        }

        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean resultado = false;

        try {
            conn = conexion.conectar();
            String sql = "UPDATE CLIENTE SET DEUDA = ? WHERE ID_CLIENTE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, nuevaDeuda);
            stmt.setLong(2, idCliente);

            int filasAfectadas = stmt.executeUpdate();
            resultado = filasAfectadas > 0;

            if (resultado) {
                System.out.println("Deuda actualizada correctamente para cliente ID: " + idCliente);
            } else {
                System.out.println("No se encontró el cliente con ID: " + idCliente);
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al actualizar deuda: " + e.getMessage());
            System.err.println("Código error: " + e.getErrorCode());
            e.printStackTrace();
        } finally {
            // Cerrar recursos en orden inverso
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar Statement: " + e.getMessage());
            }
            try {
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (Exception e) {
                System.err.println("Error al cerrar Conexión: " + e.getMessage());
            }
        }
        return resultado;
    }

    public boolean agregarDeuda(Long idCliente, Double montoAAgregar) {
        if (idCliente == null || montoAAgregar == null || montoAAgregar <= 0) {
            System.err.println("Error: Parámetros inválidos");
            return false;
        }

        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean resultado = false;

        try {
            conn = conexion.conectar();
            // Sumar al valor existente de DEUDA
            String sql = "UPDATE CLIENTE SET DEUDA = DEUDA + ? WHERE ID_CLIENTE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montoAAgregar);
            stmt.setLong(2, idCliente);

            int filasAfectadas = stmt.executeUpdate();
            resultado = filasAfectadas > 0;

            if (resultado) {
                System.out.println("Se agregó " + montoAAgregar + " a la deuda del cliente ID: " + idCliente);
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al agregar deuda: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return resultado;
    }

    public boolean eliminarCliente(Long idCliente) {
        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean resultado = false;

        try {
            conn = conexion.conectar();
            String sql = "UPDATE CLIENTE SET ACTIVO = 0 WHERE ID_CLIENTE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idCliente);

            int filasAfectadas = stmt.executeUpdate();
            resultado = filasAfectadas > 0;

            if (resultado) {
                System.out.println("Cliente eliminado (ACTIVO=0) - ID: " + idCliente);
            }

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return resultado;
    }

    public boolean actualizarCliente(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() == null) {
            System.err.println("Error: Cliente o ID no pueden ser nulos");
            return false;
        }

        Conexion conexion = new Conexion();
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean resultado = false;

        try {
            conn = conexion.conectar();
            String sql = "UPDATE CLIENTE SET DNI_CLIENTE = ?, NOMBRES = ?, APELLIDOS = ?, DIRECCION = ?, CORREO = ? WHERE ID_CLIENTE = ?";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, cliente.getDniCliente());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getDireccion());
            stmt.setString(5, cliente.getCorreo());
            stmt.setLong(6, cliente.getIdCliente());

            int filasAfectadas = stmt.executeUpdate();
            resultado = filasAfectadas > 0;

            if (resultado) {
                System.out.println("Cliente actualizado - ID: " + cliente.getIdCliente());
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conexion.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return resultado;
    }
}
