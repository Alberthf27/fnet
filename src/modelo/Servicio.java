package modelo;

public class Servicio {
    private int idServicio; // Usamos int para evitar problemas
    private String descripcion;
    private Double mensualidad;

    public Servicio() {}

    public int getIdServicio() { return idServicio; }
    public void setIdServicio(int idServicio) { this.idServicio = idServicio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getMensualidad() { return mensualidad; }
    public void setMensualidad(Double mensualidad) { this.mensualidad = mensualidad; }
// Método alias para que funcione getNombre() igual que getDescripcion()
    public String getNombre() {
        return this.descripcion;
    }
    // Importante para el ComboBox del formulario
    @Override
    public String toString() {
        return descripcion + " (S/. " + mensualidad + ")";
    }
}
