package vista;

import DAO.ClienteDAO;
import modelo.Cliente;
import java.awt.*;
import java.awt.event.*;
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
    
    // Paginación
    private int paginaActual = 0;
    private final int FILAS_POR_PAGINA = 50;
    private JLabel lblPagina;
    private JButton btnAnterior, btnSiguiente;

    public subpanel_DirectorioClientes() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Margen pequeño
        
        clienteDAO = new ClienteDAO();
        initUI();
        cargarClientes();
    }

    private void initUI() {
        // --- 1. PANEL SUPERIOR ---
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("DIRECTORIO PERSONAS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 10, 250, 30);
        topPanel.add(lblTitulo);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por DNI o Apellido...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.setBounds(260, 10, 250, 30);
        txtBuscar.addActionListener(e -> buscar()); // Enter para buscar
        topPanel.add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        // Intento cargar icono lupa (opcional)
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource("/img/lupa.png"));
            Image img = icono.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnBuscar.setIcon(new ImageIcon(img));
            btnBuscar.setText("");
        } catch (Exception e) { btnBuscar.setText("🔍"); }
        
        btnBuscar.setBounds(520, 10, 40, 30);
        btnBuscar.addActionListener(e -> buscar());
        estilarBoton(btnBuscar, new Color(241, 245, 249), Color.BLACK);
        topPanel.add(btnBuscar);

        // BOTONES ACCIÓN
        int xAccion = 600;
        JButton btnNuevo = new JButton("+ NUEVO");
        estilarBoton(btnNuevo, new Color(22, 163, 74), Color.WHITE); // Verde
        btnNuevo.setBounds(xAccion, 10, 100, 30);
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        topPanel.add(btnNuevo);

        JButton btnEditar = new JButton("EDITAR");
        estilarBoton(btnEditar, new Color(37, 99, 235), Color.WHITE); // Azul
        btnEditar.setBounds(xAccion + 110, 10, 90, 30);
        btnEditar.addActionListener(e -> editarSeleccionado());
        topPanel.add(btnEditar);

        JButton btnEliminar = new JButton("ELIMINAR");
        estilarBoton(btnEliminar, new Color(220, 38, 38), Color.WHITE); // Rojo
        btnEliminar.setBounds(xAccion + 210, 10, 100, 30);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        topPanel.add(btnEliminar);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABLA ESTILO EXCEL ---
        String[] cols = {"ID", "DNI", "NOMBRES", "APELLIDOS", "DIRECCIÓN", "TELÉFONO", "ESTADO", "OBJ"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(25); // Filas compactas
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(220, 220, 220));
        
        // Ocultar ID y Objeto
        tabla.getColumnModel().getColumn(0).setMinWidth(0); tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(7).setMinWidth(0); tabla.getColumnModel().getColumn(7).setMaxWidth(0);
        
        // Anchos
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);  // DNI
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150); // Nombres
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150); // Apellidos
        tabla.getColumnModel().getColumn(4).setPreferredWidth(250); // Dirección (Más espacio)
        tabla.getColumnModel().getColumn(5).setPreferredWidth(90);  // Teléfono
        tabla.getColumnModel().getColumn(6).setPreferredWidth(70);  // Estado

        // Renderizadores
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

    // --- CARGA DE DATOS ---
    private void cargarClientes() {
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        modelo.setRowCount(0);

        new Thread(() -> {
            try {
                int offset = paginaActual * FILAS_POR_PAGINA;
                List<Cliente> lista = clienteDAO.obtenerClientesPaginados(FILAS_POR_PAGINA, offset);

                SwingUtilities.invokeLater(() -> {
                    for (Cliente c : lista) {
                        agregarFila(c);
                    }
                    lblPagina.setText("Página " + (paginaActual + 1));
                    btnAnterior.setEnabled(paginaActual > 0);
                    btnSiguiente.setEnabled(lista.size() == FILAS_POR_PAGINA); // Si llena la pág, hay más

                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            paginaActual = 0;
            cargarClientes();
            return;
        }
        
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        modelo.setRowCount(0);
        
        new Thread(() -> {
            List<Cliente> lista = clienteDAO.buscarClientes(texto);
            SwingUtilities.invokeLater(() -> {
                for (Cliente c : lista) agregarFila(c);
                lblPagina.setText("Resultados: " + lista.size());
                btnAnterior.setEnabled(false);
                btnSiguiente.setEnabled(false);
                if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
            });
        }).start();
    }

    private void cambiarPagina(int dir) {
        paginaActual += dir;
        if(paginaActual < 0) paginaActual = 0;
        cargarClientes();
    }

    private void agregarFila(Cliente c) {
        modelo.addRow(new Object[]{
            c.getIdCliente(),
            c.getDniCliente() != null ? c.getDniCliente() : "---",
            c.getNombres(),
            c.getApellidos() != null ? c.getApellidos() : "",
            c.getDireccion() != null ? c.getDireccion() : "",
            c.getTelefono() != null ? c.getTelefono() : "---", // Mostramos Teléfono real
            (c.getActivo() == 1 ? "ACTIVO" : "BAJA"),
            c // Objeto oculto
        });
    }

    // --- ACCIONES ---
    private void abrirFormulario(Cliente c) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        FormularioCliente form = new FormularioCliente((Frame) parent, c);
        form.setVisible(true);
        if (form.isGuardado()) cargarClientes();
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return; }
        Cliente c = (Cliente) modelo.getValueAt(fila, 7); // Objeto completo
        abrirFormulario(c);
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { JOptionPane.showMessageDialog(this, "Seleccione un cliente."); return; }
        Cliente c = (Cliente) modelo.getValueAt(fila, 7);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Dar de baja a " + c.getNombres() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if(confirm == JOptionPane.YES_OPTION) {
            if(clienteDAO.eliminarCliente(c.getIdCliente())) {
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
                if ("BAJA".equals(estado)) setForeground(Color.RED);
                else setForeground(new Color(0, 128, 0));
                setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                setForeground(Color.BLACK);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }

            if (isS) {
                setBackground(new Color(200, 230, 255));
                setForeground(Color.BLACK);
            } else {
                if (row % 2 == 0) setBackground(Color.WHITE);
                else setBackground(new Color(248, 248, 250));
            }
            
            setBorder(new EmptyBorder(0, 5, 0, 0)); // Padding texto
            return this;
        }
    }
}