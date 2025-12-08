package vista;

public class panel_Servicios extends javax.swing.JPanel {

    public panel_Servicios() {
        initComponents(); // NetBeans init
        
        // Personalizar Pestañas
        jTabbedPane1.removeAll();
        
        // Pestaña 1: El Catálogo que acabamos de crear
        jTabbedPane1.addTab("Planes y Tarifas", 
                cargarIcono("/img/servicios.png"), 
                new subpanel_Planes());
        
        // Pestaña 2: Configuración (Proveedores, Tipos) - Opcional para MVP
        // Usamos un panel vacío por ahora o puedes crear subpanel_Configuracion
        jTabbedPane1.addTab("Configuración Técnica", 
                cargarIcono("/img/equipos.png"), 
                new javax.swing.JPanel());
    }

    private javax.swing.Icon cargarIcono(String ruta) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            return (url != null) ? new javax.swing.ImageIcon(url) : null;
        } catch (Exception e) { return null; }
    }

    // --- CÓDIGO GENERADO ---
    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        setLayout(new java.awt.BorderLayout());
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }
    private javax.swing.JTabbedPane jTabbedPane1;
}