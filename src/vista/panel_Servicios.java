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

public class panel_Servicios extends JPanel {

    public panel_Servicios() {
        // 1. Usamos BorderLayout: La clave para que se expanda
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(241, 245, 249)); // Fondo gris claro moderno

        // --- ZONA SUPERIOR (Header) ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BorderLayout());
        pnlHeader.setBackground(getBackground());
        pnlHeader.setPreferredSize(new Dimension(0, 80)); // Altura fija del encabezado
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Título y Subtítulo
        JPanel pnlTitulos = new JPanel(new BorderLayout());
        pnlTitulos.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("Catálogo de Servicios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        
        JLabel lblSub = new JLabel("Gestiona los planes de internet y precios");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        
        pnlTitulos.add(lblTitulo, BorderLayout.NORTH);
        pnlTitulos.add(lblSub, BorderLayout.SOUTH);
        pnlHeader.add(pnlTitulos, BorderLayout.WEST);

        // Botones y Buscador (Derecha)
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlControles.setOpaque(false);

        JTextField txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar servicio..."); // (Si usas FlatLaf)
        
        JButton btnBuscar = new JButton("Buscar");
        JButton btnNuevo = new JButton("+ Nuevo Servicio");
        btnNuevo.setBackground(new Color(37, 99, 235));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFocusPainted(false);
        
        pnlControles.add(txtBuscar);
        pnlControles.add(btnBuscar);
        pnlControles.add(btnNuevo);
        pnlHeader.add(pnlControles, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // --- ZONA CENTRAL (Tabla) ---
        // Al ponerlo en el CENTER del BorderLayout, crecerá automáticamente
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(new EmptyBorder(0, 30, 30, 30)); // Márgenes laterales

        String[] columnas = {"ID", "Descripción", "Velocidad (MB)", "Mensualidad", "Estado"};
        // Datos de ejemplo para ver el diseño
        Object[][] datos = {
            {"1", "Plan Básico Fibra", "50 MB", "S/. 50.00", "Activo"},
            {"2", "Plan Estándar Fibra", "100 MB", "S/. 80.00", "Activo"},
            {"3", "Plan Gamer Pro", "300 MB", "S/. 120.00", "Activo"}
        };

        DefaultTableModel model = new DefaultTableModel(datos, columnas);
        JTable tabla = new JTable(model);
        tabla.setRowHeight(35);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        pnlTabla.add(scroll, BorderLayout.CENTER); // <--- ESTO HACE QUE SE EXPANDA
        
        add(pnlTabla, BorderLayout.CENTER);
    }
}