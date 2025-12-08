package vista;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class subpanel_HistorialCaja extends JPanel {

    public subpanel_HistorialCaja() {
        setBackground(Color.WHITE);
        setLayout(null);
        
        JLabel lbl = new JLabel("Movimientos del Día");
        lbl.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lbl.setBounds(30, 20, 300, 30);
        add(lbl);
        
        // Tabla de movimientos
        String[] cols = {"Hora", "Cliente", "Concepto", "Método", "Monto"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        
        // Datos falsos
        model.addRow(new Object[]{"09:00", "Juan Perez", "Mensualidad Marzo", "Efectivo", "S/. 50.00"});
        model.addRow(new Object[]{"10:15", "Maria Lopez", "Instalación", "Yape", "S/. 30.00"});
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(30, 70, 1100, 500);
        add(scroll);
        
        // Total del día
        JLabel lblTotal = new JLabel("Total Recaudado: S/. 80.00");
        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTotal.setForeground(new Color(22, 163, 74));
        lblTotal.setBounds(850, 600, 300, 30);
        add(lblTotal);
    }
}