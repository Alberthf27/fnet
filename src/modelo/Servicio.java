package modelo;

public class Servicio {
    private int idServicio;
    private String descripcion;
    private Double mensualidad;
    
    // --- CAMPOS QUE FALTABAN ---
    private int velocidadMb; // Para guardar los megas
    private int activo;      // 1=Activo, 0=Inactivo

    public Servicio() {}

    // Getters y Setters
    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getMensualidad() { return mensualidad; }
    public void setMensualidad(Double mensualidad) { this.mensualidad = mensualidad; }

    // --- NUEVOS GETTERS Y SETTERS ---
    public int getVelocidadMb() { return velocidadMb; }
    public void setVelocidadMb(int velocidadMb) { this.velocidadMb = velocidadMb; }

    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }

    // Alias útil
    public String getNombre() { return this.descripcion; }

    @Override
    public String toString() {
        return descripcion + " - " + velocidadMb + "MB (S/. " + mensualidad + ")";
    }
}