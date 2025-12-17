
package util;
import java.io.File;

public class VerificarRuta {
    public static void main(String[] args) {
        // 1. Probamos si la carpeta existe
        String rutaCarpeta = "E:\\ALBERTH ALM\\DESCARGAS";
        File carpeta = new File(rutaCarpeta);

        if (!carpeta.exists()) {
            System.out.println("❌ ERROR: Java dice que la carpeta NO existe.");
            System.out.println("   Ruta buscada: " + rutaCarpeta);
            System.out.println("   Prueba verificando si la carpeta 'DESCARGAS' se llama 'Downloads' o si 'ALBERTH ALM' está bien escrito.");
        } else {
            System.out.println("✅ La carpeta existe. Listando archivos encontrados:");
            System.out.println("--------------------------------------------------");
            
            // 2. Listamos lo que Java ve dentro
            File[] archivos = carpeta.listFiles();
            boolean encontrado = false;
            
            if (archivos != null) {
                for (File f : archivos) {
                    if (f.getName().startsWith("Libro1")) {
                        System.out.println("🎯 CANDIDATO ENCONTRADO: " + f.getName());
                        System.out.println("   Ruta completa para copiar: " + f.getAbsolutePath().replace("\\", "\\\\"));
                        encontrado = true;
                    } else {
                        // Descomenta esto si quieres ver todos los archivos
                        // System.out.println(" - " + f.getName());
                    }
                }
            }
            
            if (!encontrado) {
                System.out.println("⚠️ No encontré ningún archivo que empiece con 'Libro1' en esta carpeta.");
            }
        }
    }
}