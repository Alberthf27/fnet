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
    private JComboBox<String> cmbFiltro;

    // Etiquetas de conteo
    private JLabel lblTotalRegistros;

    // Paginación
    private int paginaActual = 0;
    private final int FILAS_POR_PAGINA = 50;
    private JLabel lblPagina;
    private JButton btnAnterior, btnSiguiente;

    // --- AUTOCOMPLETADO (Igual que Suscripciones) ---
    private JPopupMenu menuSugerencias;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;
    private List<String[]> clientesCache = new ArrayList<>(); // [0]=DNI, [1]=NOMBRE COMPLETO
    private List<Cliente> listaCache = new ArrayList<>();

    public subpanel_DirectorioClientes() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        clienteDAO = new ClienteDAO();
        precargarCacheBusqueda(); // Carga nombres y DNI en memoria
        initUI();
        initAutocompletado(); // Activamos el buscador inteligente
        cargarClientes(); // Carga inicial paginada
    }

    private void precargarCacheBusqueda() {
        new Thread(() -> {
            // Usamos obtenerListaSimpleClientes que devuelve [dni, nombre]
            // Asegúrate de que este método exista en ClienteDAO (lo usamos en
            // Suscripciones)
            clientesCache = clienteDAO.obtenerListaSimpleClientes();
        }).start();
    }

    private void initUI() {
        // --- 1. PANEL SUPERIOR (HEADER) ---
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("DIRECTORIO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Aumentado de 18 a 20
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 10, 150, 30);
        topPanel.add(lblTitulo);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar DNI o Nombre...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Aumentado de 12 a 14
        txtBuscar.setBounds(155, 10, 250, 30);
        topPanel.add(txtBuscar);

        // FILTROS
        cmbFiltro = new JComboBox<>(new String[] { "NOMBRE (A-Z)", "APELLIDO (A-Z)", "SOLO ACTIVOS", "SOLO BAJAS" });
        cmbFiltro.setBounds(415, 10, 120, 30);
        cmbFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Aumentado de 11 a 12
        cmbFiltro.setBackground(Color.WHITE);
        cmbFiltro.addActionListener(e -> {
            paginaActual = 0;
            cargarClientes();
        });
        topPanel.add(cmbFiltro);

        JButton btnBuscar = new JButton("🔍");
        estilarBoton(btnBuscar, new Color(241, 245, 249), Color.BLACK);
        btnBuscar.setBounds(545, 10, 40, 30);
        btnBuscar.addActionListener(e -> buscar(txtBuscar.getText()));
        topPanel.add(btnBuscar);

        // BOTONES ACCIÓN
        int xAccion = 600;
        JButton btnNuevo = new JButton("+ NUEVO");
        estilarBoton(btnNuevo, new Color(22, 163, 74), Color.WHITE);
        btnNuevo.setBounds(xAccion, 10, 95, 30);
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
        String[] cols = { "ID", "DNI", "NOMBRES", "APELLIDOS", "DIRECCIÓN", "TELÉFONO", "ESTADO", "OBJ" };
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(30); // Aumentado de 25 a 30 para mejor legibilidad
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Aumentado de 12 a 13
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(220, 220, 220));

        // Ocultar columnas internas
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(7).setMinWidth(0);
        tabla.getColumnModel().getColumn(7).setMaxWidth(0);

        // Anchos
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

        // --- 3. FOOTER (PAGINACIÓN + TOTAL) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Izquierda: Contador Total
        lblTotalRegistros = new JLabel("Total Clientes: 0");
        lblTotalRegistros.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalRegistros.setForeground(Color.GRAY);
        bottomPanel.add(lblTotalRegistros, BorderLayout.WEST);

        // Centro: Controles Paginación
        JPanel pnlPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlPaginacion.setBackground(Color.WHITE);

        btnAnterior = new JButton("<");
        estilarBoton(btnAnterior, Color.WHITE, Color.BLACK);
        btnAnterior.addActionListener(e -> cambiarPagina(-1));

        lblPagina = new JLabel("Página 1");
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnSiguiente = new JButton(">");
        estilarBoton(btnSiguiente, Color.WHITE, Color.BLACK);
        btnSiguiente.addActionListener(e -> cambiarPagina(1));

        pnlPaginacion.add(btnAnterior);
        pnlPaginacion.add(lblPagina);
        pnlPaginacion.add(btnSiguiente);

        bottomPanel.add(pnlPaginacion, BorderLayout.CENTER);

        // Derecha: Espacio vacío para equilibrar o poner algo más
        JPanel pnlRight = new JPanel();
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setPreferredSize(new Dimension(100, 10));
        bottomPanel.add(pnlRight, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- LÓGICA AUTOCOMPLETADO (Igual que Suscripciones) ---
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
                    cargarClientes(); // Recarga desde BD si da Enter
                } else {
                    filtrarLocalmente(txtBuscar.getText()); // Filtra en memoria mientras escribe
                }
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
            if (count > 10)
                break;

            String dni = cli[0];
            String nombre = cli[1];

            boolean coincideDni = (dni != null && dni.startsWith(texto));
            boolean coincideNombre = (nombre != null && nombre.toLowerCase().contains(texto));

            if (coincideDni || coincideNombre) {
                modeloSugerencias.addElement(nombre); // Mostrar nombre en la lista
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

    // Método auxiliar para agregar una fila al modelo de la tabla
    private void agregarFila(Cliente c) {
        modelo.addRow(new Object[] {
                c.getIdCliente(),
                c.getDniCliente() != null ? c.getDniCliente() : "---",
                c.getNombres(),
                c.getApellidos() != null ? c.getApellidos() : "",
                c.getDireccion() != null ? c.getDireccion() : "",
                c.getTelefono() != null ? c.getTelefono() : "---",
                (c.getActivo() == 1 ? "ACTIVO" : "BAJA"), // Estado visual
                c // Objeto oculto en la columna 7 (importante para editar/eliminar)
        });
    }

    // --- CARGA DE DATOS ---
    // REEMPLAZA TU MÉTODO cargarClientes() POR ESTE:
    private void cargarClientes() {
        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);
        modelo.setRowCount(0);

        new Thread(() -> {
            try {
                String filtro = (String) cmbFiltro.getSelectedItem();
                // Usamos el nuevo método del DAO para traer TODO
                List<Cliente> listaTraida = clienteDAO.listarTodo("", filtro);

                // Guardamos en memoria
                listaCache = new ArrayList<>(listaTraida);

                SwingUtilities.invokeLater(() -> {
                    llenarTabla(listaCache);
                    if (Principal.instancia != null)
                        Principal.instancia.mostrarCarga(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // AGREGA ESTE MÉTODO NUEVO (Para buscar letra por letra en el caché):
    private void filtrarLocalmente(String texto) {
        String busqueda = texto.trim().toLowerCase();
        List<Cliente> resultados = new ArrayList<>();

        for (Cliente c : listaCache) {
            // Buscamos coincidencia en cualquier campo importante
            if ((c.getNombres() != null && c.getNombres().toLowerCase().contains(busqueda)) ||
                    (c.getApellidos() != null && c.getApellidos().toLowerCase().contains(busqueda)) ||
                    (c.getDniCliente() != null && c.getDniCliente().contains(busqueda))) {
                resultados.add(c);
            }
        }
        llenarTabla(resultados);
    }

    // MODIFICA TU MÉTODO llenarTabla (o agregarFila) PARA ACTUALIZAR EL CONTADOR
    private void llenarTabla(List<Cliente> lista) {
        modelo.setRowCount(0);
        for (Cliente c : lista) {
            agregarFila(c); // Usa tu método existente agregarFila
        }
        // Actualiza el label total
        lblTotalRegistros.setText("Total Clientes: " + lista.size());
    }

    private void buscar(String texto) {
        if (texto.trim().isEmpty()) {
            paginaActual = 0;
            cargarClientes();
            return;
        }

        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);
        modelo.setRowCount(0);

        new Thread(() -> {
            // El buscador ignora la paginación para mostrar coincidencias directas de la BD
            List<Cliente> lista = clienteDAO.buscarClientes(texto);
            SwingUtilities.invokeLater(() -> {
                llenarTabla(lista);

                lblPagina.setText("Resultados: " + lista.size());
                btnAnterior.setEnabled(false);
                btnSiguiente.setEnabled(false);

                if (Principal.instancia != null)
                    Principal.instancia.mostrarCarga(false);
            });
        }).start();
    }

    private void cambiarPagina(int dir) {
        paginaActual += dir;
        if (paginaActual < 0)
            paginaActual = 0;
        cargarClientes();
    }

    // --- ACCIONES ---
    private void abrirFormulario(Cliente c) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        FormularioCliente form = new FormularioCliente((Frame) parent, c);
        form.setVisible(true);
        if (form.isGuardado()) {
            precargarCacheBusqueda(); // Actualizar cache
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
        int confirm = JOptionPane.showConfirmDialog(this, "¿Dar de baja a " + c.getNombres() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row,
                int col) {
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
                setBackground((row % 2 == 0) ? Color.WHITE : new Color(248, 248, 250));
            }
            setBorder(new EmptyBorder(0, 5, 0, 0));
            return this;
        }
    }
}