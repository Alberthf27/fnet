
package modelo;


import java.math.BigDecimal;
import java.sql.Timestamp;

public class MovimientoCaja {
    private Long idMovimiento;
    private Timestamp fecha;
    private String tipo; // INGRESO, EGRESO
    private String descripcion;
    private BigDecimal monto;
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA
    private Long cajaIdCaja;
    private Long pagoIdPago; // Relación opcional con pago
    private Long facturaIdFactura; // Relación opcional con factura
    private String observaciones;

    // Constructores
    public MovimientoCaja() {}
    
    public MovimientoCaja(String tipo, String descripcion, BigDecimal monto, Long cajaIdCaja) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.cajaIdCaja = cajaIdCaja;
        this.fecha = new Timestamp(System.currentTimeMillis());
    }

    // Getters y Setters
    public Long getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Long idMovimiento) { this.idMovimiento = idMovimiento; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public Long getCajaIdCaja() { return cajaIdCaja; }
    public void setCajaIdCaja(Long cajaIdCaja) { this.cajaIdCaja = cajaIdCaja; }

    public Long getPagoIdPago() { return pagoIdPago; }
    public void setPagoIdPago(Long pagoIdPago) { this.pagoIdPago = pagoIdPago; }

    public Long getFacturaIdFactura() { return facturaIdFactura; }
    public void setFacturaIdFactura(Long facturaIdFactura) { this.facturaIdFactura = facturaIdFactura; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return tipo + " - " + descripcion + " - S/ " + monto;
    }
}