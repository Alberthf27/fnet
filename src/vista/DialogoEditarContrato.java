package vista;

import DAO.SuscripcionDAO;
import DAO.ClienteDAO;
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

    // --- NUEVOS CONTROLES ---
    private JCheckBox chkMesAdelantado;
    private JCheckBox chkEquiposPrestados;
    private JTextField txtGarantia;
    private JTextField txtNombreSuscripcion; // Nombre del contrato
    // ------------------------

    // CAMPOS DE CLIENTE
    private JTextField txtDniCli, txtNombresCli, txtApellidosCli, txtCelularCli;
    private JButton btnBuscarDni;

    private JDateChooser dateInicio;
    private JSpinner spinDiaPago;

    private int idSuscripcion;
    private int idClienteActual;
    private int idClienteSeleccionado = -1;
    private boolean guardado = false;
    private boolean esNuevoCliente = false;

    public DialogoEditarContrato(java.awt.Frame parent, int idSuscripcion,
            String nombrePlanActual, String dirActual,
            int idClienteOriginal, String nombreClienteOriginal,
            List<Servicio> planesCache,
            Date fechaInicioActual,
            int diaPagoActual,
            boolean mesAdelantado, // Condición del contrato
            boolean equiposPrestados, // Condición del contrato
            double garantia,
            String nombreSuscripcion) { // NUEVO: Nombre del contrato
        super(parent, true);
        this.idSuscripcion = idSuscripcion;
        this.idClienteActual = idClienteOriginal;
        this.idClienteSeleccionado = idClienteOriginal;

        setTitle(idSuscripcion == -1 ? "Nuevo Contrato" : "Editar Contrato #" + idSuscripcion);
        setSize(550, 800); // Aumentamos altura para el nuevo campo
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI(nombrePlanActual, dirActual, nombreClienteOriginal, planesCache, fechaInicioActual, diaPagoActual,
                mesAdelantado, equiposPrestados, garantia, nombreSuscripcion);

        if (idSuscripcion != -1 && nombreClienteOriginal != null) {
            llenarDatosClienteExistente();
        }
    }

    private void initUI(String planActual, String dirActual, String nombreCliente,
            List<Servicio> planesCache, Date fechaInicio, int diaPago,
            boolean mesAdelantado, boolean equiposPrestados, double garantia,
            String nombreSuscripcion) {
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

        // --- 0. NOMBRE DE SUSCRIPCIÓN (NUEVO) ---
        panel.add(crearLabel("Nombre del Contrato:", 20, y));
        txtNombreSuscripcion = new JTextField(nombreSuscripcion != null ? nombreSuscripcion : nombreCliente);
        txtNombreSuscripcion.setBounds(20, y + 25, 480, 35);
        txtNombreSuscripcion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtNombreSuscripcion.setToolTipText("Por defecto usa el nombre del cliente. Puede personalizarlo.");
        panel.add(txtNombreSuscripcion);
        y += 75;

        // --- 1. PLAN DE SERVICIO ---
        panel.add(crearLabel("Plan de Servicio:", 20, y));
        cmbPlanes = new JComboBox<>();
        int indicePorDefecto = 0; // Indice del plan por defecto (Fibra 50MB)
        if (planesCache != null) {
            for (int i = 0; i < planesCache.size(); i++) {
                Servicio s = planesCache.get(i);
                cmbPlanes.addItem(s);
                // Si hay plan actual seleccionado (edicion), usarlo
                if (planActual != null && !planActual.isEmpty() && s.getNombre().equalsIgnoreCase(planActual)) {
                    cmbPlanes.setSelectedItem(s);
                }
                // Para nuevos contratos, buscar "Fibra 50" o "50MB" como defecto
                if ((planActual == null || planActual.isEmpty()) &&
                        (s.getNombre().toUpperCase().contains("50") ||
                                s.getNombre().toUpperCase().contains("FIBRA 50"))) {
                    indicePorDefecto = i;
                }
            }
            // Si es nuevo contrato y no se selecciono ninguno, usar el por defecto
            if ((planActual == null || planActual.isEmpty()) && cmbPlanes.getItemCount() > indicePorDefecto) {
                cmbPlanes.setSelectedIndex(indicePorDefecto);
            }
        }
        cmbPlanes.setBounds(20, y + 25, 480, 35);
        panel.add(cmbPlanes);
        y += 75;

        // --- 2. FECHAS ---
        panel.add(crearLabel("Fecha Inicio:", 20, y));
        dateInicio = new JDateChooser();
        dateInicio.setDate(fechaInicio != null ? fechaInicio : new Date());
        dateInicio.setDateFormatString("dd/MM/yyyy");
        dateInicio.setBounds(20, y + 25, 200, 35);

        dateInicio.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName())) {
                Date nuevaFecha = dateInicio.getDate();
                if (nuevaFecha != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(nuevaFecha);
                    int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
                    spinDiaPago.setValue(dia);
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

        // --- 3. CONDICIONES DEL CONTRATO (NUEVO) ---
        JPanel pnlCondiciones = new JPanel(null);
        pnlCondiciones.setBackground(new Color(240, 248, 255)); // Azul muy claro
        pnlCondiciones
                .setBorder(BorderFactory.createTitledBorder(null, "Condiciones", TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12)));
        pnlCondiciones.setBounds(20, y, 480, 70);
        panel.add(pnlCondiciones);

        chkMesAdelantado = new JCheckBox("Mes Adelantado");
        chkMesAdelantado.setBackground(new Color(240, 248, 255));
        chkMesAdelantado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkMesAdelantado.setSelected(mesAdelantado); // Cargar valor existente
        chkMesAdelantado.setBounds(15, 25, 130, 25);
        pnlCondiciones.add(chkMesAdelantado);

        chkEquiposPrestados = new JCheckBox("Equipos Prestados");
        chkEquiposPrestados.setBackground(new Color(240, 248, 255));
        chkEquiposPrestados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkEquiposPrestados.setSelected(equiposPrestados); // Cargar valor existente
        chkEquiposPrestados.setBounds(150, 25, 140, 25);
        pnlCondiciones.add(chkEquiposPrestados);

        JLabel lblGar = new JLabel("Garantía S/.");
        lblGar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGar.setBounds(300, 25, 80, 25);
        pnlCondiciones.add(lblGar);

        txtGarantia = new JTextField(String.format("%.2f", garantia));
        txtGarantia.setHorizontalAlignment(SwingConstants.RIGHT);
        txtGarantia.setBounds(380, 25, 80, 25);
        pnlCondiciones.add(txtGarantia);

        y += 85;

        // --- 4. DIRECCIÓN ---
        panel.add(crearLabel("Dirección de Instalación:", 20, y));
        txtDireccion = new JTextField(dirActual);
        txtDireccion.setBounds(20, y + 25, 480, 35);
        txtDireccion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDireccion);
        y += 85;

        // --- 5. DATOS DEL CLIENTE ---
        JPanel panelCli = new JPanel(null);
        panelCli.setBackground(new Color(248, 250, 252));
        panelCli.setBorder(
                BorderFactory.createTitledBorder(null, "Datos del Titular", TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12)));
        panelCli.setBounds(20, y, 480, 190);
        panel.add(panelCli);

        JLabel lblDni = new JLabel("DNI:");
        lblDni.setBounds(15, 25, 50, 20);
        panelCli.add(lblDni);

        txtDniCli = new JTextField();
        txtDniCli.setBounds(50, 20, 120, 30);
        panelCli.add(txtDniCli);

        btnBuscarDni = new JButton("Buscar");
        btnBuscarDni.setBounds(180, 20, 50, 30);
        btnBuscarDni.addActionListener(e -> buscarOActivarFormulario());
        panelCli.add(btnBuscarDni);

        JLabel lblNota = new JLabel(
                "<html><font color='gray' size='2'>* Si no existe, ingrese los datos.</font></html>");
        lblNota.setBounds(240, 20, 200, 30);
        panelCli.add(lblNota);

        JLabel lblNom = new JLabel("Nombres:");
        lblNom.setBounds(15, 65, 80, 20);
        panelCli.add(lblNom);

        txtNombresCli = new JTextField();
        txtNombresCli.setBounds(15, 85, 210, 30);
        txtNombresCli.setEnabled(false);
        panelCli.add(txtNombresCli);

        JLabel lblApe = new JLabel("Apellidos:");
        lblApe.setBounds(240, 65, 80, 20);
        panelCli.add(lblApe);

        txtApellidosCli = new JTextField();
        txtApellidosCli.setBounds(240, 85, 220, 30);
        txtApellidosCli.setEnabled(false);
        panelCli.add(txtApellidosCli);

        JLabel lblCel = new JLabel("Celular / Telf:");
        lblCel.setBounds(15, 125, 100, 20);
        panelCli.add(lblCel);

        txtCelularCli = new JTextField();
        txtCelularCli.setBounds(15, 145, 150, 30);
        txtCelularCli.setEnabled(false);
        panelCli.add(txtCelularCli);

        // --- BOTONES ---
        JButton btnGuardar = new JButton("GUARDAR CONTRATO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(150, y + 205, 220, 45); // Ajustado al final
        btnGuardar.addActionListener(e -> guardar());
        panel.add(btnGuardar);

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
            String[] datos = dao.buscarClientePorDni(dni);

            SwingUtilities.invokeLater(() -> {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (datos != null) {
                    esNuevoCliente = false;
                    idClienteSeleccionado = Integer.parseInt(datos[0]);
                    txtNombresCli.setText(datos[1]);
                    txtApellidosCli.setText("");
                    txtCelularCli.setText("---");
                    bloquearCamposCliente(true);
                    txtNombresCli.setBackground(new Color(220, 255, 220));
                } else {
                    esNuevoCliente = true;
                    idClienteSeleccionado = -1;
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

        // --- OBTENER NUEVOS DATOS ---
        boolean mesAdelantado = chkMesAdelantado.isSelected();
        boolean equiposPrestados = chkEquiposPrestados.isSelected();
        double garantia = 0.0;
        try {
            garantia = Double.parseDouble(txtGarantia.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto de garantía inválido.");
            return;
        }

        if (s == null || dir.isEmpty() || fechaInicio == null) {
            JOptionPane.showMessageDialog(this, "Plan, Dirección y Fecha Inicio son obligatorios.");
            return;
        }

        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);
        setVisible(false);

        final double garantiaFinal = garantia; // Para uso en lambda

        new Thread(() -> {
            int idFinalCliente = idClienteSeleccionado;

            if (esNuevoCliente) {
                String dni = txtDniCli.getText().trim();
                String nom = txtNombresCli.getText().trim();
                String ape = txtApellidosCli.getText().trim();
                String tel = txtCelularCli.getText().trim();

                // DNI es opcional, solo Nombre es obligatorio
                if (nom.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null)
                            Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "El Nombre del cliente es obligatorio.");
                    });
                    return;
                }

                ClienteDAO cliDao = new ClienteDAO();
                idFinalCliente = cliDao.registrarClienteYObtenerId(dni, nom, ape, dir, tel);

                if (idFinalCliente == -1) {
                    SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null)
                            Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "Error al registrar el cliente nuevo.");
                    });
                    return;
                }
            } else {
                if (idSuscripcion == -1 && idFinalCliente == -1) {
                    SwingUtilities.invokeLater(() -> {
                        if (Principal.instancia != null)
                            Principal.instancia.mostrarCarga(false);
                        setVisible(true);
                        JOptionPane.showMessageDialog(this, "Busque un cliente o registre uno nuevo.");
                    });
                    return;
                }
            }

            SuscripcionDAO susDao = new SuscripcionDAO();
            // LLAMADA AL DAO ACTUALIZADO
            String nombreSusc = txtNombreSuscripcion.getText().trim();
            boolean exito = susDao.guardarOActualizarContrato(
                    idSuscripcion, s.getIdServicio(), dir, idFinalCliente, fechaInicio, diaPago,
                    mesAdelantado, equiposPrestados, garantiaFinal, nombreSusc);

            SwingUtilities.invokeLater(() -> {
                if (Principal.instancia != null)
                    Principal.instancia.mostrarCarga(false);
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

    private void llenarDatosClienteExistente() {
        txtNombresCli.setText("(Cliente Actual Seleccionado)");
        txtNombresCli.setEnabled(false);
    }

    public boolean isGuardado() {
        return guardado;
    }

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