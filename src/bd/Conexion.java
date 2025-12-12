package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {

    // --- CREDENCIALES DE RAILWAY ---
    private final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private final String HOST = "nozomi.proxy.rlwy.net";
    private final String PORT = "20409";
    private final String DB = "railway";
    private final String USER = "root";
    private final String PASSWORD = "MUjBYtfwVPnAMAoHGDXbHqsIXYDTZnWs";

    // URL Optimizada con Timeouts para detectar rápido si está dormida y reintentar
    private final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima"
            + "&useLegacyDatetimeCode=false"
            + "&autoReconnect=true"
            + "&connectTimeout=10000"; // 10 segundos espera máx por intento

    public Connection cadena;

    public Conexion() {
        this.cadena = null;
    }

    // --- MÉTODO CON LÓGICA DE REINTENTOS (La Solución) ---
    public Connection conectar() {
        int intentos = 0;
        int maxIntentos = 5; // Intentará 5 veces antes de rendirse
        
        while (intentos < maxIntentos) {
            try {
                Class.forName(DRIVER);
                this.cadena = DriverManager.getConnection(URL, USER, PASSWORD);
                
                // Si llegamos aquí, conectó con éxito. Retornamos inmediatamente.
                return this.cadena; 
                
            } catch (ClassNotFoundException | SQLException e) {
                intentos++;
                System.out.println("⚠ Intento de conexión " + intentos + "/" + maxIntentos + " fallido (La BD podría estar durmiendo)...");
                
                // Si falló, esperamos 2 segundos antes de reintentar para dar tiempo a Railway
                try {
                    Thread.sleep(2000); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Si sale del bucle, fallaron los 5 intentos. Recién ahí mostramos el error al usuario.
        JOptionPane.showMessageDialog(null, "❌ Error Crítico: No se pudo despertar la Base de Datos.\nVerifique su internet o el estado de Railway.");
        return null;
    }

    public void desconectar() {
        try {
            if (this.cadena != null && !this.cadena.isClosed()) {
                this.cadena.close();
            }
        } catch (SQLException e) {
            // Error silencioso al cerrar no importa mucho
        }
    }

    public static Connection getConexion() {
        return new Conexion().conectar();
    }
}