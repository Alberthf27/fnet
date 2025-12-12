package vista;

import DAO.SuscripcionDAO;
import modelo.Suscripcion;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_Suscripciones extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private SuscripcionDAO susDAO;
    private PanelDetalleContrato panelDetalle;
    private JTextField txtBuscar;
    private JComboBox<String> cmbOrden;
    private JLabel lblTotalContratos;

    // --- AUTOCOMPLETADO ---
    private JPopupMenu menuSugerencias;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;
    private List<String[]> clientesCache = new ArrayList<>();

    public subpanel_Suscripciones() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        susDAO = new SuscripcionDAO();
        precargarClientes();
        initUI();
        initAutocompletado();
        cargarDatos("");
    }

    private void precargarClientes() {
        new Thread(() -> {
            DAO.ClienteDAO dao = new DAO.ClienteDAO();
            clientesCache = dao.obtenerListaSimpleClientes();
        }).start();
    }

    private void initUI() {
        // --- 1. PANEL SUPERIOR COMPACTO ---
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("CONTRATOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 10, 120, 30);
        topPanel.add(lblTitulo);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar cliente...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBounds(130, 10, 250, 30);
        topPanel.add(txtBuscar);

        // FILTROS
        cmbOrden = new JComboBox<>(new String[]{"DIA DE PAGO", "MÁS RECIENTES", "MÁS ANTIGUOS", "NOMBRE (A-Z)", "DEUDORES"});
        cmbOrden.setBounds(390, 10, 130, 30);
        cmbOrden.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbOrden.addActionListener(e -> cargarDatos(txtBuscar.getText()));
        cmbOrden.setBackground(Color.WHITE);
        topPanel.add(cmbOrden);

        JButton btnBuscar = new JButton();
        // Cargar Icono Lupa
        try {
            // Asegúrate que la imagen está en src/img/lupa.png
            ImageIcon icono = new ImageIcon(getClass().getResource("/img/lupa.png"));
            // Escalar imagen si es muy grande (opcional, ajusta 20,20 al tamaño deseado)
            Image img = icono.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btnBuscar.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            btnBuscar.setText("🔍"); // Fallback si no encuentra la imagen
        }

        btnBuscar.setBounds(530, 10, 40, 30);
        btnBuscar.addActionListener(e -> cargarDatos(txtBuscar.getText()));
        estilarBoton(btnBuscar, new Color(241, 245, 249), Color.BLACK);
        topPanel.add(btnBuscar);

        // BOTONES ACCIÓN
        int xAccion = 600;
        JButton btnCortar = new JButton("CORTAR");
        estilarBoton(btnCortar, new Color(220, 38, 38), Color.WHITE);
        btnCortar.setBounds(xAccion, 10, 80, 30);
        btnCortar.addActionListener(e -> cambiarEstadoServicio(0));
        topPanel.add(btnCortar);

        JButton btnActivar = new JButton("ACTIVAR");
        estilarBoton(btnActivar, new Color(22, 163, 74), Color.WHITE);
        btnActivar.setBounds(xAccion + 90, 10, 90, 30);
        btnActivar.addActionListener(e -> cambiarEstadoServicio(1));
        topPanel.add(btnActivar);

        JButton btnEditar = new JButton("EDITAR");
        estilarBoton(btnEditar, new Color(234, 179, 8), Color.WHITE);
        btnEditar.setBounds(xAccion + 190, 10, 80, 30);
        btnEditar.addActionListener(e -> abrirEdicion());
        topPanel.add(btnEditar);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABLA ESTILO EXCEL ---
        String[] cols = {"ID", "CLIENTE", "PLAN", "MONTO", "DIA", "ESTADO", "HISTORIAL", "OBJ"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(25); // Compacto
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(220, 220, 220));

        // Ocultar Columnas ID y OBJ
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(7).setMinWidth(0);
        tabla.getColumnModel().getColumn(7).setMaxWidth(0);

        // Anchos Optimizados
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220); // Cliente
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Plan
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60);  // Monto
        tabla.getColumnModel().getColumn(4).setPreferredWidth(40);  // Dia
        tabla.getColumnModel().getColumn(5).setPreferredWidth(70);  // Estado
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120); // Historial

        // Renderizadores
        tabla.getColumnModel().getColumn(6).setCellRenderer(new HistorialRendererCompacto());
        tabla.getColumnModel().getColumn(1).setCellRenderer(new ClienteRenderer());
        tabla.setDefaultRenderer(Object.class, new GeneralRenderer());

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        panelDetalle = new PanelDetalleContrato();

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                Suscripcion s = (Suscripcion) modelo.getValueAt(tabla.getSelectedRow(), 7);
                panelDetalle.mostrarDatos(s);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTabla, panelDetalle);
        split.setBorder(null);
        split.setDividerSize(3); // Borde delgado
        
        // MAGIA PARA QUE NO SE ESTIRE EL DETALLE:
        // 1. Le decimos que TODO el espacio extra (1.0) se lo de a la izquierda (Tabla)
        split.setResizeWeight(1.0); 
        
        // 2. Fijamos el ancho del panel de detalle
        panelDetalle.setMinimumSize(new Dimension(350, 0));
        panelDetalle.setPreferredSize(new Dimension(380, 0));

        add(split, BorderLayout.CENTER);

        // --- 3. PANEL INFERIOR (CONTADOR) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        lblTotalContratos = new JLabel("Total Contratos: 0");
        lblTotalContratos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalContratos.setForeground(Color.GRAY);
        bottomPanel.add(lblTotalContratos);

        add(bottomPanel, BorderLayout.SOUTH);
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
        for (String[] cli : clientesCache) {
            if (count > 10) {
                break;
            }
            if (cli[0].startsWith(texto) || cli[1].toLowerCase().contains(texto)) {
                modeloSugerencias.addElement(cli[1]);
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

    private void cargarDatos(String busqueda) {
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }
        int selectedRow = tabla.getSelectedRow();
        modelo.setRowCount(0);

        new Thread(() -> {
            String orden = (String) cmbOrden.getSelectedItem();
            List<Suscripcion> lista = susDAO.listarTodo(busqueda, orden);

            SwingUtilities.invokeLater(() -> {
                for (Suscripcion s : lista) {
                    modelo.addRow(new Object[]{
                        s.getIdSuscripcion(),
                        s.getNombreCliente(),
                        s.getNombreServicio(),
                        "S/. " + s.getMontoMensual(),
                        s.getDiaPago(),
                        s.getActivo() == 1 ? "ACTIVO" : "CORTADO",
                        s.getHistorialPagos(),
                        s
                    });
                }
                lblTotalContratos.setText("Total Contratos: " + lista.size());

                if (selectedRow != -1 && selectedRow < modelo.getRowCount()) {
                    tabla.setRowSelectionInterval(selectedRow, selectedRow);
                }
                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    // --- ACCIONES ---
    private void cambiarEstadoServicio(int nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contrato.");
            return;
        }
        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);
        String accion = nuevoEstado == 1 ? "ACTIVAR" : "CORTAR";

        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea " + accion + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Principal.instancia != null) {
                Principal.instancia.mostrarCarga(true);
            }
            new Thread(() -> {
                susDAO.cambiarEstado(s.getIdSuscripcion(), nuevoEstado);
                cargarDatos(txtBuscar.getText());
            }).start();
        }
    }

    private void abrirEdicion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        Suscripcion s = (Suscripcion) modelo.getValueAt(fila, 7);
        int idCliente = susDAO.obtenerIdClienteDeContrato(s.getIdSuscripcion());

        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        DialogoEditarContrato dialog = new DialogoEditarContrato(
                (java.awt.Frame) parent,
                s.getIdSuscripcion(),
                s.getNombreServicio(),
                s.getDireccionInstalacion(),
                idCliente,
                s.getNombreCliente()
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row, int col) {
            super.getTableCellRendererComponent(table, value, isS, hasF, row, col);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(0, 5, 0, 0));
            aplicarColorExcel(this, isS, row);
            return this;
        }
    }

    class GeneralRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row, int col) {
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row, int col) {
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
