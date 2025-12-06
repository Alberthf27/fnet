
package modelo;

import java.math.BigDecimal;

public class Servicio {
    private Long idServicio;
    private String descripcion;
    private BigDecimal mensualidad;
    private BigDecimal anchoBanda;
    private Long canales;
    private Long usuarioIdUsuario;
    private Long proveedorIdProveedor;
    private Long tiposervicioIdTipoServicio;
    private String nombre;

    // Constructores
    public Servicio() {}
    
    public Servicio(String nombre, String descripcion, BigDecimal mensualidad, Long tiposervicioIdTipoServicio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.mensualidad = mensualidad;
        this.tiposervicioIdTipoServicio = tiposervicioIdTipoServicio;
    }

    // Getters y Setters
    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMensualidad() { return mensualidad; }
    public void setMensualidad(BigDecimal mensualidad) { this.mensualidad = mensualidad; }

    public BigDecimal getAnchoBanda() { return anchoBanda; }
    public void setAnchoBanda(BigDecimal anchoBanda) { this.anchoBanda = anchoBanda; }

    public Long getCanales() { return canales; }
    public void setCanales(Long canales) { this.canales = canales; }

    public Long getUsuarioIdUsuario() { return usuarioIdUsuario; }
    public void setUsuarioIdUsuario(Long usuarioIdUsuario) { this.usuarioIdUsuario = usuarioIdUsuario; }

    public Long getProveedorIdProveedor() { return proveedorIdProveedor; }
    public void setProveedorIdProveedor(Long proveedorIdProveedor) { this.proveedorIdProveedor = proveedorIdProveedor; }

    public Long getTiposervicioIdTipoServicio() { return tiposervicioIdTipoServicio; }
    public void setTiposervicioIdTipoServicio(Long tiposervicioIdTipoServicio) { this.tiposervicioIdTipoServicio = tiposervicioIdTipoServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre + " - S/ " + mensualidad + "/mes";
    }
}