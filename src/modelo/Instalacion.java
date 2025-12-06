package modelo;
import java.sql.Date;

public class Instalacion {
    private Long idInstalacion;
    private Date fecha;
    private String nota;
    private String direccion;
    private String linkUbicacion;
    private Long suscripcionIdSuscripcion;
    private Long empleadoIdEmpleado;
    private Long estinstIdEstado;

    // Getters y Setters
    public Long getIdInstalacion() { return idInstalacion; }
    public void setIdInstalacion(Long idInstalacion) { this.idInstalacion = idInstalacion; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getLinkUbicacion() { return linkUbicacion; }
    public void setLinkUbicacion(String linkUbicacion) { this.linkUbicacion = linkUbicacion; }

    public Long getSuscripcionIdSuscripcion() { return suscripcionIdSuscripcion; }
    public void setSuscripcionIdSuscripcion(Long suscripcionIdSuscripcion) { this.suscripcionIdSuscripcion = suscripcionIdSuscripcion; }

    public Long getEmpleadoIdEmpleado() { return empleadoIdEmpleado; }
    public void setEmpleadoIdEmpleado(Long empleadoIdEmpleado) { this.empleadoIdEmpleado = empleadoIdEmpleado; }

    public Long getEstinstIdEstado() { return estinstIdEstado; }
    public void setEstinstIdEstado(Long estinstIdEstado) { this.estinstIdEstado = estinstIdEstado; }

    @Override
    public String toString() {
        return "Instalación " + idInstalacion + " - " + direccion;
    }
}