package modelo;

import java.sql.Timestamp; 
import java.sql.Date;

public class Cliente {

    private Long idCliente;
    private String dniCliente;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String correo;
    private Timestamp fechaRegistro; // Antes era Date
    private Integer activo;
    private Double deuda;

    // Constructores
    public Cliente() {
    }

    public Cliente(String dniCliente, String nombres, String apellidos, String direccion) {
        this.dniCliente = dniCliente;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.activo = 1;
        this.deuda = 0.0; // Valor por defecto

    }

    // Getters y Setters
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + dniCliente;
    }

    public Double getDeuda() {
        return deuda;
    }

    public void setDeuda(Double deuda) {
        this.deuda = deuda;
    }
}
