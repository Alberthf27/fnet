package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {

    // --- CREDENCIALES DE RAILWAY (NOZOMI) ---
    private final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Datos extraídos de tu conexión anterior exitosa
    private final String HOST = "nozomi.proxy.rlwy.net";
    private final String PORT = "20409";
    private final String DB = "railway";
    private final String USER = "root";
    private final String PASSWORD = "MUjBYtfwVPnAMAoHGDXbHqsIXYDTZnWs"; // Tu contraseña de Railway

    // URL Optimizada para evitar latencia
    private final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima"
            + "&useLegacyDatetimeCode=false"
            + "&autoReconnect=true";

    public Connection cadena;

    public Conexion() {
        this.cadena = null;
    }

    // Método de instancia (usado por código antiguo)
    public Connection conectar() {
        try {
            Class.forName(DRIVER);
            this.cadena = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("✅ Conectado a Railway"); // Descomentar para probar
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Falta el Driver MySQL (jar).");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error Conexión Nube: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
        }
        return this.cadena;
    }

    public void desconectar() {
        try {
            if (this.cadena != null && !this.cadena.isClosed()) {
                this.cadena.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }

    // --- IMPORTANTE: Método Estático para los nuevos DAOs Optimizados ---
    // Esto permite llamar a Conexion.getConexion() sin crear una instancia manual
    public static Connection getConexion() {
        return new Conexion().conectar();
    }

    // Método main para probar conexión rápida (Run File aquí para testear)
    public static void main(String[] args) {
        Conexion c = new Conexion();
        if (c.conectar() != null) {
            System.out.println("¡TEST DE CONEXIÓN A NUBE EXITOSO!");
            c.desconectar();
        }
    }
}