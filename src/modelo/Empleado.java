package modelo;

public class Empleado {
    private Long idEmpleado;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private Long rolIdRol;
    
    // ESTE ERA EL CAMPO QUE FALTABA PARA QUE FUNCIONE PRINCIPAL.JAVA
    private String cargo; 
    
    // Constructores
    public Empleado() {}

    public Empleado(String nombres, String apellidos, String dni, String telefono) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Long getRolIdRol() { return rolIdRol; }
    public void setRolIdRol(Long rolIdRol) { this.rolIdRol = rolIdRol; }

    // GETTER Y SETTER PARA 'CARGO' (EL ROL EN TEXTO)
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + dni;
    }
}