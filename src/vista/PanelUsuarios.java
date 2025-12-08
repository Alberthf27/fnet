package vista;

public class PanelUsuarios extends javax.swing.JPanel {

    public PanelUsuarios() {
        initComponents(); 
        
        // Configuración de pestañas (solo una por ahora)
        jTabbedPane1.removeAll();
        
        jTabbedPane1.addTab("Usuarios y Permisos", 
                null, // Puedes añadir un icono si lo tienes
                new subpanel_Usuarios());
    }

    // --- CÓDIGO GENERADO (Similar a los paneles anteriores) ---
    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        setLayout(new java.awt.BorderLayout());
        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }
    private javax.swing.JTabbedPane jTabbedPane1;
}