package vista;

import DAO.ServicioDAO;
import modelo.Servicio;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Diálogo para eliminar o desactivar un plan/servicio.
 * Muestra los clientes afiliados y permite migrarlos a otro plan.
 */
public class DialogoEliminarPlan extends JDialog {

    private final ServicioDAO servicioDAO;
    private final int idServicio;
    private final String nombreServicio;

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JComboBox<Servicio> cmbPlanDestino;
    private JLabel lblResumen;

    private boolean accionRealizada = false;

    public DialogoEliminarPlan(Frame parent, int idServicio, String nombreServicio) {
        super(parent, "Eliminar/Desactivar Plan", true);
        this.servicioDAO = new ServicioDAO();
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;

        initComponents();
        cargarClientesAfiliados();
        cargarPlanesDestino();

        setSize(700, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(248, 250, 252));

        // === HEADER ===
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(248, 250, 252));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Eliminar Plan: " + nombreServicio);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(220, 38, 38)); // Rojo
        panelHeader.add(lblTitulo, BorderLayout.WEST);

        lblResumen = new JLabel();
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelHeader.add(lblResumen, BorderLayout.EAST);

        add(panelHeader, BorderLayout.NORTH);

        // === TABLA DE CLIENTES ===
        JPanel panelCentral = new JPanel(new BorderLayout(0, 10));
        panelCentral.setBackground(new Color(248, 250, 252));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblClientes = new JLabel("Clientes afiliados a este plan:");
        lblClientes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelCentral.add(lblClientes, BorderLayout.NORTH);

        String[] columnas = { "Contrato", "Cliente", "Dirección", "Estado" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setRowHeight(30);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaClientes.setSelectionBackground(new Color(239, 246, 255));

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panelCentral.add(scroll, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // === PANEL INFERIOR ===
        JPanel panelFooter = new JPanel(new BorderLayout(10, 10));
        panelFooter.setBackground(new Color(248, 250, 252));
        panelFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // ComboBox para migrar
        JPanel panelMigracion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelMigracion.setBackground(new Color(248, 250, 252));

        JLabel lblMigrar = new JLabel("Migrar clientes a:");
        lblMigrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelMigracion.add(lblMigrar);

        cmbPlanDestino = new JComboBox<>();
        cmbPlanDestino.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPlanDestino.setPreferredSize(new Dimension(250, 30));
        panelMigracion.add(cmbPlanDestino);

        panelFooter.add(panelMigracion, BorderLayout.NORTH);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setBackground(new Color(248, 250, 252));

        JButton btnCancelar = new JButton("Cancelar");
        estilarBoton(btnCancelar, Color.WHITE, new Color(100, 116, 139));
        btnCancelar.addActionListener(e -> dispose());
        panelBotones.add(btnCancelar);

        JButton btnDesactivar = new JButton("Solo Desactivar");
        estilarBoton(btnDesactivar, new Color(245, 158, 11), Color.WHITE); // Amarillo
        btnDesactivar.setToolTipText("El plan no aparecerá en nuevos contratos, pero los existentes se mantienen");
        btnDesactivar.addActionListener(this::accionDesactivar);
        panelBotones.add(btnDesactivar);

        JButton btnMigrarEliminar = new JButton("Migrar y Eliminar");
        estilarBoton(btnMigrarEliminar, new Color(220, 38, 38), Color.WHITE); // Rojo
        btnMigrarEliminar.setToolTipText("Mover todos los clientes al plan seleccionado y eliminar este plan");
        btnMigrarEliminar.addActionListener(this::accionMigrarYEliminar);
        panelBotones.add(btnMigrarEliminar);

        panelFooter.add(panelBotones, BorderLayout.SOUTH);

        add(panelFooter, BorderLayout.SOUTH);
    }

    private void cargarClientesAfiliados() {
        modeloTabla.setRowCount(0);

        List<Object[]> clientes = servicioDAO.obtenerSuscripcionesPorServicio(idServicio);

        for (Object[] cliente : clientes) {
            modeloTabla.addRow(new Object[] {
                    cliente[1], // codigo_contrato
                    cliente[2], // nombre_cliente
                    cliente[3], // direccion
                    cliente[4] // estado
            });
        }

        int total = clientes.size();
        if (total == 0) {
            lblResumen.setText("Sin clientes afiliados - Se puede eliminar directamente");
            lblResumen.setForeground(new Color(34, 197, 94)); // Verde
        } else {
            lblResumen.setText(total + " cliente(s) afiliado(s)");
            lblResumen.setForeground(new Color(220, 38, 38)); // Rojo
        }
    }

    private void cargarPlanesDestino() {
        cmbPlanDestino.removeAllItems();

        List<Servicio> servicios = servicioDAO.obtenerServiciosActivos();

        for (Servicio s : servicios) {
            // No incluir el plan actual en las opciones
            if (s.getIdServicio() != idServicio) {
                cmbPlanDestino.addItem(s);
            }
        }

        if (cmbPlanDestino.getItemCount() == 0) {
            cmbPlanDestino.setEnabled(false);
        }
    }

    private void accionDesactivar(ActionEvent e) {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Desactivar el plan '" + nombreServicio + "'?\n\n" +
                        "• El plan no aparecerá en nuevos contratos\n" +
                        "• Los " + modeloTabla.getRowCount() + " cliente(s) existentes mantendrán este plan\n" +
                        "• Se puede reactivar más adelante",
                "Confirmar Desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean exito = servicioDAO.desactivarServicio(idServicio);

            if (exito) {
                JOptionPane.showMessageDialog(this,
                        "Plan desactivado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                accionRealizada = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al desactivar el plan.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionMigrarYEliminar(ActionEvent e) {
        Servicio planDestino = (Servicio) cmbPlanDestino.getSelectedItem();

        if (planDestino == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un plan destino para migrar los clientes.",
                    "Plan Destino Requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalClientes = modeloTabla.getRowCount();

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Migrar " + totalClientes + " cliente(s) al plan '" + planDestino.getDescripcion() + "'?\n\n" +
                        "Después se eliminará el plan '" + nombreServicio + "'.\n\n" +
                        "ESTA ACCIÓN NO SE PUEDE DESHACER.",
                "Confirmar Migración y Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // 1. Migrar suscripciones
            int migradas = servicioDAO.migrarSuscripciones(idServicio, planDestino.getIdServicio());

            // 2. Eliminar el plan (ahora ya no tiene suscripciones)
            boolean eliminado = servicioDAO.eliminarSeguro(idServicio);

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Proceso completado:\n" +
                                "• " + migradas + " cliente(s) migrado(s) a '" + planDestino.getDescripcion() + "'\n" +
                                "• Plan '" + nombreServicio + "' eliminado",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                accionRealizada = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Los clientes fueron migrados pero hubo un error al eliminar el plan.",
                        "Error Parcial",
                        JOptionPane.WARNING_MESSAGE);
                accionRealizada = true;
                dispose();
            }
        }
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public boolean isAccionRealizada() {
        return accionRealizada;
    }
}
