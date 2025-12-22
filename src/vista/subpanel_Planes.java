package vista;

import DAO.ServicioDAO;
import modelo.Servicio;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class subpanel_Planes extends JPanel {

    private final ServicioDAO servicioDAO;

    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;

    // Campos del Formulario
    private JTextField txtNombre, txtPrecio, txtVelocidad;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbTipo;
    private JButton btnGuardar, btnLimpiar, btnEliminar;

    // ID del servicio seleccionado para edición
    private int idServicioSeleccionado = -1;

    public subpanel_Planes() {
        this.servicioDAO = new ServicioDAO();
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
        cargarDatosDesdeDB();
    }

    private void initContenido() {
        // ====================================================================
        // COLUMNA IZQUIERDA: LISTADO DE PLANES
        // ====================================================================
        JLabel lblTitulo = new JLabel("Catálogo de Servicios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 300, 30);
        add(lblTitulo);

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar plan...");
        txtBuscar.setBounds(30, 70, 280, 35);
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cargarDatosDesdeDB();
            }
        });
        add(txtBuscar);

        JButton btnBuscar = new JButton("Buscar");
        estilarBoton(btnBuscar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnBuscar.setBounds(320, 70, 80, 35);
        btnBuscar.addActionListener(e -> cargarDatosDesdeDB());
        add(btnBuscar);

        JButton btnRefrescar = new JButton("↻");
        estilarBoton(btnRefrescar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnRefrescar.setBounds(410, 70, 50, 35);
        btnRefrescar.setToolTipText("Actualizar lista");
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarDatosDesdeDB();
        });
        add(btnRefrescar);

        // Tabla
        String[] cols = { "ID", "Nombre del Plan", "Velocidad", "Precio (S/.)", "Estado" };
        modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(239, 246, 255));

        // Ocultar columna ID (pero mantenerla para referencia)
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setWidth(0);

        // Evento: Al hacer clic en la tabla, llenar el formulario
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cargarFormularioDesdeTabla();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 120, 600, 560);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // ====================================================================
        // COLUMNA DERECHA: FORMULARIO DE EDICIÓN
        // ====================================================================
        JPanel panelForm = new JPanel(null);
        panelForm.setBackground(new Color(248, 250, 252));
        panelForm.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelForm.setBounds(660, 0, 554, 760);
        add(panelForm);

        JLabel lblForm = new JLabel("Detalles del Servicio");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblForm.setForeground(new Color(15, 23, 42));
        lblForm.setBounds(30, 30, 200, 30);
        panelForm.add(lblForm);

        // 1. Nombre del Plan
        panelForm.add(crearLabel("Nombre del Plan:", 30, 80));
        txtNombre = new JTextField();
        txtNombre.setBounds(30, 105, 490, 35);
        panelForm.add(txtNombre);

        // 2. Tipo de Servicio
        panelForm.add(crearLabel("Tipo:", 30, 150));
        cmbTipo = new JComboBox<>(new String[] { "INTERNET", "CABLE TV", "DUO (Net+TV)" });
        cmbTipo.setBounds(30, 175, 230, 35);
        cmbTipo.setBackground(Color.WHITE);
        panelForm.add(cmbTipo);

        // 3. Velocidad
        panelForm.add(crearLabel("Velocidad (Megas):", 290, 150));
        txtVelocidad = new JTextField();
        txtVelocidad.setBounds(290, 175, 230, 35);
        panelForm.add(txtVelocidad);

        // 4. Precio Mensual
        panelForm.add(crearLabel("Mensualidad (S/.):", 30, 220));
        txtPrecio = new JTextField();
        txtPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPrecio.setBounds(30, 245, 230, 35);
        panelForm.add(txtPrecio);

        // 5. Descripción
        panelForm.add(crearLabel("Descripción / Notas:", 30, 290));
        txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)));
        txtDescripcion.setBounds(30, 315, 490, 80);
        panelForm.add(txtDescripcion);

        // BOTONES DE ACCIÓN
        btnLimpiar = new JButton("Limpiar / Nuevo");
        estilarBoton(btnLimpiar, Color.WHITE, new Color(15, 23, 42));
        btnLimpiar.setBorder(new LineBorder(new Color(203, 213, 225)));
        btnLimpiar.setBounds(30, 420, 150, 40);
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        panelForm.add(btnLimpiar);

        btnGuardar = new JButton("GUARDAR SERVICIO");
        estilarBoton(btnGuardar, new Color(37, 99, 235), Color.WHITE);
        btnGuardar.setBounds(200, 420, 200, 40);
        btnGuardar.addActionListener(e -> accionGuardar());
        panelForm.add(btnGuardar);

        btnEliminar = new JButton("ELIMINAR");
        estilarBoton(btnEliminar, new Color(220, 38, 38), Color.WHITE);
        btnEliminar.setBounds(420, 420, 100, 40);
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> accionEliminar());
        panelForm.add(btnEliminar);

        // Mensaje de ayuda
        JLabel lblHelp = new JLabel("<html><body style='width: 450px; color: gray; font-size: 10px;'>" +
                "Nota: Al editar el precio de un plan, los contratos existentes " +
                "mantendrán su precio antiguo hasta que se actualicen manualmente. " +
                "Al eliminar un plan con clientes, se mostrará un diálogo para migrarlos a otro plan." +
                "</body></html>");
        lblHelp.setBounds(30, 480, 490, 50);
        panelForm.add(lblHelp);
    }

    // --- LÓGICA DE CARGA DESDE BD ---

    private void cargarDatosDesdeDB() {
        modelo.setRowCount(0);
        String busqueda = txtBuscar != null ? txtBuscar.getText() : "";

        List<Servicio> servicios = servicioDAO.listar(busqueda);

        for (Servicio s : servicios) {
            modelo.addRow(new Object[] {
                    s.getIdServicio(),
                    s.getDescripcion(),
                    s.getVelocidadMb() + " MB",
                    String.format("S/. %.2f", s.getMensualidad()),
                    s.getActivo() == 1 ? "ACTIVO" : "DESACTIVADO"
            });
        }
    }

    private void cargarFormularioDesdeTabla() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            idServicioSeleccionado = (int) modelo.getValueAt(fila, 0);
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            txtVelocidad.setText(modelo.getValueAt(fila, 2).toString().replace(" MB", ""));
            txtPrecio.setText(modelo.getValueAt(fila, 3).toString().replace("S/. ", ""));

            btnGuardar.setText("ACTUALIZAR PLAN");
            btnGuardar.setBackground(new Color(22, 163, 74)); // Verde al editar
            btnEliminar.setEnabled(true);
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        idServicioSeleccionado = -1;
        txtNombre.setText("");
        txtPrecio.setText("");
        txtVelocidad.setText("");
        txtDescripcion.setText("");
        cmbTipo.setSelectedIndex(0);
        btnGuardar.setText("GUARDAR SERVICIO");
        btnGuardar.setBackground(new Color(37, 99, 235)); // Azul al crear
        btnEliminar.setEnabled(false);
    }

    private void accionGuardar() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String velocidadStr = txtVelocidad.getText().trim();

        if (nombre.isEmpty() || precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y precio son obligatorios.");
            return;
        }

        double precio;
        int velocidad;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
            velocidad = velocidadStr.isEmpty() ? 0 : Integer.parseInt(velocidadStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio y velocidad deben ser números válidos.");
            return;
        }

        Servicio servicio = new Servicio();
        servicio.setDescripcion(nombre);
        servicio.setMensualidad(precio);
        servicio.setVelocidadMb(velocidad);
        servicio.setActivo(1);

        boolean exito;
        if (idServicioSeleccionado == -1) {
            // Crear nuevo
            exito = servicioDAO.insertar(servicio);
        } else {
            // Actualizar existente
            servicio.setIdServicio(idServicioSeleccionado);
            exito = servicioDAO.actualizar(servicio);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this,
                    idServicioSeleccionado == -1 ? "Servicio creado exitosamente."
                            : "Servicio actualizado exitosamente.");
            limpiarFormulario();
            cargarDatosDesdeDB();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el servicio.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionEliminar() {
        if (idServicioSeleccionado == -1)
            return;

        String nombrePlan = txtNombre.getText();
        int cantidadClientes = servicioDAO.contarSuscripcionesPorServicio(idServicioSeleccionado);

        if (cantidadClientes > 0) {
            // Tiene clientes afiliados - mostrar diálogo especial
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogoEliminarPlan dialogo = new DialogoEliminarPlan(parentFrame, idServicioSeleccionado, nombrePlan);
            dialogo.setVisible(true);

            if (dialogo.isAccionRealizada()) {
                limpiarFormulario();
                cargarDatosDesdeDB();
            }
        } else {
            // Sin clientes - eliminación directa
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el plan '" + nombrePlan + "'?\n\nEsta acción no se puede deshacer.",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean eliminado = servicioDAO.eliminar(idServicioSeleccionado);

                if (eliminado) {
                    JOptionPane.showMessageDialog(this, "Plan eliminado correctamente.");
                    limpiarFormulario();
                    cargarDatosDesdeDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar el plan.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // --- UTILIDADES ---
    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBounds(x, y, 200, 20);
        return lbl;
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}