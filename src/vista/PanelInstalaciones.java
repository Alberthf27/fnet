package vista;

import java.awt.BorderLayout;
import javax.swing.JPanel;

public class PanelInstalaciones extends JPanel {

    public PanelInstalaciones() {
        // Inicializar
        initComponents();
        
        // --- CONFIGURACIÓN MANUAL DEL DISEÑO ---
        setLayout(new BorderLayout()); // Usamos BorderLayout para que ocupe todo el espacio
        
        jTabbedPane1.removeAll(); // Limpiar pestañas por defecto
        
        // Agregar la pestaña con tu panel potente
        jTabbedPane1.addTab("Programación de Visitas", 
                cargarIcono("/img/equipos.png"), // Icono opcional
                new subpanel_Instalaciones());
        
        // Pestaña extra para historial (vacía por ahora)
        jTabbedPane1.addTab("Historial Técnico", 
                null, 
                new JPanel());
        
        add(jTabbedPane1, BorderLayout.CENTER);
    }

    private javax.swing.Icon cargarIcono(String ruta) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            return (url != null) ? new javax.swing.ImageIcon(url) : null;
        } catch (Exception e) { return null; }
    }

    // --- CÓDIGO GENERADO POR NETBEANS (Necesario para que lo reconozca) ---
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        setLayout(new java.awt.BorderLayout());
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration                   
}