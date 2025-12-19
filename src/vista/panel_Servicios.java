package vista;

import DAO.ServicioDAO;
import modelo.Servicio;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class panel_Servicios extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private ServicioDAO servicioDAO;
    private JTextField txtBuscar;

    public panel_Servicios() {
        // Inicializar DAO
        servicioDAO = new ServicioDAO();

        // Configuración del Panel Principal
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(241, 245, 249)); // Fondo gris claro

        initUI();       // Construimos la interfaz manualmente
        cargarDatos(""); // Cargamos los datos de la BD
    }

    private void initUI() {
        // --- ZONA SUPERIOR (Header) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(getBackground());
        pnlHeader.setPreferredSize(new Dimension(0, 80));
        pnlHeader.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Título y Subtítulo
        JPanel pnlTitulos = new JPanel(new BorderLayout());
        pnlTitulos.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("Catálogo de Servicios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        
        JLabel lblSub = new JLabel("Gestiona los planes de internet y precios");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        
        pnlTitulos.add(lblTitulo, BorderLayout.NORTH);
        pnlTitulos.add(lblSub, BorderLayout.SOUTH);
        pnlHeader.add(pnlTitulos, BorderLayout.WEST);

        // --- CONTROLES (Derecha) ---
        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlControles.setOpaque(false);

        // Buscador
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar plan...");
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cargarDatos(txtBuscar.getText());
            }
        });
        
        // Botones
        JButton btnNuevo = createButton("+ Nuevo Plan", new Color(37, 99, 235), Color.WHITE);
        btnNuevo.addActionListener(e -> abrirDialogo(null));

        JButton btnEditar = createButton("Editar", new Color(234, 179, 8), Color.WHITE);
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnEliminar = createButton("Eliminar", new Color(220, 38, 38), Color.WHITE);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        
        pnlControles.add(txtBuscar);
        pnlControles.add(btnNuevo);
        pnlControles.add(btnEditar);
        pnlControles.add(btnEliminar);
        
        pnlHeader.add(pnlControles, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- ZONA CENTRAL (Tabla) ---
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(new EmptyBorder(0, 30, 30, 30));

        String[] columnas = {"ID", "Descripción del Plan", "Velocidad", "Mensualidad", "Estado", "OBJ"};
        
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(40);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ocultar columnas ID y Objeto
        tabla.getColumnModel().getColumn(0).setMinWidth(0); tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(5).setMinWidth(0); tabla.getColumnModel().getColumn(5).setMaxWidth(0);
        
        // APLICAR RENDERER (Esto es lo que fallaba antes)
        tabla.setDefaultRenderer(Object.class, new ServicioRenderer());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        pnlTabla.add(scroll, BorderLayout.CENTER);
        add(pnlTabla, BorderLayout.CENTER);
    }

    // --- LÓGICA DE NEGOCIO ---

    private void cargarDatos(String busqueda) {
        modelo.setRowCount(0);
        try {
            List<Servicio> lista = servicioDAO.listar(busqueda);
            for (Servicio s : lista) {
                modelo.addRow(new Object[]{
                    s.getIdServicio(),
                    s.getDescripcion(),
                    s.getVelocidadMb() + " MB",
                    String.format("S/. %.2f", s.getMensualidad()),
                    (s.getActivo() == 1 ? "Activo" : "Inactivo"),
                    s
                });
            }
        } catch (Exception e) {
            System.err.println("Error cargando servicios: " + e.getMessage());
        }
    }

    private void abrirDialogo(Servicio servicioEditar) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        // Asegúrate de tener la clase DialogoServicio creada como te pasé antes
        DialogoServicio dialogo = new DialogoServicio((Frame) parent, servicioEditar);
        dialogo.setVisible(true);
        
        if (dialogo.isGuardado()) {
            cargarDatos("");
        }
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio para editar.");
            return;
        }
        Servicio s = (Servicio) modelo.getValueAt(fila, 5);
        abrirDialogo(s);
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un servicio.");
            return;
        }
        Servicio s = (Servicio) modelo.getValueAt(fila, 5);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Eliminar el plan: " + s.getDescripcion() + "?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (servicioDAO.eliminar(s.getIdServicio())) {
                cargarDatos("");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar.");
            }
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    // --- CLASE INTERNA RENDERER (ESTA ES LA QUE FALTABA) ---
    class ServicioRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));

            if (!isSelected) {
                if (row % 2 == 0) setBackground(Color.WHITE);
                else setBackground(new Color(249, 250, 251)); 
                setForeground(new Color(51, 65, 85));
            } else {
                setBackground(new Color(37, 99, 235));
                setForeground(Color.WHITE);
            }

            if (column == 4) { // Columna Estado
                String estado = (String) value;
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if ("Activo".equals(estado)) {
                    setForeground(isSelected ? Color.WHITE : new Color(22, 163, 74));
                } else {
                    setForeground(isSelected ? Color.WHITE : new Color(220, 38, 38));
                }
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
            
            return this;
        }
    }
}