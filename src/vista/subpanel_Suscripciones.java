package vista;

import DAO.SuscripcionDAO;
import modelo.Suscripcion;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Servicio;
import DAO.ServicioDAO;

public class subpanel_Suscripciones extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private SuscripcionDAO susDAO;
    private PanelDetalleContrato panelDetalle;
    private JTextField txtBuscar;
    private JComboBox<String> cmbOrden;
    private JLabel lblTotalContratos;

    // Botones dinámicos
    private JButton btnEstadoDinamico; // Suspender / Reactivar
    private JButton btnDarBaja; // Cancelar Contrato Definitivamente

    // Datos en memoria
    private List<String[]> clientesCache = new ArrayList<>();
    private List<Suscripcion> listaCache = new ArrayList<>();
    private List<Servicio> planesCache = new ArrayList<>();

    // --- AUTOCOMPLETADO ---
    private JPopupMenu menuSugerencias;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;

    // ...

    public subpanel_Suscripciones() {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 253, 245)); // Crema sutil
        setBorder(new EmptyBorder(5, 10, 5, 10));

        susDAO = new SuscripcionDAO();
        precargarDatosAuxiliares();
        precargarClientes();
        initUI();
        // initAutocompletado();
        cargarDatos("");
    }

    private void precargarDatosAuxiliares() {
        new Thread(() -> {
            DAO.ClienteDAO dao = new DAO.ClienteDAO();
            clientesCache = dao.obtenerListaSimpleClientes();
            try {
                ServicioDAO servDao = new ServicioDAO();
                planesCache = servDao.obtenerServiciosActivos();
            } catch (Exception e) {
                System.err.println("Error planes: " + e.getMessage());
            }
        }).start();
    }

    private void precargarClientes() {
        new Thread(() -> {
            // 1. Clientes (Lo que ya tenías)
            DAO.ClienteDAO dao = new DAO.ClienteDAO();
            clientesCache = dao.obtenerListaSimpleClientes();

            // 2. NUEVO: Cargar Planes
            try {
                ServicioDAO servDao = new ServicioDAO();
                planesCache = servDao.obtenerServiciosActivos(); // O .listar()
            } catch (Exception e) {
                System.out.println("Error cargando planes: " + e.getMessage());
            }
        }).start();
    }

    private void initUI() {
        // --- 1. PANEL SUPERIOR (HEADER) ---
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(new Color(255, 253, 245)); // Crema sutil

        // Título
        JLabel lblTitulo = new JLabel("CONTRATOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 10, 120, 30);
        topPanel.add(lblTitulo);

        // Buscador
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar cliente...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBounds(130, 10, 250, 30);

        // Listener del Buscador (Enter = BD, Escribir = Filtro Local)
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cargarDatos(txtBuscar.getText());
                } else {
                    filtrarLocalmente(txtBuscar.getText());
                }
            }
        });
        topPanel.add(txtBuscar);

        // Filtro Orden
        cmbOrden = new JComboBox<>(
                new String[] { "DIA DE PAGO", "MÁS RECIENTES", "MÁS ANTIGUOS", "NOMBRE (A-Z)", "DEUDORES" });
        cmbOrden.setBounds(390, 10, 130, 30);
        cmbOrden.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbOrden.setBackground(Color.WHITE);
        cmbOrden.addActionListener(e -> cargarDatos(txtBuscar.getText()));
        topPanel.add(cmbOrden);

        // Botón Lupa
        JButton btnBuscar = new JButton("Buscar");
        estilarBoton(btnBuscar, new Color(241, 245, 249), Color.BLACK);
        btnBuscar.setBounds(530, 10, 85, 30);
        btnBuscar.addActionListener(e -> cargarDatos(txtBuscar.getText()));
        topPanel.add(btnBuscar);

        // --- BOTONES DE ACCIÓN ---

        // Botón Nuevo (+ Contrato)
        JButton btnNuevo = new JButton("+ CONTRATO");
        estilarBoton(btnNuevo, new Color(37, 99, 235), Color.WHITE);
        btnNuevo.setBounds(600, 10, 110, 30);
        btnNuevo.addActionListener(e -> abrirNuevoContrato());
        topPanel.add(btnNuevo);

        // Coordenada X base para los botones de gestión
        int xAccion = 700;

        // Botón Dinámico (Suspender/Reactivar)
        btnEstadoDinamico = new JButton("SUSPENDER");
        estilarBoton(btnEstadoDinamico, new Color(245, 158, 11), Color.WHITE);
        btnEstadoDinamico.setBounds(xAccion, 10, 100, 30);
        btnEstadoDinamico.setEnabled(false);
        btnEstadoDinamico.addActionListener(e -> accionBotonDinamico());
        topPanel.add(btnEstadoDinamico);

        // Botón Dar de Baja
        btnDarBaja = new JButton("BAJA");
        estilarBoton(btnDarBaja, new Color(220, 38, 38), Color.WHITE);
        btnDarBaja.setBounds(xAccion + 110, 10, 70, 30);
        btnDarBaja.setEnabled(false);
        btnDarBaja.addActionListener(e -> accionDarDeBaja());
        topPanel.add(btnDarBaja);

        // Botón Editar
        JButton btnEditar = new JButton("EDITAR");
        estilarBoton(btnEditar, new Color(234, 179, 8), Color.WHITE);
        btnEditar.setBounds(xAccion + 190, 10, 80, 30);
        btnEditar.addActionListener(e -> abrirEdicion());
        topPanel.add(btnEditar);

        // Agregamos el TopPanel UNA SOLA VEZ
        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABLA Y DETALLES ---
        String[] cols = { "ID", "CLIENTE", "PLAN", "MONTO", "DIA", "ESTADO", "HISTORIAL", "OBJ" };
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setShowVerticalLines(true);
        tabla.setGridColor(new Color(220, 220, 220));

        // Ocultar columnas internas
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0); // ID
        tabla.getColumnModel().getColumn(7).setMinWidth(0);
        tabla.getColumnModel().getColumn(7).setMaxWidth(0); // OBJ

        // Anchos
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220); // Cliente
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Plan
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60); // Monto
        tabla.getColumnModel().getColumn(4).setPreferredWidth(40); // Dia
        tabla.getColumnModel().getColumn(5).setPreferredWidth(70); // Estado
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120); // Historial

        // Renderizadores
        tabla.setDefaultRenderer(Object.class, new GeneralRenderer());
        tabla.getColumnModel().getColumn(6).setCellRenderer(new HistorialRendererCompacto());
        tabla.getColumnModel().getColumn(1).setCellRenderer(new ClienteRenderer());

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        panelDetalle = new PanelDetalleContrato();

        // Listener de selección corregido
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (tabla.getSelectedRow() != -1) {
                    Suscripcion s = (Suscripcion) modelo.getValueAt(tabla.getSelectedRow(), 7);
                    panelDetalle.mostrarDatos(s);
                    actualizarEstadoBotones(s); // Lógica de botones
                } else {
                    btnEstadoDinamico.setEnabled(false);
                    btnDarBaja.setEnabled(false);
                }
            }
        });

        // Split Pane (Tabla | Detalle)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelDetalle);
        split.setBorder(null);
        split.setDividerSize(3);
        split.setResizeWeight(1.0); // Tabla ocupa el espacio extra
        panelDetalle.setMinimumSize(new Dimension(350, 0));
        panelDetalle.setPreferredSize(new Dimension(380, 0));

        add(split, BorderLayout.CENTER);

        // --- 3. FOOTER (CONTADOR) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(new Color(255, 253, 245)); // Crema sutil
        bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        lblTotalContratos = new JLabel("Total Contratos: 0");
        lblTotalContratos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalContratos.setForeground(Color.GRAY);
        bottomPanel.add(lblTotalContratos);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- LÓGICA INTELIGENTE DE BOTONES ---
    private void actualizarEstadoBotones(Suscripcion s) {
        // Verificar si está dado de baja (Si tu objeto tiene fechaCancelacion, úsala
        // aquí)
        // Por ahora asumimos la lógica solicitada: Activo vs Suspendido

        btnDarBaja.setEnabled(true);
        btnEstadoDinamico.setEnabled(true);

        if (s.getActivo() == 1) {
            // ESTÁ ACTIVO -> Ofrecer SUSPENDER
            btnEstadoDinamico.setText("SUSPENDER");
            btnEstadoDinamico.setBackground(new Color(245, 158, 11)); // Naranja
        } else {
            // ESTÁ SUSPENDIDO (INACTIVO) -> Ofrecer REACTIVAR
            btnEstadoDinamico.setText("REACTIVAR");
            btnEstadoDinamico.setBackground(new Color(22, 163, 74)); // Verde
        }

        // Opcional: Si ya está dado de baja definitivamente, podrías deshabilitar todo
        // if (s.getFechaCancelacion() != null) {
        // btnEstadoDinamico.setEnabled(false);
        // btnEstadoDinamico.setText("DE BAJA");
        // }
    }

    private void accionBotonDinamico() {
        int fila = tabla.getSelectedRow();
        if (fila == -1)
            return;
        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);

        boolean esSuspension = (s.getActivo() == 1);
        String msj = esSuspension ? "¿Suspender el servicio por falta de pago?" : "¿Reactivar el servicio al cliente?";

        if (JOptionPane.showConfirmDialog(this, msj, "Confirmar Estado",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // 1 = Activo, 0 = Suspendido
            int nuevoEstado = esSuspension ? 0 : 1;
            new Thread(() -> {
                susDAO.cambiarEstado(s.getIdSuscripcion(), nuevoEstado);
                SwingUtilities.invokeLater(() -> cargarDatos(txtBuscar.getText()));
            }).start();
        }
    }

    private void accionDarDeBaja() {
        int fila = tabla.getSelectedRow();
        if (fila == -1)
            return;
        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);

        String msj = "<html>ADVERTENCIA: Dar de baja finalizará el contrato hoy.<br>"
                + "El servicio pasará a estado 'BAJA' y se guardará la fecha de cancelación.<br><br>"
                + "¿Confirmar la BAJA DEFINITIVA?</html>";

        if (JOptionPane.showConfirmDialog(this, msj, "Confirmar Baja", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            // Mostrar carga
            if (Principal.instancia != null)
                Principal.instancia.mostrarCarga(true);

            new Thread(() -> {
                // Aquí llamamos al método que actualiza fecha_cancelacion = NOW() y activo = 0
                susDAO.darDeBajaContrato(s.getIdSuscripcion());
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null)
                        Principal.instancia.mostrarCarga(false);
                    JOptionPane.showMessageDialog(this, "Contrato finalizado correctamente.");
                    txtBuscar.setText(""); // Limpiar búsqueda
                    cargarDatos("");
                });
            }).start();
        }
    }

    private void abrirNuevoContrato() {
        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);

        // PASAMOS: planesCache, Fecha de hoy, Día 1, y condiciones por defecto para
        // nuevo contrato
        DialogoEditarContrato dialog = new DialogoEditarContrato(
                (java.awt.Frame) parent,
                -1, "", "", -1, "", // Datos vacíos
                planesCache, // Lista de planes
                new Date(), // Fecha de hoy
                1, // Día de pago default
                true, // Mes adelantado (default: true)
                true, // Equipos prestados (default: true)
                0.0, // Garantía (default: 0)
                null // Nombre suscripción (se llenara del nombre del cliente)
        );

        dialog.setVisible(true);
        if (dialog.isGuardado()) {
            txtBuscar.setText(""); // Limpiar búsqueda para mostrar lista completa
            cargarDatos("");
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> txtBuscar.requestFocusInWindow());
    }

    // --- LÓGICA DE AUTOCOMPLETADO ---
    private void initAutocompletado() {
        menuSugerencias = new JPopupMenu();
        menuSugerencias.setFocusable(false);
        modeloSugerencias = new DefaultListModel<>();
        listaSugerencias = new JList<>(modeloSugerencias);
        listaSugerencias.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        listaSugerencias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                escogerSugerencia();
            }
        });
        menuSugerencias.add(new JScrollPane(listaSugerencias));

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    menuSugerencias.setVisible(false);
                    cargarDatos(txtBuscar.getText());
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (menuSugerencias.isVisible()) {
                        listaSugerencias.requestFocusInWindow();
                        listaSugerencias.setSelectedIndex(0);
                    }
                    return;
                }
                filtrarSugerencias();
            }
        });

        listaSugerencias.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    escogerSugerencia();
                }
            }
        });
    }

    private void filtrarSugerencias() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        modeloSugerencias.clear();
        if (texto.isEmpty()) {
            menuSugerencias.setVisible(false);
            return;
        }

        int count = 0;
        // EN: vista/subpanel_Suscripciones.java -> filtrarSugerencias()

        for (String[] cli : clientesCache) {
            if (count > 10) {
                break;
            }

            String dni = cli[0];
            String nombre = cli[1];

            // SOLUCIÓN: Validamos que NO sean null antes de usar startsWith o contains
            boolean matchDni = (dni != null && dni.startsWith(texto));
            boolean matchNombre = (nombre != null && nombre.toLowerCase().contains(texto));

            if (matchDni || matchNombre) {
                modeloSugerencias.addElement(nombre);
                count++;
            }
        }

        if (!modeloSugerencias.isEmpty()) {
            menuSugerencias.setPopupSize(txtBuscar.getWidth(), 150);
            menuSugerencias.show(txtBuscar, 0, txtBuscar.getHeight());
            txtBuscar.requestFocus();
        } else {
            menuSugerencias.setVisible(false);
        }
    }

    private void escogerSugerencia() {
        String sel = listaSugerencias.getSelectedValue();
        if (sel != null) {
            txtBuscar.setText(sel);
            menuSugerencias.setVisible(false);
            cargarDatos(sel);
        }
    }

    private void cargarDatos(String busqueda) { // Tu método existente
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }
        modelo.setRowCount(0);

        new Thread(() -> {
            String orden = (String) cmbOrden.getSelectedItem();
            // Hacemos la consulta REAL a la BD
            List<Suscripcion> listaTraida = susDAO.listarTodo(busqueda, orden);

            // --- CAMBIO AQUÍ: Guardamos copia en memoria ---
            listaCache = new ArrayList<>(listaTraida);
            // -----------------------------------------------

            SwingUtilities.invokeLater(() -> {
                llenarTabla(listaCache); // Usamos un método auxiliar para llenar
                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    private void filtrarLocalmente(String texto) {
        String busqueda = texto.trim().toLowerCase();
        List<Suscripcion> resultados = new ArrayList<>();

        for (Suscripcion s : listaCache) {
            // Aquí defines tus criterios de búsqueda (Cliente o Servicio)
            if (s.getNombreCliente().toLowerCase().contains(busqueda)
                    || s.getNombreServicio().toLowerCase().contains(busqueda)) {
                resultados.add(s);
            }
        }
        llenarTabla(resultados); // Actualiza la tabla visualmente sin ir a la BD
    }

    private void llenarTabla(List<Suscripcion> listaParaMostrar) {
        modelo.setRowCount(0);
        for (Suscripcion s : listaParaMostrar) {
            modelo.addRow(new Object[] {
                    s.getIdSuscripcion(),
                    s.getNombreCliente(),
                    s.getNombreServicio(),
                    "S/. " + s.getMontoMensual(),
                    s.getDiaPago(),
                    s.getActivo() == 1 ? "ACTIVO" : "CORTADO",
                    s.getHistorialPagos(),
                    s // Objeto oculto
            });
        }
        lblTotalContratos.setText("Total Contratos: " + listaParaMostrar.size());
    }

    // EN: vista/subpanel_Suscripciones.java

    private void cambiarEstadoServicio(int estadoForzado) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contrato.");
            return;
        }

        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);

        // 1. Lógica para alternar estado si viene del botón dinámico (-1)
        int nuevoEstado;
        if (estadoForzado == -1) {
            nuevoEstado = (s.getActivo() == 1) ? 0 : 1;
        } else {
            nuevoEstado = estadoForzado;
        }

        String accion = (nuevoEstado == 1) ? "ACTIVAR" : "CORTAR / SUSPENDER";

        // 2. AQUÍ ESTÁ LA LÓGICA DE ALERTAS QUE PEDISTE
        if (nuevoEstado == 0) { // Si vamos a CORTAR
            StringBuilder alerta = new StringBuilder();
            alerta.append("¿Está seguro de ").append(accion).append(" el servicio a ").append(s.getNombreCliente())
                    .append("?\n\n");

            // Verificamos si tiene equipos prestados (SEMA/SIFM)
            if (s.isEquiposPrestados()) {
                alerta.append("⚠️ ¡ATENCIÓN! CLIENTE TIENE EQUIPOS PRESTADOS.\n");
                alerta.append("   -> Debe recuperar: Router / ONU / Antena.\n\n");
            } else {
                alerta.append("ℹ️ Equipos propios (No requiere devolución).\n\n");
            }

            // Verificamos si tiene garantía
            if (s.getGarantia() > 0) {
                alerta.append("💰 ¡ALERTA! HAY GARANTÍA POR DEVOLVER: S/. ")
                        .append(String.format("%.2f", s.getGarantia())).append("\n");
            }

            int confirm = JOptionPane.showConfirmDialog(this, alerta.toString(), "Confirmar Corte",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;

        } else {
            // Lógica simple para ACTIVAR
            int confirm = JOptionPane.showConfirmDialog(this, "¿Reactivar servicio?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;
        }

        // 3. EJECUCIÓN EN BASE DE DATOS
        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);

        new Thread(() -> {
            boolean dbSuccess = susDAO.cambiarEstado(s.getIdSuscripcion(), nuevoEstado);

            SwingUtilities.invokeLater(() -> {
                if (Principal.instancia != null)
                    Principal.instancia.mostrarCarga(false);
                if (dbSuccess) {
                    JOptionPane.showMessageDialog(this, "Estado actualizado correctamente.");
                    cargarDatos(txtBuscar.getText()); // Refrescar tabla
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar base de datos.");
                }
            });
        }).start();
    }

    private void abrirEdicion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1)
            return;

        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);
        int idCliente = susDAO.obtenerIdClienteDeContrato(s.getIdSuscripcion());

        // Obtener condiciones del contrato
        Object[] condiciones = susDAO.obtenerCondicionesContrato(s.getIdSuscripcion());
        boolean mesAdelantado = true;
        boolean equiposPrestados = true;
        double garantia = 0.0;

        if (condiciones != null) {
            mesAdelantado = ((Integer) condiciones[0]) == 1;
            equiposPrestados = ((Integer) condiciones[1]) == 1;
            garantia = (Double) condiciones[2];
        }

        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);

        DialogoEditarContrato dialog = new DialogoEditarContrato(
                (java.awt.Frame) parent,
                s.getIdSuscripcion(),
                s.getNombreServicio(),
                s.getDireccionInstalacion(),
                idCliente,
                s.getNombreCliente(),
                planesCache, // Lista de planes
                s.getFechaInicio(), // Fecha del contrato
                s.getDiaPago(), // Día de pago del contrato
                mesAdelantado, // Condición: mes adelantado
                equiposPrestados, // Condición: equipos prestados
                garantia, // Monto de garantía
                s.getNombreSuscripcion() // Nombre del contrato
        );

        dialog.setVisible(true);
        if (dialog.isGuardado()) {
            cargarDatos(txtBuscar.getText());
        }
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
    }

    // --- RENDERIZADORES ---
    class ClienteRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row,
                int col) {
            super.getTableCellRendererComponent(table, value, isS, hasF, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(0, 5, 0, 0));
            aplicarColorExcel(this, isS, row);
            return this;
        }
    }

    class GeneralRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row,
                int col) {
            super.getTableCellRendererComponent(table, value, isS, hasF, row, col);
            if (col == 3 || col == 4) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (col == 5) { // Estado
                String estado = (String) value;
                if ("CORTADO".equals(estado) || "SUSPENDIDO".equals(estado)) {
                    setForeground(new Color(200, 0, 0));
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else {
                    setForeground(new Color(0, 128, 0));
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                }
            } else {
                setForeground(Color.BLACK);
            }
            aplicarColorExcel(this, isS, row);
            return this;
        }
    }

    // --- RENDERIZADOR HISTORIAL INTELIGENTE ---
    class HistorialRendererCompacto extends DefaultTableCellRenderer {

        private String historialActual;

        private char[] obtenerLetrasMeses() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int mesActual = cal.get(java.util.Calendar.MONTH);
            char[] letras = new char[6];
            String iniciales = "EFMAMJJASOND";

            for (int i = 0; i < 6; i++) {
                int indiceMes = (mesActual - i);
                if (indiceMes < 0) {
                    indiceMes += 12;
                }
                letras[5 - i] = iniciales.charAt(indiceMes);
            }
            return letras;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row,
                int col) {
            this.historialActual = (String) value;
            super.getTableCellRendererComponent(table, value, isS, hasF, row, col);
            setText("");
            aplicarColorExcel(this, isS, row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (historialActual == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 16;
            int gap = 4;
            int startX = 5;
            int y = (getHeight() - size) / 2;
            char[] letrasMeses = obtenerLetrasMeses();

            for (int i = 0; i < 6; i++) {
                char estado = (i < historialActual.length()) ? historialActual.charAt(i) : '1';

                if (estado == '1') {
                    g2.setColor(new Color(34, 197, 94)); // Verde
                } else if (estado == '0') {
                    g2.setColor(new Color(239, 68, 68)); // Rojo
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }

                int x = startX + (i * (size + gap));
                g2.fillOval(x, y, size, size);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                String letra = String.valueOf(letrasMeses[i]);

                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (size - fm.stringWidth(letra)) / 2;
                int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent() - 2;
                g2.drawString(letra, textX, textY);
            }
        }
    }

    private void aplicarColorExcel(Component c, boolean isSelected, int row) {
        if (isSelected) {
            c.setBackground(new Color(200, 230, 255)); // Azul Excel
            c.setForeground(Color.BLACK);
        } else {
            if (row % 2 == 0) {
                c.setBackground(Color.WHITE);
            } else {
                c.setBackground(new Color(248, 248, 250));
            }
        }
    }
}
