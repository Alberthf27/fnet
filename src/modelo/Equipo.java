
package modelo;

import java.math.BigDecimal;

public class Equipo {
    private Long idEquipo;
    private BigDecimal costo;
    private String serial;
    private Long instalacionIdInstalacion;
    private Long tipoequipoIdTipo;

    // Getters y Setters
    public Long getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Long idEquipo) { this.idEquipo = idEquipo; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public Long getInstalacionIdInstalacion() { return instalacionIdInstalacion; }
    public void setInstalacionIdInstalacion(Long instalacionIdInstalacion) { this.instalacionIdInstalacion = instalacionIdInstalacion; }

    public Long getTipoequipoIdTipo() { return tipoequipoIdTipo; }
    public void setTipoequipoIdTipo(Long tipoequipoIdTipo) { this.tipoequipoIdTipo = tipoequipoIdTipo; }

    @Override
    public String toString() {
        return "Equipo " + idEquipo + " - Serial: " + serial;
    }
}
