package vista;

import DAO.ClienteDAO;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;

public class subpanel_DirectorioClientes extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private ClienteDAO clienteDAO;

    public subpanel_DirectorioClientes() {
        initComponents();
        clienteDAO = new ClienteDAO();
        inicializarTabla();
        cargarClientes();
    }

    private void inicializarTabla() {
        String[] cols = {"ID", "DNI", "Nombres", "Apellidos", "Dirección", "Deuda (S/.)", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaClientes.setModel(tableModel);
        // Ocultar ID
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setWidth(0);
    }

    private void cargarClientes() {
        tableModel.setRowCount(0);
        List<Cliente> lista = clienteDAO.obtenerTodosClientes();
        for (Cliente c : lista) {
            Object[] row = {
                c.getIdCliente(),
                c.getDniCliente(),
                c.getNombres(),
                c.getApellidos(),
                c.getDireccion(),
                String.format("%.2f", c.getDeuda()),
                (c.getActivo() == 1 ? "Activo" : "Inactivo")
            };
            tableModel.addRow(row);
        }
    }

    // --- CÓDIGO GENERADO (Pega esto en la sección correspondiente si usas Matisse o déjalo así si es manual) ---
    private void initComponents() {
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("Directorio de Clientes");
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton("Buscar");
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        btnEditar = new javax.swing.JButton("Editar Seleccionado");
        btnEliminar = new javax.swing.JButton("Eliminar Seleccionado");

        setBackground(new java.awt.Color(255, 255, 255)); // Fondo blanco limpio

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); 
        lblTitulo.setForeground(new java.awt.Color(15, 23, 42));

        tablaClientes.setRowHeight(30);
        tablaClientes.setFont(new java.awt.Font("Segoe UI", 0, 14));
        jScrollPane1.setViewportView(tablaClientes);

        // Layout simple para empezar
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBuscar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEditar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEliminar)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblTitulo)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }

    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tablaClientes;
    private javax.swing.JTextField txtBuscar;
}