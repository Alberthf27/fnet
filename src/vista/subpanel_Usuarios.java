package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_Usuarios extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;

    // Campos del Formulario
    private JTextField txtNombre, txtDNI, txtTelefono, txtEmail, txtUsuario;
    private JPasswordField txtContrasena;
    private JComboBox<String> cmbRol, cmbEstado;
    private JButton btnGuardar, btnLimpiar;

    public subpanel_Usuarios() {
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
        cargarDatosSimulados();
    }

    private void initContenido() {
        // ====================================================================
        // COLUMNA IZQUIERDA: LISTADO DE USUARIOS
        // ====================================================================
        JLabel lblTitulo = new JLabel("Gestión de Usuarios y Empleados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 500, 30);
        add(lblTitulo);

        JTextField txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o Usuario...");
        txtBuscar.setBounds(30, 70, 280, 35);
        add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        estilarBotonSimple(btnBuscar);
        btnBuscar.setBounds(320, 70, 50, 35);
        add(btnBuscar);

        // Tabla
        // Columnas clave para la gestión: ID, Nombre, Rol, Usuario, Estado
        String[] cols = {"ID", "Nombre Completo", "Rol", "Usuario", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        
        // RENDERER: Colores para el Estado (ACTIVO/INACTIVO)
        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) value;
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if (estado.equals("ACTIVO")) {
                    setForeground(new Color(22, 163, 74)); // Verde
                } else if (estado.equals("INACTIVO")) {
                    setForeground(new Color(220, 38, 38)); // Rojo
                } else {
                    setForeground(Color.GRAY);
                }
                return c;
            }
        });

        // Evento: Al hacer clic en la tabla, llenar el formulario
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cargarFormularioDesdeTabla();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 120, 600, 560);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // ====================================================================
        // COLUMNA DERECHA: FORMULARIO DE EDICIÓN
        // ====================================================================
        JPanel panelForm = new JPanel(null);
        panelForm.setBackground(new Color(248, 250, 252));
        panelForm.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelForm.setBounds(660, 0, 554, 760);
        add(panelForm);

        JLabel lblForm = new JLabel("Datos Personales y Credenciales");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblForm.setForeground(new Color(15, 23, 42));
        lblForm.setBounds(30, 30, 300, 30);
        panelForm.add(lblForm);

        int y = 80;

        // 1. Nombre Completo
        panelForm.add(crearLabel("Nombre Completo:", 30, y));
        txtNombre = new JTextField();
        txtNombre.setBounds(30, y + 25, 490, 35);
        panelForm.add(txtNombre);
        y += 70;

        // 2. DNI / Cédula
        panelForm.add(crearLabel("DNI / Cédula:", 30, y));
        txtDNI = new JTextField();
        txtDNI.setBounds(30, y + 25, 230, 35);
        panelForm.add(txtDNI);
        
        // 3. Teléfono
        panelForm.add(crearLabel("Teléfono:", 290, y));
        txtTelefono = new JTextField();
        txtTelefono.setBounds(290, y + 25, 230, 35);
        panelForm.add(txtTelefono);
        y += 70;
        
        // 4. Email
        panelForm.add(crearLabel("Email:", 30, y));
        txtEmail = new JTextField();
        txtEmail.setBounds(30, y + 25, 490, 35);
        panelForm.add(txtEmail);
        y += 70;

        // --- CREDENCIALES ---
        JLabel lblCredenciales = new JLabel("Credenciales del Sistema");
        lblCredenciales.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCredenciales.setForeground(new Color(15, 23, 42));
        lblCredenciales.setBounds(30, y + 10, 300, 20);
        panelForm.add(lblCredenciales);
        y += 40;
        
        // 5. Usuario (Login) -> Se conecta al campo 'usuario' de la tabla Usuario
        panelForm.add(crearLabel("Usuario (Login):", 30, y));
        txtUsuario = new JTextField();
        txtUsuario.setBounds(30, y + 25, 230, 35);
        panelForm.add(txtUsuario);
        
        // 6. Contraseña
        panelForm.add(crearLabel("Contraseña (Dejar vacío para no cambiar):", 290, y));
        txtContrasena = new JPasswordField();
        txtContrasena.setBounds(290, y + 25, 230, 35);
        panelForm.add(txtContrasena);
        y += 70;
        
        // 7. Rol (Niveles de Permiso) -> Se conecta a la tabla 'Rol'
        panelForm.add(crearLabel("Rol / Permisos:", 30, y));
        cmbRol = new JComboBox<>(new String[]{"ADMINISTRADOR", "CAJERO", "TÉCNICO", "SOPORTE"});
        cmbRol.setBounds(30, y + 25, 230, 35);
        cmbRol.setBackground(Color.WHITE);
        panelForm.add(cmbRol);

        // 8. Estado (Activo/Inactivo) -> Se conecta a la tabla 'EstadoUsuario'
        panelForm.add(crearLabel("Estado del Usuario:", 290, y));
        cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        cmbEstado.setBounds(290, y + 25, 230, 35);
        cmbEstado.setBackground(Color.WHITE);
        panelForm.add(cmbEstado);
        y += 70;

        // BOTONES DE ACCIÓN
        btnLimpiar = new JButton("Limpiar / Nuevo");
        estilarBoton(btnLimpiar, Color.WHITE, new Color(15, 23, 42));
        btnLimpiar.setBorder(new LineBorder(new Color(203, 213, 225)));
        btnLimpiar.setBounds(30, y + 20, 150, 40);
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        panelForm.add(btnLimpiar);

        btnGuardar = new JButton("GUARDAR USUARIO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE); // Azul fuerte
        btnGuardar.setBounds(200, y + 20, 320, 40);
        btnGuardar.addActionListener(e -> accionGuardar());
        panelForm.add(btnGuardar);
    }

    // --- LÓGICA DE NEGOCIO Y SIMULACIÓN ---

    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            // Carga de datos de ejemplo (en un proyecto real, se consultaría a UsuarioDAO)
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            txtDNI.setText("10203040"); 
            txtTelefono.setText("987654321");
            txtEmail.setText("usuario" + modelo.getValueAt(fila, 0).toString() + "@empresa.com");
            
            // Credenciales y Permisos
            txtUsuario.setText(modelo.getValueAt(fila, 3).toString());
            cmbRol.setSelectedItem(modelo.getValueAt(fila, 2).toString());
            cmbEstado.setSelectedItem(modelo.getValueAt(fila, 4).toString());
            txtContrasena.setText(""); 
            
            btnGuardar.setText("ACTUALIZAR USUARIO");
            btnGuardar.setBackground(new Color(22, 163, 74)); // Cambia a verde
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtNombre.setText("");
        txtDNI.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtUsuario.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        btnGuardar.setText("GUARDAR USUARIO");
        btnGuardar.setBackground(new Color(37, 99, 235)); // Vuelve a azul
    }

    private void accionGuardar() {
        // En un proyecto real, esto llamaría a un UsuarioDAO para insertar o actualizar.
        String nombre = txtNombre.getText();
        String usuario = txtUsuario.getText();
        
        if (nombre.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y el usuario son obligatorios.");
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Usuario " + nombre + " guardado/actualizado. Rol asignado: " + cmbRol.getSelectedItem());
        limpiarFormulario();
        cargarDatosSimulados(); 
    }

    private void cargarDatosSimulados() {
        modelo.setRowCount(0);
        
        // Datos simulados (Rol y Estado provienen de sus respectivas tablas)
        modelo.addRow(new Object[]{"1", "Carlos Mendoza", "ADMINISTRADOR", "cmendoza", "ACTIVO"});
        modelo.addRow(new Object[]{"2", "Ana Torres", "CAJERO", "atorres", "ACTIVO"});
        modelo.addRow(new Object[]{"3", "Luis Ramos", "TÉCNICO", "lramos", "ACTIVO"});
        modelo.addRow(new Object[]{"4", "Gerente General", "ADMINISTRADOR", "ggeneral", "INACTIVO"});
    }

    // --- UTILIDADES DE ESTILO ---
    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
    
    private void estilarBotonSimple(JButton btn) {
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
    }
}