package vista;

import DAO.ClienteDAO;
import DAO.ServicioDAO;
import modelo.Cliente;
import modelo.Servicio;
import java.awt.Color;
import java.awt.Font;
import java.awt.Cursor;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FormularioCliente extends JDialog {

    private JTextField txtDni, txtNombres, txtApellidos, txtDireccion, txtTelefono, txtCorreo;
    private JCheckBox chkContrato;
    private JComboBox<Servicio> cmbPlanes;
    private boolean resultado = false;
    private Cliente clienteEdicion = null;

    public FormularioCliente(java.awt.Frame parent, Cliente clienteAEditar) {
        super(parent, true); // Modal
        this.clienteEdicion = clienteAEditar;
        
        setTitle(clienteAEditar == null ? "Nuevo Cliente" : "Editar Cliente");
        setSize(500, 650); // Un poco más alto
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        
        // Cargar datos en hilo aparte si es edición (aunque es rápido, es buena práctica)
        if (clienteAEditar != null) cargarDatos();
    }

    private void initUI() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        JLabel lblTitulo = new JLabel(clienteEdicion == null ? "Registrar Persona" : "Actualizar Datos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 10, 300, 30);
        panel.add(lblTitulo);

        int y = 60;
        
        panel.add(crearLabel("DNI / Cédula:", 30, y));
        txtDni = crearInput(30, y + 25, 200);
        panel.add(txtDni);
        
        panel.add(crearLabel("Teléfono:", 250, y));
        txtTelefono = crearInput(250, y + 25, 200);
        panel.add(txtTelefono);
        y += 70;

        panel.add(crearLabel("Nombres:", 30, y));
        txtNombres = crearInput(30, y + 25, 420);
        panel.add(txtNombres);
        y += 70;

        panel.add(crearLabel("Apellidos:", 30, y));
        txtApellidos = crearInput(30, y + 25, 420);
        panel.add(txtApellidos);
        y += 70;

        panel.add(crearLabel("Dirección:", 30, y));
        txtDireccion = crearInput(30, y + 25, 420);
        panel.add(txtDireccion);
        y += 70;
        
        panel.add(crearLabel("Correo (Opcional):", 30, y));
        txtCorreo = crearInput(30, y + 25, 420);
        panel.add(txtCorreo);
        y += 70;

        // SECCIÓN CONTRATO (Solo para nuevos)
        if (clienteEdicion == null) {
            JPanel panelContrato = new JPanel(null);
            panelContrato.setBackground(new Color(241, 245, 249));
            panelContrato.setBounds(30, y, 420, 80);
            panelContrato.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            panel.add(panelContrato);

            chkContrato = new JCheckBox("¿Crear contrato ahora?");
            chkContrato.setFont(new Font("Segoe UI", Font.BOLD, 13));
            chkContrato.setBackground(new Color(241, 245, 249));
            chkContrato.setBounds(10, 10, 300, 20);
            panelContrato.add(chkContrato);

            JLabel lblPlan = new JLabel("Plan:");
            lblPlan.setBounds(10, 45, 50, 20);
            panelContrato.add(lblPlan);

            cmbPlanes = new JComboBox<>();
            cmbPlanes.setBounds(60, 42, 340, 30);
            cmbPlanes.setEnabled(false);
            panelContrato.add(cmbPlanes);

            chkContrato.addActionListener(e -> cmbPlanes.setEnabled(chkContrato.isSelected()));

            // Cargar planes
            cargarPlanes();
            y += 90;
        }

        JButton btnGuardar = new JButton("💾 GUARDAR");
        estilarBoton(btnGuardar, new Color(22, 163, 74), Color.WHITE);
        btnGuardar.setBounds(250, 520, 200, 45);
        
        // ACCIÓN DE GUARDAR CON HILO (Velocidad y Barra de carga)
        btnGuardar.addActionListener(e -> procesarGuardado());
        
        panel.add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        estilarBoton(btnCancelar, new Color(203, 213, 225), Color.BLACK);
        btnCancelar.setBounds(30, 520, 150, 45);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);
    }
    
    private void cargarPlanes() {
        // Cargar combos no suele demorar, pero si quieres perfección, úsalo en hilo también
        new Thread(() -> {
            ServicioDAO dao = new ServicioDAO();
            List<Servicio> servicios = dao.obtenerServiciosActivos();
            SwingUtilities.invokeLater(() -> {
                for(Servicio s : servicios) {
                    cmbPlanes.addItem(s);
                }
            });
        }).start();
    }

    private void procesarGuardado() {
        if (txtDni.getText().isEmpty() || txtNombres.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "DNI y Nombres obligatorios.");
            return;
        }

        // 1. Activar barra en Principal (si existe)
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        // 2. Ejecutar guardado en segundo plano
        new Thread(() -> {
            try {
                boolean exito = guardarEnBD(); // Llamamos a la lógica pesada
                
                // 3. Volver a la interfaz
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    
                    if (exito) {
                        JOptionPane.showMessageDialog(this, "Datos guardados correctamente.");
                        resultado = true;
                        dispose(); // Cerrar ventana
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar en BD.");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                     if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                     setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                });
            }
        }).start();
    }

    private boolean guardarEnBD() {
        Cliente c = clienteEdicion != null ? clienteEdicion : new Cliente();
        c.setDniCliente(txtDni.getText());
        c.setNombres(txtNombres.getText());
        c.setApellidos(txtApellidos.getText());
        c.setDireccion(txtDireccion.getText());
        c.setTelefono(txtTelefono.getText());
        c.setCorreo(txtCorreo.getText());

        ClienteDAO dao = new ClienteDAO();
        if (clienteEdicion == null) {
            boolean conContrato = chkContrato.isSelected();
            int idServicio = 0;
            if (conContrato) {
                Servicio s = (Servicio) cmbPlanes.getSelectedItem();
                if(s != null) idServicio = s.getIdServicio();
            }
            return dao.registrarClienteCompleto(c, idServicio, conContrato);
        } else {
            return dao.actualizarCliente(c);
        }
    }
    
    private void cargarDatos() {
        txtDni.setText(clienteEdicion.getDniCliente());
        txtNombres.setText(clienteEdicion.getNombres());
        txtApellidos.setText(clienteEdicion.getApellidos());
        txtDireccion.setText(clienteEdicion.getDireccion());
        txtTelefono.setText(clienteEdicion.getTelefono());
        txtCorreo.setText(clienteEdicion.getCorreo());
        txtDni.setEditable(false);
    }

    public boolean isGuardado() { return resultado; }

    // Helpers visuales
    private JLabel crearLabel(String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(Color.GRAY);
        l.setBounds(x, y, 200, 20);
        return l;
    }
    private JTextField crearInput(int x, int y, int w) {
        JTextField t = new JTextField();
        t.setBounds(x, y, w, 30);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return t;
    }
    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
    }
}