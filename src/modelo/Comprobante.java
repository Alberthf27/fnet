package modelo;

import java.sql.Date;

public class Comprobante {
    private Long idComprobante;
    private Date fechaGeneracion;
    private String ruta;
    private Long pagoIdPago;

    // Getters y Setters
    public Long getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Long idComprobante) { this.idComprobante = idComprobante; }

    public Date getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(Date fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public Long getPagoIdPago() { return pagoIdPago; }
    public void setPagoIdPago(Long pagoIdPago) { this.pagoIdPago = pagoIdPago; }

    @Override
    public String toString() {
        return "Comprobante " + idComprobante + " - " + fechaGeneracion;
    }
}