
package modelo;

public class Usuario {
    private Long idUsuario;
    private String contraseña;
    private String nombre;
    private Long estadousuarioIdEstado;

    // Constructores
    public Usuario() {}

    public Usuario(String contraseña, String nombre) {
        this.contraseña = contraseña;
        this.nombre = nombre;
    }

    // Getters y Setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Long getEstadousuarioIdEstado() { return estadousuarioIdEstado; }
    public void setEstadousuarioIdEstado(Long estadousuarioIdEstado) { this.estadousuarioIdEstado = estadousuarioIdEstado; }

    @Override
    public String toString() {
        return nombre + " (ID: " + idUsuario + ")";
    }
}