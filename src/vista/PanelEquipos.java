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

public class PanelEquipos extends JPanel {

    public PanelEquipos() {
        setLayout(new BorderLayout(0, 0)); // Diseño Responsive
        setBackground(new Color(241, 245, 249));

        // --- 1. HEADER (Fijo arriba) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(getBackground());
        pnlHeader.setPreferredSize(new Dimension(0, 80));
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Títulos
        JPanel pnlTextos = new JPanel(new BorderLayout());
        pnlTextos.setOpaque(false);
        JLabel lblTitulo = new JLabel("Inventario de Equipos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        
        JLabel lblSub = new JLabel("Gestión de Routers, Antenas y ONUs");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        
        pnlTextos.add(lblTitulo, BorderLayout.NORTH);
        pnlTextos.add(lblSub, BorderLayout.SOUTH);
        pnlHeader.add(pnlTextos, BorderLayout.WEST);

        // Controles (Buscador y Botón)
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlControles.setOpaque(false);
        
        JTextField txtBuscar = new JTextField(15);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnNuevo = new JButton("+ Registrar Equipo");
        btnNuevo.setBackground(new Color(37, 99, 235));
        btnNuevo.setForeground(Color.WHITE);
        
        pnlControles.add(txtBuscar);
        pnlControles.add(btnBuscar);
        pnlControles.add(btnNuevo);
        pnlHeader.add(pnlControles, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. TABLA (Se expande en el centro) ---
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(new EmptyBorder(0, 30, 30, 30));

        String[] col = {"Serie/MAC", "Modelo", "Tipo", "Estado", "Asignado a"};
        Object[][] data = {
            {"A1:B2:C3:D4", "Huawei HG8145V5", "ONU", "En Uso", "Juan Perez"},
            {"TP-12345-X", "TP-Link Archer C6", "Router", "Disponible", "--"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, col);
        JTable tabla = new JTable(model);
        tabla.setRowHeight(35);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        pnlTabla.add(scroll, BorderLayout.CENTER); // IMPORTANTE
        
        add(pnlTabla, BorderLayout.CENTER);
    }
}