package modelo;

import java.sql.Date;

public class Suscripcion {
    private Long idSuscripcion;
    private Date fechaInicio;
    private Date fechaFin;
    private Long servicioIdServicio;
    private Long instalacionIdInstalacion;
    private Long clienteIdCliente;
    private Long estsuscIdEstado;

    // Constructores
    public Suscripcion() {}
    
    public Suscripcion(Date fechaInicio, Long servicioIdServicio, Long clienteIdCliente) {
        this.fechaInicio = fechaInicio;
        this.servicioIdServicio = servicioIdServicio;
        this.clienteIdCliente = clienteIdCliente;
    }

    // Getters y Setters
    public Long getIdSuscripcion() { return idSuscripcion; }
    public void setIdSuscripcion(Long idSuscripcion) { this.idSuscripcion = idSuscripcion; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public Long getServicioIdServicio() { return servicioIdServicio; }
    public void setServicioIdServicio(Long servicioIdServicio) { this.servicioIdServicio = servicioIdServicio; }

    public Long getInstalacionIdInstalacion() { return instalacionIdInstalacion; }
    public void setInstalacionIdInstalacion(Long instalacionIdInstalacion) { this.instalacionIdInstalacion = instalacionIdInstalacion; }

    public Long getClienteIdCliente() { return clienteIdCliente; }
    public void setClienteIdCliente(Long clienteIdCliente) { this.clienteIdCliente = clienteIdCliente; }

    public Long getEstsuscIdEstado() { return estsuscIdEstado; }
    public void setEstsuscIdEstado(Long estsuscIdEstado) { this.estsuscIdEstado = estsuscIdEstado; }

    @Override
    public String toString() {
        return "Suscripción " + idSuscripcion + " - Cliente: " + clienteIdCliente;
    }
}