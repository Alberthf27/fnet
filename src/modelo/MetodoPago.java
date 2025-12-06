
package modelo;

public class MetodoPago {
    private Long idMetodo;
    private String nombre;
    private String descripcion;

    // Getters y Setters
    public Long getIdMetodo() { return idMetodo; }
    public void setIdMetodo(Long idMetodo) { this.idMetodo = idMetodo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre;
    }
}