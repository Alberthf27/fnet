package vista;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class subpanel_Usuarios extends JPanel {

    // --- VARIABLES GLOBALES (Para que tu lógica funcione) ---
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtNombre, txtDNI, txtTelefono, txtEmail, txtUsuario;
    private JPasswordField txtContrasena;
    private JComboBox<String> cmbRol, cmbEstado;
    private JButton btnGuardar, btnLimpiar, btnEliminar;

    public subpanel_Usuarios() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249)); // Fondo gris suave

        initInterfaz();
        cargarDatosSimulados(); // Tu método para llenar la tabla
    }

    private void initInterfaz() {
        // 1. TÍTULO SUPERIOR
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        pnlHeader.setBackground(getBackground());
        JLabel lblTitulo = new JLabel("Gestión de Personal");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblTitulo);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. CONTENIDO PRINCIPAL (Tabla Centro + Formulario Derecha)
        JPanel pnlBody = new JPanel(new BorderLayout(20, 0));
        pnlBody.setBackground(getBackground());
        pnlBody.setBorder(new EmptyBorder(0, 20, 20, 20));

        // --- A. TABLA (CENTER) ---
        pnlBody.add(crearPanelTabla(), BorderLayout.CENTER);

        // --- B. FORMULARIO (EAST) ---
        pnlBody.add(crearPanelFormulario(), BorderLayout.EAST);

        add(pnlBody, BorderLayout.CENTER);
    }

    private JPanel crearPanelTabla() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        String[] cols = {"ID", "Nombre Completo", "Rol", "Usuario", "Estado"};
        modelo = new DefaultTableModel(null, cols) {
            @Override // Hacemos la tabla no editable directamente
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        
        // EVENTO: Al hacer clic, cargamos el formulario (TU LÓGICA)
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarFormularioDesdeTabla();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);
        pnl.add(scroll, BorderLayout.CENTER);
        
        return pnl;
    }

    private JPanel crearPanelFormulario() {
        JPanel pnl = new JPanel(null); // Layout nulo para usar setBounds (como en tu estilo)
        pnl.setPreferredSize(new Dimension(320, 0)); // Ancho fijo
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Título del Formulario
        JLabel lblSub = new JLabel("Datos del Usuario");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSub.setForeground(new Color(37, 99, 235));
        lblSub.setBounds(20, 20, 200, 20);
        pnl.add(lblSub);

        int y = 60;
        int gap = 55;

        // Campos
        pnl.add(crearLabel("Nombre Completo:", 20, y));
        txtNombre = new JTextField();
        txtNombre.setBounds(20, y + 20, 280, 30);
        estilarInput(txtNombre);
        pnl.add(txtNombre);

        y += gap;
        pnl.add(crearLabel("DNI:", 20, y));
        txtDNI = new JTextField();
        txtDNI.setBounds(20, y + 20, 130, 30);
        estilarInput(txtDNI);
        pnl.add(txtDNI);

        // Teléfono al lado del DNI
        pnl.add(crearLabel("Teléfono:", 160, y));
        txtTelefono = new JTextField();
        txtTelefono.setBounds(160, y + 20, 140, 30);
        estilarInput(txtTelefono);
        pnl.add(txtTelefono);

        y += gap;
        pnl.add(crearLabel("Correo Electrónico:", 20, y));
        txtEmail = new JTextField();
        txtEmail.setBounds(20, y + 20, 280, 30);
        estilarInput(txtEmail);
        pnl.add(txtEmail);

        y += gap; // Separador visual
        JSeparator sep = new JSeparator();
        sep.setBounds(20, y + 10, 280, 10);
        pnl.add(sep);
        y += 20;

        pnl.add(crearLabel("Usuario:", 20, y));
        txtUsuario = new JTextField();
        txtUsuario.setBounds(20, y + 20, 280, 30);
        estilarInput(txtUsuario);
        pnl.add(txtUsuario);

        y += gap;
        pnl.add(crearLabel("Contraseña:", 20, y));
        txtContrasena = new JPasswordField();
        txtContrasena.setBounds(20, y + 20, 280, 30);
        estilarInput(txtContrasena);
        pnl.add(txtContrasena);

        y += gap;
        pnl.add(crearLabel("Rol:", 20, y));
        cmbRol = new JComboBox<>(new String[]{"ADMINISTRADOR", "TÉCNICO", "CAJERO", "VENTAS"});
        cmbRol.setBounds(20, y + 20, 130, 30);
        pnl.add(cmbRol);

        pnl.add(crearLabel("Estado:", 160, y));
        cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});
        cmbEstado.setBounds(160, y + 20, 140, 30);
        pnl.add(cmbEstado);

        // Botones
        y += 70;
        btnGuardar = new JButton("GUARDAR USUARIO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(20, y, 280, 40);
        btnGuardar.addActionListener(e -> accionGuardar());
        pnl.add(btnGuardar);

        y += 50;
        btnLimpiar = new JButton("Limpiar / Nuevo");
        estilarBotonSimple(btnLimpiar);
        btnLimpiar.setBounds(20, y, 135, 30);
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        pnl.add(btnLimpiar);

        btnEliminar = new JButton("Eliminar");
        estilarBoton(btnEliminar, new Color(239, 68, 68), Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setBounds(165, y, 135, 30);
        btnEliminar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Función Eliminar: Pendiente de conectar a DAO");
        });
        pnl.add(btnEliminar);

        return pnl;
    }

    // ========================================================================
    // --- TU LÓGICA ORIGINAL (RECUPERADA E INTEGRADA) ---
    // ========================================================================

    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            // Carga de datos de ejemplo (en un proyecto real, se consultaría a UsuarioDAO)
            // Asumimos columnas: 0=ID, 1=Nombre, 2=Rol, 3=User, 4=Estado
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            
            // Datos simulados (ya que no están en la tabla visible)
            txtDNI.setText("10203040"); 
            txtTelefono.setText("987654321");
            txtEmail.setText("usuario" + modelo.getValueAt(fila, 0).toString() + "@empresa.com");
            
            // Credenciales y Permisos
            txtUsuario.setText(modelo.getValueAt(fila, 3).toString());
            cmbRol.setSelectedItem(modelo.getValueAt(fila, 2).toString());
            cmbEstado.setSelectedItem(modelo.getValueAt(fila, 4).toString());
            txtContrasena.setText(""); // Por seguridad no se carga la pass
            
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
        
        // Simulación de guardado
        JOptionPane.showMessageDialog(this, "Usuario " + nombre + " guardado/actualizado.\nRol: " + cmbRol.getSelectedItem());
        
        // Si estábamos editando, aquí actualizaríamos el modelo. Si es nuevo, addRow.
        // Por simplicidad en la demo, recargamos todo:
        limpiarFormulario();
        cargarDatosSimulados(); 
    }

    private void cargarDatosSimulados() {
        modelo.setRowCount(0);
        // Datos simulados (ID, Nombre, Rol, Usuario, Estado)
        modelo.addRow(new Object[]{"1", "Carlos Mendoza", "ADMINISTRADOR", "cmendoza", "ACTIVO"});
        modelo.addRow(new Object[]{"2", "Ana Torres", "CAJERO", "atorres", "ACTIVO"});
        modelo.addRow(new Object[]{"3", "Luis Ramos", "TÉCNICO", "lramos", "ACTIVO"});
        modelo.addRow(new Object[]{"4", "Gerente General", "ADMINISTRADOR", "ggeneral", "INACTIVO"});
    }

    // --- UTILIDADES DE ESTILO (INCLUIDAS) ---
    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(x, y, 200, 20);
        return lbl;
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void estilarBotonSimple(JButton btn) {
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void estilarInput(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
}