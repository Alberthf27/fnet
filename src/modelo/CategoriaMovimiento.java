package modelo;

public class CategoriaMovimiento {
    private int idCategoria;
    private String nombre;
    private String tipo; // 'INGRESO' o 'EGRESO'

    public CategoriaMovimiento(int id, String nom, String tipo) {
        this.idCategoria = id;
        this.nombre = nom;
        this.tipo = tipo;
    }

    public int getIdCategoria() { return idCategoria; }
    public String getTipo() { return tipo; }
    
    @Override
    public String toString() { return nombre; } // Para que el ComboBox muestre el nombre
}