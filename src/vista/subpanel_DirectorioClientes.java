package vista;

import DAO.ClienteDAO;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;

public class subpanel_DirectorioClientes extends JPanel {

    private DefaultTableModel tableModel;
    private ClienteDAO clienteDAO;
    private JTable tablaClientes;
    private JTextField txtBuscar;

    // --- VARIABLES PARA PAGINACIÓN (VELOCIDAD) ---
    private int paginaActual = 0;
    private final int FILAS_POR_PAGINA = 50; // Carga de 50 en 50
    private JLabel lblPagina;
    private JButton btnAnterior, btnSiguiente;

    public subpanel_DirectorioClientes() {
        setBackground(Color.WHITE);
        setLayout(null);
        clienteDAO = new ClienteDAO();
        initContenido();
        cargarClientes();
    }

    private void initContenido() {
        // TÍTULO
        JLabel lblTitulo = new JLabel("Directorio de Personas (Clientes)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 450, 30);
        add(lblTitulo);

        // BUSCADOR
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por DNI o Apellido...");
        txtBuscar.setBounds(30, 70, 300, 35);
        add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        estilarBoton(btnBuscar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnBuscar.setBounds(340, 70, 50, 35);
        btnBuscar.addActionListener(e -> buscar());
        add(btnBuscar);

        // BOTONES DE ACCIÓN (A la derecha)
        JButton btnEditar = new JButton("✏ Editar Datos");
        estilarBoton(btnEditar, new Color(37, 99, 235), Color.WHITE); // Azul
        btnEditar.setBounds(830, 70, 140, 35);
        add(btnEditar);

        JButton btnEliminar = new JButton("🗑 Eliminar");
        estilarBoton(btnEliminar, new Color(220, 38, 38), Color.WHITE); // Rojo
        btnEliminar.setBounds(980, 70, 130, 35);
        add(btnEliminar);
        
        // ... junto a los otros botones ...
        JButton btnNuevo = new JButton("+ Nuevo Cliente");
        estilarBoton(btnNuevo, new Color(15, 23, 42), Color.WHITE); // Oscuro
        btnNuevo.setBounds(650, 70, 150, 35); // Ajusta la X según tu espacio
        btnNuevo.addActionListener(e -> abrirFormulario(null));
        add(btnNuevo);
        
        // Conectar los otros botones existentes
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        // TABLA
        String[] cols = {"ID", "DNI", "Nombres", "Apellidos", "Dirección", "Correo", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(tableModel);
        tablaClientes.setRowHeight(35);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaClientes.getTableHeader().setBackground(new Color(248, 250, 252));
        tablaClientes.setShowVerticalLines(false);

        // Ocultar ID visualmente
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setBounds(30, 130, 1080, 500); // Un poco menos de alto para dar espacio a paginación
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // --- CONTROLES DE PAGINACIÓN (ABAJO) ---
        btnAnterior = new JButton("🡠 Anterior");
        estilarBoton(btnAnterior, new Color(241, 245, 249), new Color(15, 23, 42));
        btnAnterior.setBounds(30, 640, 120, 35);
        btnAnterior.addActionListener(e -> cambiarPagina(-1));
        add(btnAnterior);

        lblPagina = new JLabel("Página 1");
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPagina.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPagina.setBounds(160, 640, 150, 35);
        add(lblPagina);

        btnSiguiente = new JButton("Siguiente 🡢");
        estilarBoton(btnSiguiente, new Color(241, 245, 249), new Color(15, 23, 42));
        btnSiguiente.setBounds(320, 640, 120, 35);
        btnSiguiente.addActionListener(e -> cambiarPagina(1));
        add(btnSiguiente);
    }

    // --- LÓGICA DE CARGA DE DATOS ---
    private void cargarClientes() {
        // 1. Activar la barra de carga en el Principal
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }

        // Limpiamos la tabla visualmente para dar feedback de que "algo va a pasar"
        tableModel.setRowCount(0);

        // 2. INICIAR HILO EN SEGUNDO PLANO (Esto evita que se congele la pantalla)
        new Thread(() -> {
            try {
                // --- ZONA LENTA (Conexión a Nube) ---
                int offset = paginaActual * FILAS_POR_PAGINA;
                List<Cliente> lista = clienteDAO.obtenerClientesPaginados(FILAS_POR_PAGINA, offset);
                // ------------------------------------

                // 3. VOLVER A LA INTERFAZ GRÁFICA (EDT) PARA PINTAR
                javax.swing.SwingUtilities.invokeLater(() -> {
                    for (Cliente c : lista) {
                        agregarFila(c);
                    }

                    lblPagina.setText("Página " + (paginaActual + 1));

                    // Actualizar botones
                    btnAnterior.setEnabled(paginaActual > 0);
                    // Si trajo menos de 50, es la última página
                    btnSiguiente.setEnabled(lista.size() == FILAS_POR_PAGINA);

                    // Desactivar barra de carga
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                });

            } catch (Exception e) {
                // Manejo de errores también en el hilo gráfico
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                    System.err.println("Error en hilo de carga: " + e.getMessage());
                });
            }
        }).start();
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            cargarClientes(); // Si está vacío, carga normal paginado
            return;
        }

        tableModel.setRowCount(0);
        // La búsqueda sí trae todos los coincidentes (normalmente son pocos)
        List<Cliente> lista = clienteDAO.buscarClientes(texto);
        for (Cliente c : lista) {
            agregarFila(c);
        }

        // En modo búsqueda, desactivamos paginación
        lblPagina.setText("Resultados Búsqueda");
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
    }

    private void cambiarPagina(int direccion) {
        paginaActual += direccion;
        if (paginaActual < 0) {
            paginaActual = 0;
        }
        cargarClientes();
    }

    private void agregarFila(Cliente c) {
        Object[] row = {
            c.getIdCliente(),
            c.getDniCliente(),
            c.getNombres(),
            c.getApellidos(),
            c.getDireccion(),
            c.getCorreo(),
            (c.getActivo() == 1 ? "ACTIVO" : "INACTIVO")
        };
        tableModel.addRow(row);
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // ABRE EL FORMULARIO (Crear o Editar)
    private void abrirFormulario(Cliente cliente) {
        // Obtenemos la ventana padre (Principal) para bloquearla mientras se edita
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        java.awt.Frame parentFrame = (parentWindow instanceof java.awt.Frame) ? (java.awt.Frame) parentWindow : null;

        FormularioCliente form = new FormularioCliente(parentFrame, cliente);
        form.setVisible(true); // Se detiene aquí hasta que cierren el formulario
        
        if (form.isGuardado()) {
            cargarClientes(); // Refrescar la tabla si guardaron algo
        }
    }

    private void editarSeleccionado() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.");
            return;
        }
        
        // Recuperar datos de la tabla para llenar el objeto
        // NOTA: Lo ideal es buscar por ID en BD para tener datos frescos, pero por rapidez usamos la tabla
        Cliente c = new Cliente();
        c.setIdCliente(Long.parseLong(tableModel.getValueAt(fila, 0).toString()));
        c.setDniCliente(tableModel.getValueAt(fila, 1).toString());
        c.setNombres(tableModel.getValueAt(fila, 2).toString());
        c.setApellidos(tableModel.getValueAt(fila, 3).toString());
        c.setDireccion(tableModel.getValueAt(fila, 4).toString());
        c.setCorreo(tableModel.getValueAt(fila, 5).toString());
        // El teléfono no lo mostramos en la tabla, deberías agregarlo al modelo o buscarlo en BD
        
        abrirFormulario(c);
    }

    private void eliminarSeleccionado() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para eliminar.");
            return;
        }
        
        String nombre = tableModel.getValueAt(fila, 2) + " " + tableModel.getValueAt(fila, 3);
        Long id = Long.parseLong(tableModel.getValueAt(fila, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Seguro que desea dar de baja a: " + nombre + "?\nEsto podría afectar sus contratos activos.",
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            if (clienteDAO.eliminarCliente(id)) {
                JOptionPane.showMessageDialog(this, "Cliente dado de baja.");
                cargarClientes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.");
            }
        }
    }
}
