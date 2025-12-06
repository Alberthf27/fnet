/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;


import java.math.BigDecimal;
import java.sql.Timestamp;

public class CAJA {
    private Long idCaja;
    private Timestamp fechaApertura;
    private Timestamp fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private BigDecimal montoCalculado;
    private String estado; // ABIERTA, CERRADA
    private Long usuarioIdUsuario;
    private String observaciones;

    // Constructores
    public CAJA() {}
    
    public CAJA(BigDecimal montoInicial, Long usuarioIdUsuario) {
        this.montoInicial = montoInicial;
        this.usuarioIdUsuario = usuarioIdUsuario;
        this.estado = "ABIERTA";
    }

    // Getters y Setters
    public Long getIdCaja() { return idCaja; }
    public void setIdCaja(Long idCaja) { this.idCaja = idCaja; }

    public Timestamp getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Timestamp fechaApertura) { this.fechaApertura = fechaApertura; }

    public Timestamp getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Timestamp fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; }

    public BigDecimal getMontoFinal() { return montoFinal; }
    public void setMontoFinal(BigDecimal montoFinal) { this.montoFinal = montoFinal; }

    public BigDecimal getMontoCalculado() { return montoCalculado; }
    public void setMontoCalculado(BigDecimal montoCalculado) { this.montoCalculado = montoCalculado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getUsuarioIdUsuario() { return usuarioIdUsuario; }
    public void setUsuarioIdUsuario(Long usuarioIdUsuario) { this.usuarioIdUsuario = usuarioIdUsuario; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Caja " + idCaja + " - " + estado + " - Inicial: S/ " + montoInicial;
    }
}