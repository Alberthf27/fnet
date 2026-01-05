package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class subpanel_Instalaciones extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private JComboBox<String> cmbFiltroEstado;

    // Campos del formulario derecho
    private JTextField txtCliente, txtDireccion, txtFecha;
    private JComboBox<String> cmbTecnico;
    private JTextArea txtNotas;

    public subpanel_Instalaciones() {
        setBackground(Color.WHITE);
        setLayout(null); // Diseño absoluto para control total
        initContenido();
        cargarDatosSimulados(); // Para que veas cómo queda
    }

    private void initContenido() {
        // ====================================================================
        // COLUMNA IZQUIERDA: AGENDA (LISTADO)
        // ====================================================================
        JLabel lblTitulo = new JLabel("Agenda de Instalaciones");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 400, 30);
        add(lblTitulo);

        // Filtro de Estado
        JLabel lblFiltro = new JLabel("Mostrar:");
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFiltro.setBounds(30, 70, 100, 35);
        add(lblFiltro);

        cmbFiltroEstado = new JComboBox<>(new String[] { "PENDIENTES", "REALIZADAS", "CANCELADAS" });
        cmbFiltroEstado.setBounds(100, 70, 150, 35);
        add(cmbFiltroEstado);

        JButton btnRefrescar = new JButton("Actualizar");
        estilarBoton(btnRefrescar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnRefrescar.setBounds(260, 70, 120, 35);
        add(btnRefrescar);

        // Tabla de Visitas
        String[] cols = { "ID", "Fecha", "Cliente", "Dirección", "Estado" };
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);

        // Simular clic en la tabla para llenar la derecha
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cargarDetalleSimulado();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 120, 600, 560);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // ====================================================================
        // COLUMNA DERECHA: DETALLES Y CIERRE
        // ====================================================================
        JPanel panelDetalle = new JPanel(null);
        panelDetalle.setBackground(new Color(248, 250, 252)); // Gris claro
        panelDetalle.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelDetalle.setBounds(660, 0, 554, 760);
        add(panelDetalle);

        JLabel lblDetalle = new JLabel("Detalle de la Visita");
        lblDetalle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetalle.setForeground(new Color(15, 23, 42));
        lblDetalle.setBounds(30, 30, 300, 30);
        panelDetalle.add(lblDetalle);

        int y = 80;

        // 1. Cliente (Solo lectura, viene de la tabla)
        panelDetalle.add(crearLabel("Cliente / Suscriptor:", 30, y));
        txtCliente = new JTextField();
        txtCliente.setEditable(false);
        txtCliente.setBounds(30, y + 25, 490, 35);
        panelDetalle.add(txtCliente);
        y += 70;

        // 2. Dirección y Coordenadas
        panelDetalle.add(crearLabel("Dirección de Instalación:", 30, y));
        txtDireccion = new JTextField();
        txtDireccion.setEditable(false);
        txtDireccion.setBounds(30, y + 25, 490, 35);
        panelDetalle.add(txtDireccion);
        y += 70;

        // 3. Asignación de Técnico (Empleado)
        panelDetalle.add(crearLabel("Técnico Asignado:", 30, y));
        cmbTecnico = new JComboBox<>(new String[] { "Sin Asignar", "Juan Técnico", "Carlos Instalador" });
        cmbTecnico.setBounds(30, y + 25, 230, 35);
        cmbTecnico.setBackground(Color.WHITE);
        panelDetalle.add(cmbTecnico);

        // 4. Fecha Programada
        panelDetalle.add(crearLabel("Fecha Programada:", 290, y));
        txtFecha = new JTextField();
        txtFecha.setBounds(290, y + 25, 230, 35);
        panelDetalle.add(txtFecha);
        y += 70;

        // 5. Notas Técnicas / Materiales
        panelDetalle.add(crearLabel("Notas de Cierre / Materiales Usados:", 30, y));
        txtNotas = new JTextArea();
        txtNotas.setLineWrap(true);
        txtNotas.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtNotas.setBounds(30, y + 25, 490, 100);
        panelDetalle.add(txtNotas);
        y += 140;

        // BOTONES DE ACCIÓN
        JButton btnFinalizar = new JButton("FINALIZAR (Instalado)");
        estilarBoton(btnFinalizar, new Color(22, 163, 74), Color.WHITE); // Verde
        btnFinalizar.setBounds(200, y, 320, 45);
        panelDetalle.add(btnFinalizar);

        JButton btnReprogramar = new JButton("Reprogramar");
        estilarBoton(btnReprogramar, new Color(234, 179, 8), Color.WHITE); // Amarillo
        btnReprogramar.setBounds(30, y, 150, 45);
        panelDetalle.add(btnReprogramar);
    }

    // --- MÉTODOS DE SOPORTE ---

    private void cargarDatosSimulados() {
        modelo.addRow(new Object[] { "101", "15/10/2025", "Restaurante El Gusto", "Av. Principal 123", "PENDIENTE" });
        modelo.addRow(new Object[] { "102", "16/10/2025", "Familia Gomez", "Calle Los Olivos 44", "PENDIENTE" });
        modelo.addRow(new Object[] { "103", "16/10/2025", "CyberCafe Zona", "Jr. Comercio 88", "REALIZADO" });
    }

    private void cargarDetalleSimulado() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            txtCliente.setText(modelo.getValueAt(fila, 2).toString());
            txtDireccion.setText(modelo.getValueAt(fila, 3).toString());
            txtFecha.setText(modelo.getValueAt(fila, 1).toString());
            // Aquí en el futuro cargarás datos reales de la BD
        }
    }

    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(x, y, 300, 20);
        return lbl;
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}