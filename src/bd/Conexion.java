package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {

    // CAMBIO 1: Driver para MySQL 8+
    private final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // CAMBIO 2: URL de conexión (Reemplaza 'nombre_de_tu_bd' por el real)
    // El parámetro serverTimezone es importante para evitar errores de hora
    private final String URL = "jdbc:mysql://127.0.0.1:3306/fibranet?serverTimezone=America/Lima&useSSL=false";

    // CAMBIO 3: Tus credenciales locales
    private final String USER = "root";
    private final String PASSWORD = "alberth789"; // <--- ¡Pon aquí tu clave de Workbench!

    public Connection cadena;

    public Conexion() {
        this.cadena = null;
    }

    public Connection conectar() {
        try {
            Class.forName(DRIVER);
            this.cadena = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ CONECTADO A MYSQL EXITOSAMENTE");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error: No se encontró el Driver MySQL. \n¿Añadiste el .jar a Libraries?");
            System.exit(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
            System.out.println("❌ Error SQL: " + e.getMessage());
        }
        return this.cadena;
    }

    public void desconectar() {
        try {
            if (this.cadena != null && !this.cadena.isClosed()) {
                this.cadena.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cerrar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Conexion c = new Conexion();
        c.conectar();
        // c.desconectar(); // Déjalo comentado para ver si conecta y se mantiene
    }
}
