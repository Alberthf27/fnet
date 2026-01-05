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

public class subpanel_Inventario extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JComboBox<String> cmbFiltroEstado;

    public subpanel_Inventario() {
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
        cargarDatosSimulados();
    }

    private void initContenido() {
        // TÍTULO
        JLabel lblTitulo = new JLabel("Inventario de Equipos (Routers / Antenas)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 550, 30);
        add(lblTitulo);

        // --- BARRA DE HERRAMIENTAS ---

        // Buscador de Serial
        JTextField txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Escribe el Serial...");
        txtBuscar.setBounds(30, 70, 250, 35);
        add(txtBuscar);

        JButton btnBuscar = new JButton("Buscar");
        estilarBoton(btnBuscar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnBuscar.setBounds(290, 70, 50, 35);
        add(btnBuscar);

        // Filtro de Estado
        JLabel lblEstado = new JLabel("Filtrar:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setBounds(360, 70, 60, 35);
        add(lblEstado);

        cmbFiltroEstado = new JComboBox<>(new String[] { "TODOS", "EN STOCK", "INSTALADO", "DAÑADO" });
        cmbFiltroEstado.setBounds(420, 70, 150, 35);
        add(cmbFiltroEstado);

        // --- BOTONES DE ACCIÓN ---
        JButton btnNuevo = new JButton("+ Registrar Equipo");
        estilarBoton(btnNuevo, new Color(15, 23, 42), Color.WHITE);
        btnNuevo.setBounds(980, 70, 160, 35);
        btnNuevo.addActionListener(e -> accionNuevoEquipo());
        add(btnNuevo);

        JButton btnEditar = new JButton("✏ Editar");
        estilarBoton(btnEditar, new Color(37, 99, 235), Color.WHITE);
        btnEditar.setBounds(860, 70, 100, 35);
        add(btnEditar);

        // --- TABLA ---
        String[] cols = { "ID", "Serial / MAC", "Tipo / Modelo", "Costo (S/.)", "Ubicación", "Estado" };
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);

        // RENDERER: Colores para el estado
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) value;
                setFont(new Font("Segoe UI", Font.BOLD, 12));

                if (estado.equals("EN STOCK")) {
                    setForeground(new Color(22, 163, 74)); // Verde
                } else if (estado.equals("INSTALADO")) {
                    setForeground(new Color(37, 99, 235)); // Azul
                } else {
                    setForeground(Color.GRAY);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 130, 1110, 550);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);
    }

    private void cargarDatosSimulados() {
        // Ejemplo de lógica: Si instalacion_id es NULL -> Stock
        modelo.addRow(
                new Object[] { "1", "SN-99887766", "ONU Huawei HG8145", "80.00", "Almacén Principal", "EN STOCK" });
        modelo.addRow(
                new Object[] { "2", "SN-11223344", "Router TP-Link", "50.00", "Cliente: Juan Perez", "INSTALADO" });
        modelo.addRow(new Object[] { "3", "SN-55555555", "Antena LiteBeam", "120.00", "Almacén (Averiado)", "DAÑADO" });
    }

    private void accionNuevoEquipo() {
        // Aquí abrirías un JDialog pidiendo: Serial, Modelo (Combo de Tipos) y Costo.
        JOptionPane.showMessageDialog(this, "Abrir formulario de Alta de Equipo");
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}