
package modelo;

import java.math.BigDecimal;

public class PagoFactura {
    private Long idPagoFactura;
    private BigDecimal montoAplicado;
    private Long facturaIdFactura;
    private Long pagoIdPago;

    // Getters y Setters
    public Long getIdPagoFactura() { return idPagoFactura; }
    public void setIdPagoFactura(Long idPagoFactura) { this.idPagoFactura = idPagoFactura; }

    public BigDecimal getMontoAplicado() { return montoAplicado; }
    public void setMontoAplicado(BigDecimal montoAplicado) { this.montoAplicado = montoAplicado; }

    public Long getFacturaIdFactura() { return facturaIdFactura; }
    public void setFacturaIdFactura(Long facturaIdFactura) { this.facturaIdFactura = facturaIdFactura; }

    public Long getPagoIdPago() { return pagoIdPago; }
    public void setPagoIdPago(Long pagoIdPago) { this.pagoIdPago = pagoIdPago; }

    @Override
    public String toString() {
        return "PagoFactura: Pago " + pagoIdPago + " - Factura " + facturaIdFactura;
    }
}
