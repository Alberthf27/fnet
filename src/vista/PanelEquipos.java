package vista;

public class PanelEquipos extends javax.swing.JPanel {

    public PanelEquipos() {
        initComponents(); // NetBeans init
        
        // Configurar Pestañas
        jTabbedPane1.removeAll();
        
        // Pestaña 1: Inventario (El panel que acabamos de crear)
        jTabbedPane1.addTab("Inventario General", 
                cargarIcono("/img/equipos.png"), 
                new subpanel_Inventario());
        
        // Pestaña 2: Tipos de Equipo (Para no repetir código, aquí podrías reusar 
        // la lógica de 'subpanel_Planes' (lista izquierda, form derecha) 
        // pero adaptada a Tipos, o crear un 'subpanel_TiposEquipo' nuevo).
        // Por ahora dejamos un panel vacío para que compile.
        jTabbedPane1.addTab("Modelos y Tipos", 
                cargarIcono("/img/servicios.png"), 
                new javax.swing.JPanel()); 
    }

    private javax.swing.Icon cargarIcono(String ruta) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            return (url != null) ? new javax.swing.ImageIcon(url) : null;
        } catch (Exception e) { return null; }
    }

    // --- CÓDIGO GENERADO POR NETBEANS (NO TOCAR, SE LIMPIA EN CONSTRUCTOR) ---
    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1140, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
        );
    }
    private javax.swing.JTabbedPane jTabbedPane1;
}