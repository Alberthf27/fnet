package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {
    private final String DRIVER = "oracle.jdbc.OracleDriver";
    private final String URL = "jdbc:oracle:thin:@localhost:1522:XE";
    private final String USER = "alberth";
    private final String PASSWORD = "alberth789"; // Corregí "PASWORD" a "PASSWORD"

    public Connection cadena;

    public Conexion() {
        this.cadena = null;
    }

    public Connection conectar() {
        try {
            Class.forName(DRIVER);
            this.cadena = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("SE CONECTÓ CORRECTAMENTE");
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
            System.exit(0);
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
            JOptionPane.showMessageDialog(null, "Error al cerrar conexión: " + e.getMessage());
        }
    }

    // Método main corregido
    public static void main(String[] args) {
        Conexion c = new Conexion(); // Cambié "ConexionOracle" por "Conexion"
        c.conectar();
        c.desconectar();
    }
}