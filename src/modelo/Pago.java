
package modelo;


import java.sql.Date;
import java.math.BigDecimal;

public class Pago {
    private Long idPago;
    private BigDecimal monto;
    private Date fecha;
    private Long empleadoIdEmpleado;
    private Long comprobanteIdComprobante;
    private Long metodopagoIdMetodo;

    // Getters y Setters
    public Long getIdPago() { return idPago; }
    public void setIdPago(Long idPago) { this.idPago = idPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Long getEmpleadoIdEmpleado() { return empleadoIdEmpleado; }
    public void setEmpleadoIdEmpleado(Long empleadoIdEmpleado) { this.empleadoIdEmpleado = empleadoIdEmpleado; }

    public Long getComprobanteIdComprobante() { return comprobanteIdComprobante; }
    public void setComprobanteIdComprobante(Long comprobanteIdComprobante) { this.comprobanteIdComprobante = comprobanteIdComprobante; }

    public Long getMetodopagoIdMetodo() { return metodopagoIdMetodo; }
    public void setMetodopagoIdMetodo(Long metodopagoIdMetodo) { this.metodopagoIdMetodo = metodopagoIdMetodo; }

    @Override
    public String toString() {
        return "Pago " + idPago + " - S/ " + monto;
    }
}