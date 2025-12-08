package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_Suscripciones extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JComboBox<String> cmbEstadoRapido;

    public subpanel_Suscripciones() {
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
    }

    private void initContenido() {
        // Título y Buscador (Igual que antes)
        JLabel lblTitulo = new JLabel("Gestión de Contratos y Deudas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 400, 30);
        add(lblTitulo);

        JTextField txtBuscar = new JTextField();
        txtBuscar.setBounds(30, 70, 300, 35);
        add(txtBuscar);
        
        JButton btnBuscar = new JButton("🔍");
        btnBuscar.setBounds(330, 70, 50, 35);
        add(btnBuscar);

        // --- ACCIONES RÁPIDAS ---
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setBounds(420, 70, 60, 35);
        add(lblEstado);

        cmbEstadoRapido = new JComboBox<>(new String[]{"ACTIVO", "SUSPENDIDO", "BAJA"});
        cmbEstadoRapido.setBounds(480, 70, 120, 35);
        add(cmbEstadoRapido);

        JButton btnAplicar = new JButton("✔");
        btnAplicar.setBackground(new Color(22, 163, 74));
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.setBounds(610, 70, 40, 35);
        add(btnAplicar);

        JButton btnEditar = new JButton("📝 Editar Detalles");
        btnEditar.setBackground(new Color(37, 99, 235));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setBounds(670, 70, 150, 35);
        add(btnEditar);
        
        JButton btnNuevo = new JButton("+ Nuevo");
        btnNuevo.setBackground(new Color(15, 23, 42));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setBounds(980, 70, 130, 35);
        add(btnNuevo);

        // --- TABLA CON INFORMACIÓN FINANCIERA ---
        // Nuevas Columnas: Vencimiento, Deuda Total, Meses
        String[] cols = {"ID", "Cliente", "Plan", "Dirección", "Vencimiento", "Deuda Total", "Meses", "Estado"};
        
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        
        // RENDERER PERSONALIZADO: Pinta de ROJO si hay deuda
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Lógica visual: Si la columna "Deuda" (índice 5) tiene valor > 0, pintar texto rojo
                String deudaStr = (String) table.getValueAt(row, 5); // "S/. 50.00"
                if (deudaStr.contains("S/.") && !deudaStr.equals("S/. 0.00")) {
                    if (column == 5 || column == 6) { // Columnas Deuda y Meses
                        setForeground(new Color(220, 38, 38)); // Rojo
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else {
                        setForeground(Color.BLACK);
                    }
                } else {
                    setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    setBackground(new Color(224, 231, 255)); // Azul claro selección
                } else {
                    setBackground(Color.WHITE);
                }
                return c;
            }
        });

        // Datos de ejemplo con DEUDAS REALES
        modelo.addRow(new Object[]{"101", "Juan Perez", "Plan 50MB", "Av. España 123", "05/03/2025", "S/. 50.00", "1", "ACTIVO"});
        modelo.addRow(new Object[]{"102", "Maria Lopez", "TV Cable", "Jr. Lima 44", "05/02/2025", "S/. 100.00", "2", "SUSPENDIDO"});
        modelo.addRow(new Object[]{"103", "Empresa ABC", "Dedicado", "Centro Civico", "05/03/2025", "S/. 0.00", "0", "ACTIVO"});

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 130, 1080, 550);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);
    }
}