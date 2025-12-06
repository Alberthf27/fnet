
package modelo;

public class TipoServicio {
    private Long idTipoServicio;
    private String nombre;
    private String descripcion;

    // Getters y Setters
    public Long getIdTipoServicio() { return idTipoServicio; }
    public void setIdTipoServicio(Long idTipoServicio) { this.idTipoServicio = idTipoServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre;
    }
}