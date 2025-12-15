package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelInstalaciones extends JPanel {

    public PanelInstalaciones() {
        // Diseño Responsive
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(241, 245, 249));

        // 1. HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(getBackground());
        pnlHeader.setPreferredSize(new Dimension(0, 80));
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Títulos
        JPanel pnlTextos = new JPanel(new BorderLayout());
        pnlTextos.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("Programación de Instalaciones");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        
        JLabel lblSub = new JLabel("Agenda técnica y seguimiento de altas");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        
        pnlTextos.add(lblTitulo, BorderLayout.NORTH);
        pnlTextos.add(lblSub, BorderLayout.SOUTH);
        pnlHeader.add(pnlTextos, BorderLayout.WEST);

        // Botones
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtn.setOpaque(false);
        
        JButton btnCalendario = new JButton("Ver Calendario");
        JButton btnNuevaInst = new JButton("+ Nueva Instalación");
        btnNuevaInst.setBackground(new Color(22, 163, 74)); // Verde
        btnNuevaInst.setForeground(Color.WHITE);
        
        pnlBtn.add(btnCalendario);
        pnlBtn.add(btnNuevaInst);
        pnlHeader.add(pnlBtn, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // 2. CUERPO (Tabla ocupando todo el espacio)
        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(0, 30, 30, 30));

        String[] cols = {"Fecha", "Cliente", "Dirección", "Técnico", "Estado"};
        Object[][] rowData = {
            {"12/12/2025", "Juan Pérez", "Av. España 123", "Carlos T.", "Pendiente"},
            {"12/12/2025", "Maria Lopez", "Jr. Bolivar 456", "Luis R.", "En Proceso"}
        };
        
        JTable table = new JTable(new DefaultTableModel(rowData, cols));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        pnlBody.add(scroll, BorderLayout.CENTER); // <--- CLAVE PARA FULL SCREEN
        
        add(pnlBody, BorderLayout.CENTER);
    }
}