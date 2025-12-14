package vista;

import DAO.ClienteDAO;
import java.awt.BorderLayout;
import javax.swing.table.DefaultTableModel;

public class PanelClientes extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private ClienteDAO clienteDAO;

    public PanelClientes() {
        // 1. Carga componentes de NetBeans (con tamaños fijos)
        initComponents(); 

        // ============================================================
        // 2. CORRECCIÓN DE DISEÑO (RESPONSIVE)
        // ============================================================
        
        // A. ANULAR TAMAÑO FIJO (CRUCIAL PARA PANTALLA COMPLETA)
        this.setPreferredSize(null); 
        this.setMinimumSize(null);
        
        // B. Cambiar a Layout Elástico
        this.setLayout(new BorderLayout()); 
        
        // C. Configurar el TabbedPane
        jTabbedPane1.removeAll(); // Limpiar pestañas de diseño
        
        
        jTabbedPane1.addTab("Contratos / Suscripciones", 
                cargarIcono("/img/servicios.png"), 
                new subpanel_Suscripciones());
        
        
        // D. Agregar Pestañas Reales
        jTabbedPane1.addTab("Directorio Personas", 
                cargarIcono("/img/clientes.png"), 
                new subpanel_DirectorioClientes());

        
        // E. Agregar al centro (Se estirará automáticamente)
        this.add(jTabbedPane1, BorderLayout.CENTER);
        
        // F. Forzar actualización visual
        this.revalidate();
        this.repaint();
    }

    private javax.swing.Icon cargarIcono(String ruta) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            return (url != null) ? new javax.swing.ImageIcon(url) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * CÓDIGO GENERADO POR NETBEANS
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(new java.awt.Dimension(1170, 728));
        setPreferredSize(new java.awt.Dimension(1214, 728));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPane1.addTab("Gestion de cortes", jPanel4);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPane1.addTab("Directorio", jPanel2);

        add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1210, 730));
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration                   
}