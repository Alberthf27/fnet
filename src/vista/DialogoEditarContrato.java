package vista;

import DAO.SuscripcionDAO;
import DAO.ClienteDAO; // Necesario para crear clientes
import modelo.Servicio;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DialogoEditarContrato extends JDialog {

    private JComboBox<Servicio> cmbPlanes;
    private JTextField txtDireccion;
    
    // CAMPOS DE CLIENTE
    private JTextField txtDniCli, txtNombresCli, txtApellidosCli, txtCelularCli;
    private JButton btnBuscarDni;
    
    private JDateChooser dateInicio; 
    private JSpinner spinDiaPago;

    private int idSuscripcion;
    private int idClienteActual; 
    private int idClienteSeleccionado = -1; // ID final a usar
    private boolean guardado = false;
    private boolean esNuevoCliente = false; // Bandera para saber si insertamos

    public DialogoEditarContrato(java.awt.Frame parent, int idSuscripcion, 
                                 String nombrePlanActual, String dirActual, 
                                 int idClienteOriginal, String nombreClienteOriginal,
                                 List<Servicio> planesCache, 
                                 Date fechaInicioActual,     
                                 int diaPagoActual) {        
        super(parent, true);
        this.idSuscripcion = idSuscripcion;
        this.idClienteActual = idClienteOriginal;
        this.idClienteSeleccionado = idClienteOriginal;

        setTitle(idSuscripcion == -1 ? "Nuevo Contrato" : "Editar Contrato #" + idSuscripcion);
        setSize(550, 680); // Más alto para el formulario de cliente
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI(nombrePlanActual, dirActual, nombreClienteOriginal, planesCache, fechaInicioActual, diaPagoActual);
        
        // Si es editar, cargamos los datos del cliente actual (simulado)
        if (idSuscripcion != -1 && nombreClienteOriginal != null) {
            llenarDatosClienteExistente(); 
        }
    }

    private void initUI(String planActual, String dirActual, String nombreCliente, 
                        List<Servicio> planesCache, Date fechaInicio, int diaPago) {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        JLabel lblTitulo = new JLabel(idSuscripcion == -1 ? "Registrar Nuevo Contrato" : "Modificar Contrato");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(20, 10, 300, 30);
        panel.add(lblTitulo);

        int y = 60;

        // --- 1. PLAN DE SERVICIO ---
        panel.add(crearLabel("Plan de Servicio:", 20, y));
        cmbPlanes = new JComboBox<>();
        if (planesCache != null) {
            for (Servicio s : planesCache) {
                cmbPlanes.addItem(s);
                if (planActual != null && s.getNombre().equalsIgnoreCase(planActual)) {
                    cmbPlanes.setSelectedItem(s);
                }
            }
        }
        cmbPlanes.setBounds(20, y + 25, 480, 35);
        panel.add(cmbPlanes);
        y += 75;

        // --- 2. FECHAS (CON SINCRONIZACIÓN AUTOMÁTICA) ---
        panel.add(crearLabel("Fecha Inicio:", 20, y));
        dateInicio = new JDateChooser();
        dateInicio.setDate(fechaInicio != null ? fechaInicio : new Date());
        dateInicio.setDateFormatString("dd/MM/yyyy");
        dateInicio.setBounds(20, y + 25, 200, 35);
        
        // LISTENER MAGICO: Al cambiar fecha, cambia el día de pago
        dateInicio.getDateEditor().addPropertyChangeListener(
            new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    if ("date".equals(e.getPropertyName())) {
                        Date nuevaFecha = dateInicio.getDate();
                        if (nuevaFecha != null) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(nuevaFecha);
                            int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
                            spinDiaPago.setValue(dia); // Actualiza spinner
                        }
                    }
                }
            });
        
        panel.add(dateInicio);

        panel.add(crearLabel("Día de Pago:", 250, y));
        spinDiaPago = new JSpinner(new SpinnerNumberModel(diaPago > 0 ? diaPago : 1, 1, 31, 1));
        spinDiaPago.setBounds(250, y + 25, 100, 35);
        spinDiaPago.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(spinDiaPago);
        y += 75;

        // --- 3. DIRECCIÓN ---
        panel.add(crearLabel("Dirección de Instalación:", 20, y));
        txtDireccion = new JTextField(dirActual);
        txtDireccion.setBounds(20, y + 25, 480, 35);
        txtDireccion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDireccion);
        y += 85;

        // --- 4. DATOS DEL CLIENTE (FORMULARIO INTELIGENTE) ---
        JPanel panelCli = new JPanel(null);
        panelCli.setBackground(new Color(248, 250, 252));
        panelCli.setBorder(BorderFactory.createTitledBorder(null, "Datos del Titular", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12)));
        panelCli.setBounds(20, y, 480, 190); // Más alto
        panel.add(panelCli);

        // Fila 1: DNI y Botón
        JLabel lblDni = new JLabel("DNI:");
        lblDni.setBounds(15, 25, 50, 20);
        panelCli.add(lblDni);

        txtDniCli = new JTextField();
        txtDniCli.setBounds(50, 20, 120, 30);
        panelCli.add(txtDniCli);

        btnBuscarDni = new JButton("🔍");
        btnBuscarDni.setBounds(180, 20, 50, 30);
        btnBuscarDni.setToolTipText("Buscar o Validar DNI");
        btnBuscarDni.addActionListener(e -> buscarOActivarFormulario());
        panelCli.add(btnBuscarDni);
        
        JLabel lblNota = new JLabel("<html><font color='gray' size='2'>* Si no existe, ingrese los datos.</font></html>");
        lblNota.setBounds(240, 20, 200, 30);
        panelCli.add(lblNota);

        // Fila 2: Nombres y Apellidos
        JLabel lblNom = new JLabel("Nombres:");
        lblNom.setBounds(15, 65, 80, 20);
        panelCli.add(lblNom);
        
        txtNombresCli = new JTextField();
        txtNombresCli.setBounds(15, 85, 210, 30);
        txtNombresCli.setEnabled(false); // Bloqueado por defecto
        panelCli.add(txtNombresCli);
        
        JLabel lblApe = new JLabel("Apellidos:");
        lblApe.setBounds(240, 65, 80, 20);
        panelCli.add(lblApe);
        
        txtApellidosCli = new JTextField();
        txtApellidosCli.setBounds(240, 85, 220, 30);
        txtApellidosCli.setEnabled(false);
        panelCli.add(txtApellidosCli);

        // Fila 3: Celular
        JLabel lblCel = new JLabel("Celular / Telf:");
        lblCel.setBounds(15, 125, 100, 20);
        panelCli.add(lblCel);
        
        txtCelularCli = new JTextField();
        txtCelularCli.setBounds(15, 145, 150, 30);
        txtCelularCli.setEnabled(false);
        panelCli.add(txtCelularCli);

        // --- BOTONES ---
        JButton btnGuardar = new JButton("💾 GUARDAR CONTRATO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(150, 570, 220, 45);
        btnGuardar.addActionListener(e -> guardar());
        panel.add(btnGuardar);
        
        // Trigger inicial del listener de fecha para setear día hoy
        if (fechaInicio != null) {
             java.util.Calendar cal = java.util.Calendar.getInstance();
             cal.setTime(fechaInicio);
             spinDiaPago.setValue(cal.get(java.util.Calendar.DAY_OF_MONTH));
        }
    }

    private void buscarOActivarFormulario() {
        String dni = txtDniCli.getText().trim();
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un DNI.");
            return;
        }

        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            SuscripcionDAO dao = new SuscripcionDAO();
            // Usamos buscarClientePorDni que devuelve [id, nombres+apellidos]
            // Lo ideal sería que tu DAO devolviera objeto Cliente completo, 
            // pero para no cambiar todo tu DAO, haremos un truco.
            
            String[] datos = dao.buscarClientePorDni(dni); 
            
            SwingUtilities.invokeLater(() -> {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (datos != null) {
                    // --- CLIENTE EXISTE ---
                    esNuevoCliente = false;
                    idClienteSeleccionado = Integer.parseInt(datos[0]);
                    
                    // Como el DAO actual concatena nombre+apellido, lo ponemos todo en nombre
                    // y bloqueamos para que no edite
                    txtNombresCli.setText(datos[1]); 
                    txtApellidosCli.setText(""); // Ya viene concatenado
                    txtCelularCli.setText("---"); // No lo trajimos del DAO
                    
                    bloquearCamposCliente(true);
                    txtNombresCli.setBackground(new Color(220, 255, 220)); // Verde claro
                } else {
                    // --- CLIENTE NO EXISTE (NUEVO) ---
                    esNuevoCliente = true;
                    idClienteSeleccionado = -1;
                    
                    // Limpiamos y habilitamos para que escriba
                    txtNombresCli.setText("");
                    txtApellidosCli.setText("");
                    txtCelularCli.setText("");
                    
                    bloquearCamposCliente(false);
                    txtNombresCli.requestFocus();
                    JOptionPane.showMessageDialog(this, "Cliente nuevo. Por favor complete los datos.");
                }
            });
        }).start();
    }
    
    private void bloquearCamposCliente(boolean bloquear) {
        txtNombresCli.setEnabled(!bloquear);
        txtApellidosCli.setEnabled(!bloquear);
        txtCelularCli.setEnabled(!bloquear);
    }

    private void guardar() {
        Servicio s = (Servicio) cmbPlanes.getSelectedItem();
        String dir = txtDireccion.getText();
        Date fechaInicio = dateInicio.getDate();
        int diaPago = (int) spinDiaPago.getValue();

        // 1. Validaciones Básicas
        if (s == null || dir.isEmpty() || fechaInicio == null) {
            JOptionPane.showMessageDialog(this, "Plan, Dirección y Fecha Inicio son obligatorios.");
            return;
        }
        
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        setVisible(false);

        new Thread(() -> {
            // 2. Lógica de Cliente (Existente o Nuevo)
            int idFinalCliente = idClienteSeleccionado;

            if (esNuevoCliente) {
                // Registrar Cliente Nuevo "al vuelo"
                String dni = txtDniCli.getText().trim();
                String nom = txtNombresCli.getText().trim();
                String ape = txtApellidosCli.getText().trim();
                String tel = txtCelularCli.getText().trim();
                
                if (dni.isEmpty() || nom.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "Para cliente nuevo, DNI y Nombre son obligatorios.");
                    });
                    return; // Abortar
                }

                ClienteDAO cliDao = new ClienteDAO();
                // Llamamos al método nuevo que creaste en el paso 1
                idFinalCliente = cliDao.registrarClienteYObtenerId(dni, nom, ape, dir, tel);
                
                if (idFinalCliente == -1) {
                    SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "Error al registrar el cliente nuevo.");
                    });
                    return; // Abortar
                }
            } else {
                // Si no se buscó nada y era nuevo contrato
                if (idSuscripcion == -1 && idFinalCliente == -1) {
                     SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "Busque un cliente o registre uno nuevo.");
                    });
                    return;
                }
            }

            // 3. Guardar el Contrato
            SuscripcionDAO susDao = new SuscripcionDAO();
            boolean exito = susDao.guardarOActualizarContrato(
                idSuscripcion, s.getIdServicio(), dir, idFinalCliente, fechaInicio, diaPago
            );
            
            SwingUtilities.invokeLater(() -> {
                if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                if (exito) {
                    JOptionPane.showMessageDialog(null, "Contrato guardado exitosamente.");
                    guardado = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al guardar contrato.");
                    setVisible(true);
                }
            });
        }).start();
    }
    
    // Método auxiliar para rellenar visualmente si es edición
    private void llenarDatosClienteExistente() {
        // Como tu suscripción actual solo trae "Nombre Completo" y no DNI separado
        // en el objeto suscripción simple, dejaremos el DNI vacío para que lo busquen
        // o lo puedes traer haciendo una consulta extra si quieres perfección.
        // Por ahora, solo mostramos el nombre en el campo bloqueado.
        
        // NOTA: Si quisieras traer el DNI, tendrías que hacer una query extra en el init.
        // Aquí asumiremos que el usuario busca el DNI si quiere cambiarlo.
        txtNombresCli.setText("(Cliente Actual Seleccionado)"); 
        txtNombresCli.setEnabled(false);
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