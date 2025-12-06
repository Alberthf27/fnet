
package modelo;

import java.sql.Date;
import java.math.BigDecimal;

public class Factura {
    private Long idFactura;
    private BigDecimal monto;
    private Date fechaEmision;
    private Date fechaVencimiento;
    private Long servicioIdServicio;
    private BigDecimal montoPendiente;
    private Integer esAdelantada;
    private BigDecimal montoCubierto;
    private Long estadofacturaIdEstado;

    // Getters y Setters
    public Long getIdFactura() { return idFactura; }
    public void setIdFactura(Long idFactura) { this.idFactura = idFactura; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }

    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Long getServicioIdServicio() { return servicioIdServicio; }
    public void setServicioIdServicio(Long servicioIdServicio) { this.servicioIdServicio = servicioIdServicio; }

    public BigDecimal getMontoPendiente() { return montoPendiente; }
    public void setMontoPendiente(BigDecimal montoPendiente) { this.montoPendiente = montoPendiente; }

    public Integer getEsAdelantada() { return esAdelantada; }
    public void setEsAdelantada(Integer esAdelantada) { this.esAdelantada = esAdelantada; }

    public BigDecimal getMontoCubierto() { return montoCubierto; }
    public void setMontoCubierto(BigDecimal montoCubierto) { this.montoCubierto = montoCubierto; }

    public Long getEstadofacturaIdEstado() { return estadofacturaIdEstado; }
    public void setEstadofacturaIdEstado(Long estadofacturaIdEstado) { this.estadofacturaIdEstado = estadofacturaIdEstado; }

    @Override
    public String toString() {
        return "Factura " + idFactura + " - S/ " + monto;
    }
}