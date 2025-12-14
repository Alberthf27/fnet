package vista;

import DAO.ClienteDAO;
import modelo.Cliente;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_DirectorioClientes extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private ClienteDAO clienteDAO;
    private JTextField txtBuscar;
    private JComboBox<String> cmbFiltro; // NUEVO FILTRO

    // Paginación
    private int paginaActual = 0;
    private final int FILAS_POR_PAGINA = 50;
    private JLabel lblPagina;
    private JButton btnAnterior, btnSiguiente;

    // --- AUTOCOMPLETADO (Google Style) ---
    private JPopupMenu menuSugerencias;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;
    private List<String[]> clientesCache = new ArrayList<>(); // [dni, nombre_completo]

    public subpanel_DirectorioClientes() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        clienteDAO = new ClienteDAO();
        precargarCacheBusqueda(); // Carga rápida para buscador
        initUI();
        //initAutocompletado();
        cargarClientes();
    }

    private void precargarCacheBusqueda() {
        new Thread(() -> {
            // Asumo que tu DAO tiene un método ligero para traer solo DNI y Nombre
            // Si no, usa obtenerListaSimpleClientes que usaste en Suscripciones
            clientesCache = clienteDAO.obtenerListaSimpleClientes();
        }).start();
    }

    private void initUI() {
        // --- 1. PANEL SUPERIOR ---
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("DIRECTORIO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 10, 120, 30);
        topPanel.add(lblTitulo);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar DNI o Nombre...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBounds(130, 10, 230, 30);
        topPanel.add(txtBuscar);

        // FILTROS (NUEVO)
        cmbFiltro = new JComboBox<>(new String[]{"NOMBRE (A-Z)", "APELLIDO (A-Z)", "SOLO ACTIVOS", "SOLO BAJAS"});
        cmbFiltro.setBounds(370, 10, 120, 30);
        cmbFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbFiltro.setBackground(Color.WHITE);
        cmbFiltro.addActionListener(e -> {
            paginaActual = 0; // Resetear página al filtrar
            cargarClientes();
        });
        topPanel.add(cmbFiltro);

        JButton btnBuscar = new JButton("🔍");
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource("/img/lupa.png"));
            Image img = icono.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnBuscar.setIcon(new ImageIcon(img));
            btnBuscar.setText("");
        } catch (Exception e) {
            btnBuscar.setText("🔍");
        }

        btnBuscar.setBounds(500, 10, 40, 30);
        btnBuscar.addActionListener(e -> buscar(txtBuscar.getText()));
        estilarBoton(btnBuscar, new Color(241, 245, 249), Color.BLACK);
        topPanel.add(btnBuscar);

        // BOTONES ACCIÓN
        int xAccion = 560;
        JButton btnNuevo = new JButton("+ NUEVO");
        estilarBoton(btnNuevo, new Color(22, 163, 74), Color.WHITE);
        btnNuevo.setBounds(xAccion, 10, 90, 30);
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        topPanel.add(btnNuevo);

        JButton btnEditar = new JButton("EDITAR");
        estilarBoton(btnEditar, new Color(37, 99, 235), Color.WHITE);
        btnEditar.setBounds(xAccion + 100, 10, 80, 30);
        btnEditar.addActionListener(e -> editarSeleccionado());
        topPanel.add(btnEditar);

        JButton btnEliminar = new JButton("BAJA");
        estilarBoton(btnEliminar, new Color(220, 38, 38), Color.WHITE);
        btnEliminar.setBounds(xAccion + 190, 10, 70, 30);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        topPanel.add(btnEliminar);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABLA ---
        String[] cols = {"ID", "DNI", "NOMBRES", "APELLIDOS", "DIRECCIÓN", "TELÉFONO", "ESTADO", "OBJ"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(220, 220, 220));

        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(7).setMinWidth(0);
        tabla.getColumnModel().getColumn(7).setMaxWidth(0);

        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(70);

        tabla.setDefaultRenderer(Object.class, new GeneralRenderer());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scroll, BorderLayout.CENTER);

        // --- 3. PAGINACIÓN ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        btnAnterior = new JButton("<");
        estilarBoton(btnAnterior, Color.WHITE, Color.BLACK);
        btnAnterior.addActionListener(e -> cambiarPagina(-1));

        lblPagina = new JLabel("Página 1");
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnSiguiente = new JButton(">");
        estilarBoton(btnSiguiente, Color.WHITE, Color.BLACK);
        btnSiguiente.addActionListener(e -> cambiarPagina(1));

        bottomPanel.add(btnAnterior);
        bottomPanel.add(lblPagina);
        bottomPanel.add(btnSiguiente);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- LÓGICA AUTOCOMPLETADO ---
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
                    buscar(txtBuscar.getText());
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
            // Verificamos que cli[0] (DNI) no sea null antes de usarlo
            String dni = cli[0];
            String nombre = cli[1];

            boolean coincideDni = (dni != null && dni.startsWith(texto));
            boolean coincideNombre = (nombre != null && nombre.toLowerCase().contains(texto));

            if (coincideDni || coincideNombre) {
                // ... el resto sigue igual
                modeloSugerencias.addElement(cli[1]); // Muestra nombre
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
            buscar(sel);
        }
    }

    // --- CARGA DE DATOS ---
    private void cargarClientes() {
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }
        modelo.setRowCount(0);

        new Thread(() -> {
            try {
                int offset = paginaActual * FILAS_POR_PAGINA;
                String filtro = (String) cmbFiltro.getSelectedItem();

                // NOTA: Asegúrate de que tu DAO acepte el parámetro 'filtro'
                // Si no, modifica tu DAO para manejar el ORDER BY según este String
                List<Cliente> lista = clienteDAO.obtenerClientesPaginados(FILAS_POR_PAGINA, offset, filtro);

                SwingUtilities.invokeLater(() -> {
                    for (Cliente c : lista) {
                        agregarFila(c);
                    }
                    lblPagina.setText("Página " + (paginaActual + 1));
                    btnAnterior.setEnabled(paginaActual > 0);
                    // Lógica simple: si trajo menos filas que el límite, no hay más pág.
                    btnSiguiente.setEnabled(lista.size() == FILAS_POR_PAGINA);

                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void buscar(String texto) {
        if (texto.trim().isEmpty()) {
            paginaActual = 0;
            cargarClientes();
            return;
        }

        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }
        modelo.setRowCount(0);

        new Thread(() -> {
            // El buscador ignora la paginación para mostrar coincidencias directas
            List<Cliente> lista = clienteDAO.buscarClientes(texto);
            SwingUtilities.invokeLater(() -> {
                for (Cliente c : lista) {
                    agregarFila(c);
                }
                lblPagina.setText("Resultados: " + lista.size());
                btnAnterior.setEnabled(false);
                btnSiguiente.setEnabled(false);
                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    private void cambiarPagina(int dir) {
        paginaActual += dir;
        if (paginaActual < 0) {
            paginaActual = 0;
        }
        cargarClientes();
    }

    private void agregarFila(Cliente c) {
        modelo.addRow(new Object[]{
            c.getIdCliente(),
            c.getDniCliente() != null ? c.getDniCliente() : "---",
            c.getNombres(),
            c.getApellidos() != null ? c.getApellidos() : "",
            c.getDireccion() != null ? c.getDireccion() : "",
            c.getTelefono() != null ? c.getTelefono() : "---",
            (c.getActivo() == 1 ? "ACTIVO" : "BAJA"),
            c
        });
    }

    // --- ACCIONES ---
    private void abrirFormulario(Cliente c) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        FormularioCliente form = new FormularioCliente((Frame) parent, c);
        form.setVisible(true);
        if (form.isGuardado()) {
            precargarCacheBusqueda(); // Actualizar cache si se creó uno nuevo
            cargarClientes();
        }
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }
        Cliente c = (Cliente) modelo.getValueAt(fila, 7);
        abrirFormulario(c);
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }
        Cliente c = (Cliente) modelo.getValueAt(fila, 7);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a " + c.getNombres() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (clienteDAO.eliminarCliente(c.getIdCliente())) {
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }

    // --- ESTILOS ---
    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
    }

    class GeneralRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row, int col) {
            super.getTableCellRendererComponent(table, value, isS, hasF, row, col);

            if (col == 6) { // Estado
                String estado = (String) value;
                if ("BAJA".equals(estado)) {
                    setForeground(Color.RED);
                } else {
                    setForeground(new Color(0, 128, 0));
                }
                setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                setForeground(Color.BLACK);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }

            if (isS) {
                setBackground(new Color(200, 230, 255));
                setForeground(Color.BLACK);
            } else {
                if (row % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(248, 248, 250));
                }
            }
            setBorder(new EmptyBorder(0, 5, 0, 0));
            return this;
        }
    }
}
