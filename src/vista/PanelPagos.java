package vista;

public class PanelPagos extends javax.swing.JPanel {

    public PanelPagos() {
        // 1. Dejar que NetBeans inicialice su diseño básico
        initComponents();

        // 2. AHORA SOBRESCRIBIMOS EL DISEÑO
        // Usamos la variable que NetBeans creó (jTabbedPane2 en tu caso)
        // Limpiar las pestañas vacías (jPanel1, jPanel2) que puso el diseñador
        jTabbedPane2.removeAll();

        // Agregar nuestros paneles reales
        jTabbedPane2.addTab("Caja / Registrar Pago",
                cargarIcono("/img/pagos.png"),
                new subpanel_Caja()); // <--- Tu clase potente

        jTabbedPane2.addTab("Cierre de Caja",
                cargarIcono("/img/list.png"),
                new subpanel_HistorialCaja()); // <--- Tu clase potente
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1214, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 693, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Registrar Pagos", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1214, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 693, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Historial Caja", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane2;
    // End of variables declaration//GEN-END:variables
}
