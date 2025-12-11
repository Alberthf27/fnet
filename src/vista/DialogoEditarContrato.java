package vista;

import DAO.SuscripcionDAO;
import DAO.ServicioDAO;
import modelo.Servicio;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DialogoEditarContrato extends JDialog {

    private JComboBox<Servicio> cmbPlanes;
    private JTextField txtDireccion, txtDniNuevo, txtNombreNuevo;
    private JButton btnBuscarDni;
    private int idSuscripcion;
    private int idClienteActual; // El dueño original
    private int idNuevoClienteSeleccionado = -1; // -1 significa "no cambiar dueño"
    private boolean guardado = false;

    public DialogoEditarContrato(java.awt.Frame parent, int idSuscripcion, String planActual, String dirActual, int idClienteOriginal, String nombreClienteOriginal) {
        super(parent, true);
        this.idSuscripcion = idSuscripcion;
        this.idClienteActual = idClienteOriginal;
        this.idNuevoClienteSeleccionado = idClienteOriginal; // Por defecto es el mismo

        setTitle("Administrar Contrato #" + idSuscripcion);
        setSize(500, 520); // Más grande para las nuevas opciones
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI(planActual, dirActual, nombreClienteOriginal);
        
        // Cargar datos (activando la barra del principal)
        iniciarCargaDatos(planActual);
    }

    private void initUI(String planActual, String dirActual, String nombreCliente) {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        JLabel lblTitulo = new JLabel("Modificación de Contrato");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(20, 10, 300, 30);
        panel.add(lblTitulo);

        // --- SECCIÓN 1: SERVICIO Y DIRECCIÓN ---
        int y = 60;
        panel.add(crearLabel("Plan de Servicio:", 20, y));
        cmbPlanes = new JComboBox<>();
        cmbPlanes.setBounds(20, y + 25, 440, 35);
        panel.add(cmbPlanes);
        y += 75;

        panel.add(crearLabel("Dirección de Instalación:", 20, y));
        txtDireccion = new JTextField(dirActual);
        txtDireccion.setBounds(20, y + 25, 440, 35);
        txtDireccion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDireccion);
        y += 85;

        // --- SECCIÓN 2: CAMBIO DE TITULAR ---
        JPanel panelTitular = new JPanel(null);
        panelTitular.setBackground(new Color(248, 250, 252));
        panelTitular.setBorder(BorderFactory.createTitledBorder(null, "Cambio de Titular (Opcional)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12)));
        panelTitular.setBounds(20, y, 440, 120);
        panel.add(panelTitular);

        JLabel lblInfo = new JLabel("Titular Actual: " + nombreCliente);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBounds(15, 20, 400, 20);
        panelTitular.add(lblInfo);

        JLabel lblDni = new JLabel("Buscar Nuevo DNI:");
        lblDni.setBounds(15, 45, 120, 20);
        panelTitular.add(lblDni);

        txtDniNuevo = new JTextField();
        txtDniNuevo.setBounds(15, 70, 120, 30);
        panelTitular.add(txtDniNuevo);

        btnBuscarDni = new JButton("🔍");
        btnBuscarDni.setBounds(140, 70, 50, 30);
        btnBuscarDni.addActionListener(e -> buscarCliente());
        panelTitular.add(btnBuscarDni);

        txtNombreNuevo = new JTextField("Sin cambios...");
        txtNombreNuevo.setEditable(false);
        txtNombreNuevo.setBackground(Color.WHITE);
        txtNombreNuevo.setBounds(200, 70, 220, 30);
        panelTitular.add(txtNombreNuevo);

        // --- BOTONES ---
        JButton btnGuardar = new JButton("💾 GUARDAR CAMBIOS");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(130, 420, 220, 45);
        btnGuardar.addActionListener(e -> guardar());
        panel.add(btnGuardar);
    }

    private void iniciarCargaDatos(String planActualNombre) {
        // 1. Activar barra en Principal
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        cmbPlanes.setEnabled(false); // Bloquear mientras carga
        
        new Thread(() -> {
            ServicioDAO dao = new ServicioDAO();
            List<Servicio> lista = dao.obtenerServiciosActivos();
            
            SwingUtilities.invokeLater(() -> {
                for (Servicio s : lista) {
                    cmbPlanes.addItem(s);
                    if (s.getDescripcion().equals(planActualNombre) || s.toString().contains(planActualNombre)) {
                        cmbPlanes.setSelectedItem(s);
                    }
                }
                cmbPlanes.setEnabled(true); // Desbloquear
                // 2. Apagar barra
                if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
            });
        }).start();
    }

    private void buscarCliente() {
        String dni = txtDniNuevo.getText().trim();
        if (dni.isEmpty()) return;

        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            SuscripcionDAO dao = new SuscripcionDAO();
            String[] datos = dao.buscarClientePorDni(dni);
            
            SwingUtilities.invokeLater(() -> {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (datos != null) {
                    idNuevoClienteSeleccionado = Integer.parseInt(datos[0]);
                    txtNombreNuevo.setText(datos[1]);
                    txtNombreNuevo.setForeground(new Color(22, 163, 74)); // Verde
                } else {
                    idNuevoClienteSeleccionado = idClienteActual; // Reset
                    txtNombreNuevo.setText("Cliente no encontrado");
                    txtNombreNuevo.setForeground(Color.RED);
                }
            });
        }).start();
    }

    private void guardar() {
        Servicio s = (Servicio) cmbPlanes.getSelectedItem();
        String dir = txtDireccion.getText();

        if (s == null || dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Plan y Dirección son obligatorios.");
            return;
        }

        // Bloquear UI y Mostrar Carga
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        setVisible(false); // Ocultar para que se vea la barra del principal

        new Thread(() -> {
            SuscripcionDAO dao = new SuscripcionDAO();
            boolean exito = dao.actualizarContratoCompleto(idSuscripcion, s.getIdServicio(), dir, idNuevoClienteSeleccionado);
            
            SwingUtilities.invokeLater(() -> {
                if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                
                if (exito) {
                    JOptionPane.showMessageDialog(null, "Contrato actualizado correctamente.");
                    guardado = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al guardar.");
                    setVisible(true); // Reabrir si falló
                }
            });
        }).start();
    }

    public boolean isGuardado() { return guardado; }

    private JLabel crearLabel(String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBounds(x, y, 200, 20);
        return l;
    }
    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }
}