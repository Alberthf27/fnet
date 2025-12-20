package vista;

import modelo.Empleado;
import DAO.UsuarioDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Panel para que el usuario logueado pueda ver y editar sus datos personales.
 */
public class subpanel_MiPerfil extends JPanel {

    private JTextField txtNombre, txtApellidos, txtDNI, txtTelefono, txtUsuario;
    private JPasswordField txtNuevaContrasena, txtConfirmarContrasena;
    private JLabel lblRol;
    private Empleado empleadoActual;

    public subpanel_MiPerfil() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));

        // Obtener empleado logueado
        if (Principal.instancia != null) {
            this.empleadoActual = Principal.instancia.getEmpleadoLogueado();
        }

        initUI();
        cargarDatos();
    }

    private void initUI() {
        // Panel Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        pnlHeader.setBackground(getBackground());
        JLabel lblTitulo = new JLabel("Mi Perfil");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblTitulo);
        add(pnlHeader, BorderLayout.NORTH);

        // Panel Central con Formulario
        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setBackground(Color.WHITE);
        pnlCentro.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;

        // Título de sección
        JLabel lblInfo = new JLabel("Información Personal");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfo.setForeground(new Color(37, 99, 235));
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        pnlCentro.add(lblInfo, gbc);
        fila++;

        gbc.gridwidth = 1;

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Nombres:"), gbc);
        txtNombre = crearTextField();
        gbc.gridx = 1;
        pnlCentro.add(txtNombre, gbc);
        fila++;

        // Apellidos
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Apellidos:"), gbc);
        txtApellidos = crearTextField();
        gbc.gridx = 1;
        pnlCentro.add(txtApellidos, gbc);
        fila++;

        // DNI (solo lectura)
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("DNI:"), gbc);
        txtDNI = crearTextField();
        txtDNI.setEditable(false);
        txtDNI.setBackground(new Color(241, 245, 249));
        gbc.gridx = 1;
        pnlCentro.add(txtDNI, gbc);
        fila++;

        // Teléfono
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Teléfono:"), gbc);
        txtTelefono = crearTextField();
        gbc.gridx = 1;
        pnlCentro.add(txtTelefono, gbc);
        fila++;

        // Rol (solo lectura)
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Rol:"), gbc);
        lblRol = new JLabel("---");
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRol.setForeground(new Color(22, 163, 74));
        gbc.gridx = 1;
        pnlCentro.add(lblRol, gbc);
        fila++;

        // Separador visual
        fila++;
        JSeparator sep = new JSeparator();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        pnlCentro.add(sep, gbc);
        fila++;

        // Sección de contraseña
        JLabel lblPass = new JLabel("Cambiar Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPass.setForeground(new Color(37, 99, 235));
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        pnlCentro.add(lblPass, gbc);
        fila++;

        gbc.gridwidth = 1;

        // Nueva contraseña
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Nueva Contraseña:"), gbc);
        txtNuevaContrasena = new JPasswordField();
        txtNuevaContrasena.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        pnlCentro.add(txtNuevaContrasena, gbc);
        fila++;

        // Confirmar contraseña
        gbc.gridx = 0;
        gbc.gridy = fila;
        pnlCentro.add(crearLabel("Confirmar Contraseña:"), gbc);
        txtConfirmarContrasena = new JPasswordField();
        txtConfirmarContrasena.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        pnlCentro.add(txtConfirmarContrasena, gbc);
        fila++;

        // Botón Guardar
        fila++;
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        btnGuardar.setBackground(new Color(37, 99, 235));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(200, 45));
        btnGuardar.addActionListener(e -> guardarCambios());

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        pnlCentro.add(btnGuardar, gbc);

        // Scroll wrapper
        JScrollPane scroll = new JScrollPane(pnlCentro);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void cargarDatos() {
        if (empleadoActual != null) {
            txtNombre.setText(empleadoActual.getNombres() != null ? empleadoActual.getNombres() : "");
            txtApellidos.setText(empleadoActual.getApellidos() != null ? empleadoActual.getApellidos() : "");
            txtDNI.setText(empleadoActual.getDni() != null ? empleadoActual.getDni() : "---");
            txtTelefono.setText(empleadoActual.getTelefono() != null ? empleadoActual.getTelefono() : "");
            lblRol.setText(empleadoActual.getCargo() != null ? empleadoActual.getCargo().toUpperCase() : "---");
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la información del usuario.",
                    "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardarCambios() {
        if (empleadoActual == null) {
            JOptionPane.showMessageDialog(this, "No hay usuario cargado.");
            return;
        }

        String nombres = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String nuevaPass = new String(txtNuevaContrasena.getPassword());
        String confirmarPass = new String(txtConfirmarContrasena.getPassword());

        // Validaciones básicas
        if (nombres.isEmpty() || apellidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y apellidos son obligatorios.");
            return;
        }

        // Validar contraseñas si se ingresó una nueva
        if (!nuevaPass.isEmpty()) {
            if (!nuevaPass.equals(confirmarPass)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
                return;
            }
            if (nuevaPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 4 caracteres.");
                return;
            }
        }

        // Guardar cambios (TODO: Conectar con UsuarioDAO.actualizarPerfil())
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Guardar los cambios en tu perfil?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Actualizar objeto local
            empleadoActual.setNombres(nombres);
            empleadoActual.setApellidos(apellidos);
            empleadoActual.setTelefono(telefono);

            // TODO: Llamar a DAO para persistir cambios
            // new UsuarioDAO().actualizarPerfil(empleadoActual, nuevaPass.isEmpty() ? null
            // : nuevaPass);

            JOptionPane.showMessageDialog(this,
                    "Cambios guardados correctamente.\n(Nota: Requiere implementar UsuarioDAO.actualizarPerfil)");

            // Limpiar campos de contraseña
            txtNuevaContrasena.setText("");
            txtConfirmarContrasena.setText("");
        }
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(250, 35));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return txt;
    }
}
