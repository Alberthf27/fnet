package vista;

import DAO.ClienteDAO;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;

public class PanelClientes extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private ClienteDAO clienteDAO;

    public PanelClientes() {
        // 1. Deja que NetBeans haga su trabajo sucio (crea variables)
        initComponents(); 

        // ============================================================
        // 2. CORRECCIÓN VISUAL (SOBRESCRIBIR EL DISEÑO DE NETBEANS)
        // ============================================================
        
        // A. Cambiamos el "Layout Absoluto" (rígido) por "BorderLayout" (elástico)
        this.setLayout(new BorderLayout()); 
        
        // B. Limpiamos las pestañas vacías
        jTabbedPane1.removeAll();
        
        // C. Agregamos las pestañas REALES con tus subpaneles
        jTabbedPane1.addTab("Directorio Personas", 
                cargarIcono("/img/clientes.png"), 
                new subpanel_DirectorioClientes());

        jTabbedPane1.addTab("Contratos / Suscripciones", 
                cargarIcono("/img/servicios.png"), 
                new subpanel_Suscripciones());
        
        // D. TRUCO FINAL: Re-agregamos el jTabbedPane al centro del nuevo layout
        // Esto hace que el panel de pestañas se estire a todo lo alto y ancho disponible
        this.add(jTabbedPane1, BorderLayout.CENTER);
    }

    // Método auxiliar seguro para iconos
    private javax.swing.Icon cargarIcono(String ruta) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            return (url != null) ? new javax.swing.ImageIcon(url) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * CÓDIGO GENERADO POR NETBEANS (NO TOCAR)
     * Aunque aquí diga AbsoluteLayout, el constructor lo corregirá al ejecutarse.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        // Configuración rígida de NetBeans (que ignoraremos después)
        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(new java.awt.Dimension(1170, 728));
        setPreferredSize(new java.awt.Dimension(1214, 728));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPane1.addTab("Gestion de cortes", jPanel4);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPane1.addTab("Directorio", jPanel2);

        // NetBeans lo agrega con coordenadas fijas (esto es lo que causa el problema de altura)
        add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1210, 730));
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration                   
}