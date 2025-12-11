package modelo;

import java.sql.Date;

public class Suscripcion {
    private int idSuscripcion;
    private int idCliente;
    private int idServicio;
    private String codigoContrato;
    private Date fechaInicio;
    private Date fechaFin;
    private String direccionInstalacion;
    private int activo; // 1 = Activo, 0 = Corte
    
    // --- CAMPOS EXTRA (Para mostrar en tabla sin hacer más consultas) ---
    private String nombreCliente;
    private String nombreServicio;
    private Double precioServicio;

    public Suscripcion() {}

    // Getters y Setters normales
    public int getIdSuscripcion() { return idSuscripcion; }
    public void setIdSuscripcion(int idSuscripcion) { this.idSuscripcion = idSuscripcion; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getCodigoContrato() { return codigoContrato; }
    public void setCodigoContrato(String codigoContrato) { this.codigoContrato = codigoContrato; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getDireccionInstalacion() { return direccionInstalacion; }
    public void setDireccionInstalacion(String direccionInstalacion) { this.direccionInstalacion = direccionInstalacion; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }

    // Getters y Setters EXTRAS
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    
    public Double getPrecioServicio() { return precioServicio; }
    public void setPrecioServicio(Double precio) { this.precioServicio = precio; }
    
    /**
     * Actualiza el plan y la dirección de un contrato existente.
     */
    public boolean actualizarContrato(int idSuscripcion, int idNuevoServicio, String nuevaDireccion) {
        String sql = "UPDATE suscripcion SET id_servicio = ?, direccion_instalacion = ? WHERE id_suscripcion = ?";
        
        try (java.sql.Connection conn = bd.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idNuevoServicio);
            ps.setString(2, nuevaDireccion);
            ps.setInt(3, idSuscripcion);
            
            return ps.executeUpdate() > 0;
            
        } catch (java.sql.SQLException e) {
            System.err.println("Error actualizando contrato: " + e.getMessage());
            return false;
        }
    }
}