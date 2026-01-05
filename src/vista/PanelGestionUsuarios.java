package vista;

import DAO.PermisoDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel para gestión de usuarios y permisos.
 * Solo visible para el rol GERENTE.
 */
public class PanelGestionUsuarios extends JPanel {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private PermisoDAO permisoDAO;
    private JComboBox<String> cmbRoles;
    private List<Object[]> listaRoles;

    public PanelGestionUsuarios() {
        permisoDAO = new PermisoDAO();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
        cargarUsuarios();
    }

    private void initUI() {
        // === PANEL SUPERIOR ===
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestion de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(37, 99, 235));
        panelTop.add(lblTitulo, BorderLayout.WEST);

        JButton btnNuevo = new JButton("+ Nuevo Usuario");
        btnNuevo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNuevo.setBackground(new Color(37, 99, 235));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> abrirDialogoNuevo());
        panelTop.add(btnNuevo, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);

        // === TABLA DE USUARIOS ===
        String[] columnas = { "ID", "Nombre", "DNI", "Codigo", "Rol", "Estado", "id_rol" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setRowHeight(32);
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ocultar columnas internas
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(6).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(6).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scroll, BorderLayout.CENTER);

        // === PANEL DE ACCIONES ===
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelAcciones.setBackground(new Color(249, 250, 251));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones"));

        // Combo de roles
        cmbRoles = new JComboBox<>();
        cargarRoles();
        panelAcciones.add(new JLabel("Cambiar rol a:"));
        panelAcciones.add(cmbRoles);

        JButton btnCambiarRol = new JButton("Aplicar");
        btnCambiarRol.addActionListener(e -> cambiarRol());
        panelAcciones.add(btnCambiarRol);

        panelAcciones.add(Box.createHorizontalStrut(20));

        JButton btnActivar = new JButton("Activar");
        btnActivar.setBackground(new Color(22, 163, 74));
        btnActivar.setForeground(Color.WHITE);
        btnActivar.addActionListener(e -> cambiarEstado(true));
        panelAcciones.add(btnActivar);

        JButton btnDesactivar = new JButton("Desactivar");
        btnDesactivar.setBackground(new Color(239, 68, 68));
        btnDesactivar.setForeground(Color.WHITE);
        btnDesactivar.addActionListener(e -> cambiarEstado(false));
        panelAcciones.add(btnDesactivar);

        panelAcciones.add(Box.createHorizontalStrut(20));

        JButton btnResetPass = new JButton("Reset Password");
        btnResetPass.addActionListener(e -> resetearPassword());
        panelAcciones.add(btnResetPass);

        add(panelAcciones, BorderLayout.SOUTH);
    }

    private void cargarRoles() {
        listaRoles = permisoDAO.listarRoles();
        cmbRoles.removeAllItems();
        for (Object[] rol : listaRoles) {
            cmbRoles.addItem((String) rol[1]);
        }
    }

    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        List<Object[]> usuarios = permisoDAO.listarUsuarios();
        for (Object[] u : usuarios) {
            modeloTabla.addRow(new Object[] {
                    u[0], // id_usuario
                    u[2], // nombre completo
                    u[3], // dni
                    u[4], // codigo_acceso
                    u[5], // rol
                    u[7], // estado
                    u[6] // id_rol (oculto)
            });
        }
    }

    private void abrirDialogoNuevo() {
        JTextField txtNombres = new JTextField(15);
        JTextField txtApellidos = new JTextField(15);
        JTextField txtDni = new JTextField(8);
        JTextField txtCodigo = new JTextField(4);
        JPasswordField txtPass = new JPasswordField(10);
        JComboBox<String> cmbRol = new JComboBox<>();
        for (Object[] rol : listaRoles) {
            cmbRol.addItem((String) rol[1]);
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Nombres:"));
        panel.add(txtNombres);
        panel.add(new JLabel("Apellidos:"));
        panel.add(txtApellidos);
        panel.add(new JLabel("DNI:"));
        panel.add(txtDni);
        panel.add(new JLabel("Codigo Acceso:"));
        panel.add(txtCodigo);
        panel.add(new JLabel("Contrasena:"));
        panel.add(txtPass);
        panel.add(new JLabel("Rol:"));
        panel.add(cmbRol);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String nombres = txtNombres.getText().trim();
            String apellidos = txtApellidos.getText().trim();
            String dni = txtDni.getText().trim();
            String codigo = txtCodigo.getText().trim();
            String pass = new String(txtPass.getPassword());
            int idRol = (int) listaRoles.get(cmbRol.getSelectedIndex())[0];

            if (nombres.isEmpty() || apellidos.isEmpty() || codigo.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios.");
                return;
            }

            boolean exito = permisoDAO.crearUsuario(nombres, apellidos, dni, codigo, pass, idRol);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear usuario.");
            }
        }
    }

    private void cambiarRol() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.");
            return;
        }

        int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
        int nuevoRol = (int) listaRoles.get(cmbRoles.getSelectedIndex())[0];

        if (permisoDAO.actualizarRolUsuario(idUsuario, nuevoRol)) {
            JOptionPane.showMessageDialog(this, "Rol actualizado.");
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar rol.");
        }
    }

    private void cambiarEstado(boolean activo) {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.");
            return;
        }

        int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
        String accion = activo ? "activar" : "desactivar";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Desea " + accion + " este usuario?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (permisoDAO.cambiarEstadoUsuario(idUsuario, activo)) {
                JOptionPane.showMessageDialog(this, "Usuario " + (activo ? "activado" : "desactivado") + ".");
                cargarUsuarios();
            }
        }
    }

    private void resetearPassword() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario.");
            return;
        }

        int idUsuario = (int) modeloTabla.getValueAt(fila, 0);
        String nuevaPass = JOptionPane.showInputDialog(this, "Nueva contrasena:");

        if (nuevaPass != null && !nuevaPass.trim().isEmpty()) {
            if (permisoDAO.resetearContrasena(idUsuario, nuevaPass)) {
                JOptionPane.showMessageDialog(this, "Contrasena actualizada.");
            }
        }
    }
}
