package vista;

import DAO.SuscripcionDAO;
import DAO.ClienteDAO;
import modelo.Servicio;
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.DefaultListModel;
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
    private JComboBox<String> cmbSector; // Sector del cliente
    private JCheckBox chkEjecutarPagoAdelantado; // Para ejecutar pago al guardar
    // ------------------------

    // CAMPOS DE CLIENTE
    private JTextField txtDniCli, txtNombresCli, txtApellidosCli, txtCelularCli;

    private JDateChooser dateInicio;
    private JSpinner spinDiaPago;

    private int idSuscripcion;
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
        this.idClienteSeleccionado = idClienteOriginal;

        setTitle(idSuscripcion == -1 ? "Nuevo Contrato" : "Editar Contrato #" + idSuscripcion);
        setSize(560, 1020); // Altura aumentada para todos los componentes
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
        panel.setBackground(new Color(255, 253, 245)); // Crema muy sutil para reducir cansancio visual
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        JLabel lblTitulo = new JLabel(idSuscripcion == -1 ? "Registrar Nuevo Contrato" : "Modificar Contrato");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(20, 10, 300, 30);
        panel.add(lblTitulo);

        int y = 60;

        // --- 0. NOMBRE DE SUSCRIPCIÓN (automático, editable) ---
        panel.add(crearLabel("Nombre del Contrato (automático):", 20, y));
        txtNombreSuscripcion = new JTextField(nombreSuscripcion != null ? nombreSuscripcion : nombreCliente);
        txtNombreSuscripcion.setBounds(20, y + 25, 480, 35);
        txtNombreSuscripcion.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        txtNombreSuscripcion.setToolTipText("Se autocompleta con el nombre del cliente. Solo editar si es diferente.");
        txtNombreSuscripcion.setBackground(Color.WHITE); // Fondo blanco
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

        // --- 4. PANEL INFORMATIVO DE PRÓXIMO PAGO ---
        JPanel pnlInfoPago = new JPanel(null);
        pnlInfoPago.setBackground(new Color(240, 253, 244)); // Verde muy claro
        pnlInfoPago.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(34, 197, 94)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        pnlInfoPago.setBounds(20, y, 480, 100); // Altura aumentada para texto completo
        panel.add(pnlInfoPago);

        JLabel lblInfoPago = new JLabel();
        lblInfoPago.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoPago.setBounds(10, 5, 460, 45); // Mayor altura para 2 líneas
        pnlInfoPago.add(lblInfoPago);

        // Checkbox para ejecutar pago automático (solo visible si adelantado)
        chkEjecutarPagoAdelantado = new JCheckBox("Registrar primer pago ahora");
        chkEjecutarPagoAdelantado.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkEjecutarPagoAdelantado.setBackground(new Color(254, 249, 195));
        chkEjecutarPagoAdelantado.setBounds(10, 55, 250, 25); // Movido hacia abajo
        chkEjecutarPagoAdelantado.setSelected(true);
        chkEjecutarPagoAdelantado.setVisible(false);
        pnlInfoPago.add(chkEjecutarPagoAdelantado);

        // Función para actualizar info de pago
        Runnable actualizarInfoPago = () -> {
            Date fecha = dateInicio.getDate();
            int dia = (int) spinDiaPago.getValue();
            boolean adelantado = chkMesAdelantado.isSelected();

            if (fecha != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(fecha);
                cal.set(java.util.Calendar.DAY_OF_MONTH,
                        Math.min(dia, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)));

                // Si es adelantado, el primer pago es hoy
                String infoPrimerPago;
                if (adelantado) {
                    infoPrimerPago = "<html><b>PAGO ADELANTADO:</b> El cliente pagara <b>HOY</b> al guardar.<br>" +
                            "Proximo vencimiento: <b>" +
                            new java.text.SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()) + "</b></html>";
                    pnlInfoPago.setBackground(new Color(254, 249, 195));
                    chkEjecutarPagoAdelantado.setVisible(true);
                    chkEjecutarPagoAdelantado.setBackground(new Color(254, 249, 195));
                } else {
                    infoPrimerPago = "<html><b>Primer vencimiento:</b> " +
                            new java.text.SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()) +
                            " (Día " + dia + " de cada mes)</html>";
                    pnlInfoPago.setBackground(new Color(240, 253, 244));
                    chkEjecutarPagoAdelantado.setVisible(false);
                }
                lblInfoPago.setText(infoPrimerPago);
            }
        };

        // Listeners para actualizar info
        dateInicio.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName()))
                actualizarInfoPago.run();
        });
        spinDiaPago.addChangeListener(e -> actualizarInfoPago.run());
        chkMesAdelantado.addActionListener(e -> actualizarInfoPago.run());

        // Ejecutar al inicio
        actualizarInfoPago.run();

        y += 110; // Aumentado por mayor altura del panel

        // --- 5. DIRECCIÓN ---
        panel.add(crearLabel("Dirección de Instalación:", 20, y));
        txtDireccion = new JTextField(dirActual);
        txtDireccion.setBounds(20, y + 25, 320, 35);
        txtDireccion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDireccion);

        // --- SECTOR (al lado de dirección) ---
        panel.add(crearLabel("Sector:", 360, y));
        cmbSector = new JComboBox<>(new String[] { "EL MILAGRO", "EL MIRADOR" });
        cmbSector.setBounds(360, y + 25, 140, 35);
        cmbSector.setBackground(Color.WHITE);
        cmbSector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(cmbSector);
        y += 75;

        // --- 6. DATOS DEL CLIENTE (MEJORADO CON BÚSQUEDA) ---
        JPanel panelCli = new JPanel(null);
        panelCli.setBackground(new Color(248, 250, 252));
        panelCli.setBorder(
                BorderFactory.createTitledBorder(null, "Titular del Contrato", TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12)));
        panelCli.setBounds(20, y, 480, 310); // Altura para formulario expandido
        panel.add(panelCli);

        // Checkbox para cliente nuevo - MARCADO POR DEFECTO (ARRIBA DE TODO)
        JCheckBox chkNuevoCliente = new JCheckBox("Nuevo cliente");
        chkNuevoCliente.setBackground(new Color(248, 250, 252));
        chkNuevoCliente.setBounds(15, 20, 120, 25);
        chkNuevoCliente.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkNuevoCliente.setForeground(new Color(37, 99, 235));
        chkNuevoCliente.setSelected(true); // Marcado por defecto
        panelCli.add(chkNuevoCliente);

        // --- PANEL DE CAMPOS PARA CLIENTE NUEVO (VISIBLE POR DEFECTO, ARRIBA) ---
        JPanel pnlCamposNuevo = new JPanel(null);
        pnlCamposNuevo.setBackground(new Color(255, 250, 240)); // Crema claro
        pnlCamposNuevo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11)),
                "Datos del Nuevo Cliente",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 10),
                new Color(180, 100, 0)));
        pnlCamposNuevo.setBounds(15, 48, 450, 130);
        pnlCamposNuevo.setVisible(true); // Visible por defecto ya que checkbox está marcado
        panelCli.add(pnlCamposNuevo);

        // Separador visual
        JLabel lblOpcional = new JLabel("— o buscar cliente existente —");
        lblOpcional.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblOpcional.setForeground(Color.GRAY);
        lblOpcional.setBounds(150, 180, 200, 15);
        panelCli.add(lblOpcional);

        // Campo de búsqueda (ABAJO)
        JLabel lblBuscar = new JLabel("Buscar cliente existente:");
        lblBuscar.setBounds(15, 195, 200, 20);
        panelCli.add(lblBuscar);

        JTextField txtBuscarCliente = new JTextField();
        txtBuscarCliente.setBounds(15, 215, 350, 28);
        txtBuscarCliente.setToolTipText("Escriba para buscar clientes existentes");
        panelCli.add(txtBuscarCliente);

        // Lista de resultados (ABAJO)
        DefaultListModel<String> listaModelo = new DefaultListModel<>();
        JList<String> listaClientes = new JList<>(listaModelo);
        listaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        listaClientes.setSelectionBackground(new Color(219, 234, 254));
        JScrollPane scrollLista = new JScrollPane(listaClientes);
        scrollLista.setBounds(15, 245, 450, 55);
        panelCli.add(scrollLista);

        // Lista interna para mapear selección a ID
        java.util.List<Object[]> clientesEncontrados = new java.util.ArrayList<>();

        // Fila 1: Nombres y Apellidos (mejor alineados)
        JLabel lblNomNuevo = new JLabel("Nombres*");
        lblNomNuevo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNomNuevo.setBounds(10, 20, 70, 20);
        pnlCamposNuevo.add(lblNomNuevo);

        txtNombresCli = new JTextField();
        txtNombresCli.setBounds(10, 40, 140, 28);
        txtNombresCli.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCamposNuevo.add(txtNombresCli);

        JLabel lblApeNuevo = new JLabel("Apellidos");
        lblApeNuevo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblApeNuevo.setBounds(160, 20, 70, 20);
        pnlCamposNuevo.add(lblApeNuevo);

        txtApellidosCli = new JTextField();
        txtApellidosCli.setBounds(160, 40, 140, 28);
        txtApellidosCli.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCamposNuevo.add(txtApellidosCli);

        // Fila 2: DNI y Teléfono (mejor alineados)
        JLabel lblDniNuevo = new JLabel("DNI (opcional)");
        lblDniNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDniNuevo.setForeground(Color.GRAY);
        lblDniNuevo.setBounds(310, 20, 80, 20);
        pnlCamposNuevo.add(lblDniNuevo);

        txtDniCli = new JTextField();
        txtDniCli.setBounds(310, 40, 90, 28);
        txtDniCli.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCamposNuevo.add(txtDniCli);

        JLabel lblTelNuevo = new JLabel("Teléfono*");
        lblTelNuevo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTelNuevo.setBounds(10, 72, 70, 20);
        pnlCamposNuevo.add(lblTelNuevo);

        txtCelularCli = new JTextField();
        txtCelularCli.setBounds(10, 92, 140, 28);
        txtCelularCli.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCelularCli.setToolTipText("Número para notificaciones WhatsApp");
        pnlCamposNuevo.add(txtCelularCli);

        JLabel lblReq = new JLabel("* Obligatorio");
        lblReq.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        lblReq.setForeground(new Color(180, 100, 0));
        lblReq.setBounds(160, 100, 80, 15);
        pnlCamposNuevo.add(lblReq);

        // Navegación con Enter entre campos
        java.awt.event.KeyAdapter enterNav = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    ((java.awt.Component) e.getSource()).transferFocus();
                }
            }
        };
        txtNombresCli.addKeyListener(enterNav);
        txtApellidosCli.addKeyListener(enterNav);
        txtDniCli.addKeyListener(enterNav);
        txtCelularCli.addKeyListener(enterNav);

        // Listener para búsqueda en tiempo real
        txtBuscarCliente.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private javax.swing.Timer timer;

            private void buscar() {
                if (timer != null)
                    timer.stop();
                timer = new javax.swing.Timer(300, e -> {
                    String texto = txtBuscarCliente.getText().trim();
                    if (texto.length() >= 2) {
                        new Thread(() -> {
                            ClienteDAO cliDAO = new ClienteDAO();
                            java.util.List<Object[]> resultados = cliDAO.buscarClientesParaDropdown(texto);
                            SwingUtilities.invokeLater(() -> {
                                clientesEncontrados.clear();
                                clientesEncontrados.addAll(resultados);
                                listaModelo.clear();
                                for (Object[] cli : resultados) {
                                    String display = cli[1] + " " + cli[2] + " - DNI: "
                                            + (cli[4] != null ? cli[4] : "---");
                                    listaModelo.addElement(display);
                                }
                            });
                        }).start();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }
        });

        // Listener para selección de cliente
        listaClientes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaClientes.getSelectedIndex() >= 0) {
                int idx = listaClientes.getSelectedIndex();
                if (idx < clientesEncontrados.size()) {
                    Object[] datos = clientesEncontrados.get(idx);
                    esNuevoCliente = false;
                    idClienteSeleccionado = (int) datos[0];
                    String nombreCompleto = (datos[1] + " " + datos[2]).toUpperCase();
                    txtBuscarCliente.setText(nombreCompleto);
                    txtBuscarCliente.setBackground(new Color(220, 252, 231)); // Verde claro
                    // Siempre actualizar nombre de suscripción con el cliente seleccionado
                    txtNombreSuscripcion.setText(nombreCompleto);
                }
            }
        });

        // Listener para checkbox nuevo cliente
        chkNuevoCliente.addActionListener(e -> {
            boolean nuevo = chkNuevoCliente.isSelected();
            esNuevoCliente = nuevo;
            idClienteSeleccionado = -1;

            // Mostrar/ocultar panel de campos
            pnlCamposNuevo.setVisible(nuevo);

            // Ajustar tamaño del panel contenedor si es necesario
            if (nuevo) {
                panelCli.setPreferredSize(new java.awt.Dimension(480, 280));
            } else {
                panelCli.setPreferredSize(new java.awt.Dimension(480, 220));
            }

            // Deshabilitar búsqueda si es nuevo
            txtBuscarCliente.setEnabled(!nuevo);
            listaClientes.setEnabled(!nuevo);
            scrollLista.setEnabled(!nuevo);

            if (nuevo) {
                txtBuscarCliente.setBackground(Color.LIGHT_GRAY);
                txtNombresCli.requestFocus();
            } else {
                txtBuscarCliente.setBackground(Color.WHITE);
            }

            panelCli.revalidate();
            panelCli.repaint();
        });

        // Si es edición, mostrar cliente actual seleccionado y desmarcar checkbox
        if (idSuscripcion != -1 && nombreCliente != null && !nombreCliente.isEmpty()) {
            chkNuevoCliente.setSelected(false);
            esNuevoCliente = false;
            pnlCamposNuevo.setVisible(false);
            txtBuscarCliente.setEnabled(true);
            txtBuscarCliente.setText(nombreCliente);
            txtBuscarCliente.setBackground(new Color(220, 252, 231));
        } else {
            // Para nuevo contrato, configurar estado inicial
            esNuevoCliente = true;
            txtBuscarCliente.setEnabled(false);
            txtBuscarCliente.setBackground(Color.LIGHT_GRAY);
            // Focus en el campo de nombres al abrir
            SwingUtilities.invokeLater(() -> txtNombresCli.requestFocusInWindow());
        }

        // --- BOTONES ---
        JButton btnGuardar = new JButton("GUARDAR CONTRATO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(150, y + 325, 220, 45); // Ajustado para panel expandido
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

        if (s == null || fechaInicio == null) {
            JOptionPane.showMessageDialog(this, "Plan y Fecha Inicio son obligatorios.");
            return;
        }

        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);
        // NO hacer setVisible(false) aquí - rompe la modalidad del diálogo
        // Se cerrará con dispose() cuando el thread termine

        final double garantiaFinal = garantia; // Para uso en lambda

        new Thread(() -> {
            int idFinalCliente = idClienteSeleccionado;

            if (esNuevoCliente) {
                String dni = txtDniCli.getText().trim();
                String nom = txtNombresCli.getText().trim().toUpperCase(); // Mayúsculas
                String ape = txtApellidosCli.getText().trim().toUpperCase(); // Mayúsculas
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
            // LLAMADA AL DAO ACTUALIZADO - Ahora retorna ID
            String nombreSusc = txtNombreSuscripcion.getText().trim().toUpperCase(); // Mayúsculas
            String sector = (String) cmbSector.getSelectedItem(); // Sector seleccionado
            int idSuscripcionCreada = susDao.guardarOActualizarContrato(
                    idSuscripcion, s.getIdServicio(), dir, idFinalCliente, fechaInicio, diaPago,
                    mesAdelantado, equiposPrestados, garantiaFinal, nombreSusc, sector);

            boolean exito = idSuscripcionCreada > 0;

            boolean pagoRegistrado = false;
            int mesesGenerados = 0;

            // Solo generar primer mes si es NUEVO contrato con mes adelantado
            if (exito && idSuscripcion == -1 && mesAdelantado) {
                try {
                    DAO.PagoDAO pagoDao = new DAO.PagoDAO();

                    // Calcular mes inicial basado en FECHA DE INICIO
                    java.util.Calendar calInicio = java.util.Calendar.getInstance();
                    calInicio.setTime(fechaInicio);
                    int diaDelMes = calInicio.get(java.util.Calendar.DAY_OF_MONTH);
                    if (diaDelMes > 16) {
                        calInicio.add(java.util.Calendar.MONTH, 1);
                    }
                    calInicio.set(java.util.Calendar.DAY_OF_MONTH, 1); // Normalizar al día 1

                    // SOLO PRIMER MES
                    String periodoMes = new java.text.SimpleDateFormat("MMMM yyyy",
                            new java.util.Locale("es", "ES"))
                            .format(calInicio.getTime()).toUpperCase();

                    boolean marcarComoPagado = chkEjecutarPagoAdelantado.isSelected();

                    // Calcular rango del periodo
                    java.text.SimpleDateFormat sdfRango = new java.text.SimpleDateFormat("dd MMM",
                            new java.util.Locale("es", "ES"));
                    java.util.Calendar calRangoInicio = java.util.Calendar.getInstance();
                    calRangoInicio.setTime(fechaInicio);
                    calRangoInicio.set(java.util.Calendar.DAY_OF_MONTH, diaPago);
                    java.util.Calendar calRangoFin = (java.util.Calendar) calRangoInicio.clone();
                    calRangoFin.add(java.util.Calendar.MONTH, 1);
                    String rangoPeriodo = sdfRango.format(calRangoInicio.getTime()) + " - " +
                            sdfRango.format(calRangoFin.getTime());

                    pagoRegistrado = pagoDao.crearFacturaManual(
                            idSuscripcionCreada,
                            periodoMes,
                            s.getMensualidad(),
                            marcarComoPagado ? 2 : 1,
                            new java.sql.Date(fechaInicio.getTime()),
                            marcarComoPagado,
                            1,
                            rangoPeriodo);
                    mesesGenerados = 1;
                    String estado = marcarComoPagado ? "PAGADO" : "PENDIENTE";
                    System.out.println("Factura " + estado + " creada para: " + periodoMes + " (" + rangoPeriodo + ")");
                } catch (Exception ex) {
                    System.err.println("Error creando factura: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Variable para mantener compatibilidad con mensajes
            final boolean deudaCreada = !pagoRegistrado && mesAdelantado;
            final int mesesFinal = mesesGenerados;

            final boolean exitoFinal = exito;
            final boolean pagoFinal = pagoRegistrado;
            final boolean deudaFinal = deudaCreada;
            SwingUtilities.invokeLater(() -> {
                if (Principal.instancia != null)
                    Principal.instancia.mostrarCarga(false);
                if (exitoFinal) {
                    String mensaje = "Contrato guardado exitosamente.";
                    if (pagoFinal) {
                        mensaje += "\nPrimer pago adelantado registrado en caja.";
                    } else if (deudaFinal) {
                        mensaje += "\n⚠️ Primera factura creada como PENDIENTE.";
                    }
                    JOptionPane.showMessageDialog(null, mensaje);
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