package vista;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class subpanel_Planes extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    
    // Campos del Formulario
    private JTextField txtNombre, txtPrecio, txtVelocidad;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbTipo, cmbProveedor;
    private JButton btnGuardar, btnLimpiar;

    public subpanel_Planes() {
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
        cargarDatosSimulados();
    }

    private void initContenido() {
        // ====================================================================
        // COLUMNA IZQUIERDA: LISTADO DE PLANES
        // ====================================================================
        JLabel lblTitulo = new JLabel("Catálogo de Servicios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 300, 30);
        add(lblTitulo);

        JTextField txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar plan...");
        txtBuscar.setBounds(30, 70, 280, 35);
        add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        estilarBoton(btnBuscar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnBuscar.setBounds(320, 70, 50, 35);
        add(btnBuscar);

        // Tabla
        String[] cols = {"ID", "Nombre del Plan", "Tipo", "Precio (S/.)", "Velocidad"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        
        // Evento: Al hacer clic en la tabla, llenar el formulario
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cargarFormularioDesdeTabla();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 120, 600, 560); // Ocupa la mitad izquierda
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // ====================================================================
        // COLUMNA DERECHA: FORMULARIO DE EDICIÓN
        // ====================================================================
        JPanel panelForm = new JPanel(null);
        panelForm.setBackground(new Color(248, 250, 252)); // Gris muy claro
        panelForm.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelForm.setBounds(660, 0, 554, 760); // Ocupa la mitad derecha hasta el final
        add(panelForm);

        JLabel lblForm = new JLabel("Detalles del Servicio");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblForm.setForeground(new Color(15, 23, 42));
        lblForm.setBounds(30, 30, 200, 30);
        panelForm.add(lblForm);

        // 1. Nombre del Plan
        panelForm.add(crearLabel("Nombre del Plan:", 30, 80));
        txtNombre = new JTextField();
        txtNombre.setBounds(30, 105, 490, 35);
        panelForm.add(txtNombre);

        // 2. Tipo de Servicio (Combo: Internet, Cable, Duo)
        panelForm.add(crearLabel("Tipo:", 30, 150));
        cmbTipo = new JComboBox<>(new String[]{"INTERNET", "CABLE TV", "DUO (Net+TV)"});
        cmbTipo.setBounds(30, 175, 230, 35);
        cmbTipo.setBackground(Color.WHITE);
        panelForm.add(cmbTipo);

        // 3. Proveedor (Combo: MikroTik, OLT, etc) -> Viene de tabla Proveedor
        panelForm.add(crearLabel("Proveedor / Tecnología:", 290, 150));
        cmbProveedor = new JComboBox<>(new String[]{"MIKROTIK_MAIN", "OLT_HUAWEI", "SATELITAL"});
        cmbProveedor.setBounds(290, 175, 230, 35);
        cmbProveedor.setBackground(Color.WHITE);
        panelForm.add(cmbProveedor);

        // 4. Precio Mensual
        panelForm.add(crearLabel("Mensualidad (S/.):", 30, 220));
        txtPrecio = new JTextField();
        txtPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPrecio.setBounds(30, 245, 230, 35);
        panelForm.add(txtPrecio);

        // 5. Velocidad / Ancho de Banda
        panelForm.add(crearLabel("Velocidad (Megas):", 290, 220));
        txtVelocidad = new JTextField();
        txtVelocidad.setBounds(290, 245, 230, 35);
        panelForm.add(txtVelocidad);

        // 6. Descripción
        panelForm.add(crearLabel("Descripción / Notas:", 30, 290));
        txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200,200,200)));
        txtDescripcion.setBounds(30, 315, 490, 80);
        panelForm.add(txtDescripcion);

        // BOTONES DE ACCIÓN
        btnLimpiar = new JButton("Limpiar / Nuevo");
        estilarBoton(btnLimpiar, Color.WHITE, new Color(15, 23, 42));
        btnLimpiar.setBorder(new LineBorder(new Color(203, 213, 225)));
        btnLimpiar.setBounds(30, 420, 150, 40);
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        panelForm.add(btnLimpiar);

        btnGuardar = new JButton("GUARDAR SERVICIO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE); // Azul fuerte
        btnGuardar.setBounds(200, 420, 320, 40);
        btnGuardar.addActionListener(e -> accionGuardar());
        panelForm.add(btnGuardar);
        
        // Mensaje de ayuda
        JLabel lblHelp = new JLabel("<html><body style='width: 350px; color: gray; font-size: 10px;'>"
                + "Nota: Al editar el precio de un plan, los contratos existentes "
                + "mantendrán su precio antiguo hasta que se actualicen manualmente."
                + "</body></html>");
        lblHelp.setBounds(30, 480, 400, 40);
        panelForm.add(lblHelp);
    }

    // --- LÓGICA ---

    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            cmbTipo.setSelectedItem(modelo.getValueAt(fila, 2).toString());
            txtPrecio.setText(modelo.getValueAt(fila, 3).toString().replace("S/. ", ""));
            txtVelocidad.setText(modelo.getValueAt(fila, 4).toString().replace(" MB", ""));
            
            btnGuardar.setText("ACTUALIZAR PLAN");
            btnGuardar.setBackground(new Color(22, 163, 74)); // Verde al editar
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtNombre.setText("");
        txtPrecio.setText("");
        txtVelocidad.setText("");
        txtDescripcion.setText("");
        cmbTipo.setSelectedIndex(0);
        btnGuardar.setText("GUARDAR SERVICIO");
        btnGuardar.setBackground(new Color(37, 99, 235)); // Azul al crear
    }

    private void accionGuardar() {
        // Aquí conectas con ServicioDAO.insertar() o actualizar()
        String nombre = txtNombre.getText();
        String precio = txtPrecio.getText();
        
        if (nombre.isEmpty() || precio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y precio son obligatorios.");
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Servicio guardado exitosamente:\n" + nombre);
        // Recargar tabla...
    }

    private void cargarDatosSimulados() {
        // Esto vendría de ServicioDAO.obtenerTodos()
        modelo.addRow(new Object[]{"1", "Plan Hogar 50MB", "INTERNET", "S/. 50.00", "50 MB"});
        modelo.addRow(new Object[]{"2", "Plan Gamer 100MB", "INTERNET", "S/. 80.00", "100 MB"});
        modelo.addRow(new Object[]{"3", "TV Cable Básico", "CABLE TV", "S/. 30.00", "N/A"});
    }

    // --- UTILIDADES ---
    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(x, y, 200, 20);
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