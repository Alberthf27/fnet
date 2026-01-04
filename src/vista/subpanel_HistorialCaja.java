package vista;

import DAO.PagoDAO;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de Registro de Pagos - Muestra pagos del día con filtros por método.
 */
public class subpanel_HistorialCaja extends JPanel {

    private JTable tablaMov;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalEfectivo, lblTotalYape, lblTotalGeneral;
    private JComboBox<String> comboMetodo;
    private PagoDAO pagoDAO;

    public subpanel_HistorialCaja() {
        this.pagoDAO = new PagoDAO();
        initUI();
        cargarMovimientosDelDia();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior con título, filtros y fecha
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBackground(Color.WHITE);

        // Título
        JLabel lblTitulo = new JLabel("📊 Registro de Pagos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(37, 99, 235));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelFiltros.setBackground(Color.WHITE);

        JLabel lblFiltro = new JLabel("Método:");
        lblFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFiltros.add(lblFiltro);

        comboMetodo = new JComboBox<>(new String[] { "Todos", "EFECTIVO", "YAPE" });
        comboMetodo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboMetodo.setPreferredSize(new Dimension(150, 30));
        comboMetodo.addActionListener(e -> cargarMovimientosDelDia());
        panelFiltros.add(comboMetodo);

        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnActualizar.setBackground(new Color(37, 99, 235));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> cargarMovimientosDelDia());
        panelFiltros.add(btnActualizar);

        panelSuperior.add(panelFiltros, BorderLayout.CENTER);

        // Fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        JLabel lblFecha = new JLabel("📅 " + sdf.format(new Date()));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFecha.setForeground(new Color(107, 114, 128));
        panelSuperior.add(lblFecha, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de movimientos
        String[] columnas = { "Hora", "Cliente", "Concepto", "Método", "Monto" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaMov = new JTable(modeloTabla);
        tablaMov.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaMov.setRowHeight(35);
        tablaMov.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaMov.getTableHeader().setBackground(new Color(249, 250, 251));
        tablaMov.getTableHeader().setForeground(new Color(55, 65, 81));
        tablaMov.setSelectionBackground(new Color(219, 234, 254));
        tablaMov.setGridColor(new Color(229, 231, 235));

        // Alinear montos a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaMov.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(tablaMov);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        add(scroll, BorderLayout.CENTER);

        // Panel inferior con totales
        JPanel panelInferior = new JPanel(new GridLayout(1, 3, 20, 0));
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Total Efectivo
        JPanel panelEfectivo = new JPanel(new BorderLayout());
        panelEfectivo.setBackground(new Color(240, 253, 244));
        panelEfectivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(134, 239, 172), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        JLabel lblTituloEfectivo = new JLabel("💵 Efectivo");
        lblTituloEfectivo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloEfectivo.setForeground(new Color(22, 101, 52));
        lblTotalEfectivo = new JLabel("S/. 0.00");
        lblTotalEfectivo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalEfectivo.setForeground(new Color(22, 163, 74));
        panelEfectivo.add(lblTituloEfectivo, BorderLayout.NORTH);
        panelEfectivo.add(lblTotalEfectivo, BorderLayout.CENTER);
        panelInferior.add(panelEfectivo);

        // Total Yape
        JPanel panelYape = new JPanel(new BorderLayout());
        panelYape.setBackground(new Color(240, 249, 255));
        panelYape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 197, 253), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        JLabel lblTituloYape = new JLabel("📱 Yape");
        lblTituloYape.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloYape.setForeground(new Color(30, 64, 175));
        lblTotalYape = new JLabel("S/. 0.00");
        lblTotalYape.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalYape.setForeground(new Color(37, 99, 235));
        panelYape.add(lblTituloYape, BorderLayout.NORTH);
        panelYape.add(lblTotalYape, BorderLayout.CENTER);
        panelInferior.add(panelYape);

        // Total General
        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setBackground(new Color(254, 243, 199));
        panelTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(253, 224, 71), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        JLabel lblTituloTotal = new JLabel("💰 Total");
        lblTituloTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloTotal.setForeground(new Color(133, 77, 14));
        lblTotalGeneral = new JLabel("S/. 0.00");
        lblTotalGeneral.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalGeneral.setForeground(new Color(202, 138, 4));
        panelTotal.add(lblTituloTotal, BorderLayout.NORTH);
        panelTotal.add(lblTotalGeneral, BorderLayout.CENTER);
        panelInferior.add(panelTotal);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarMovimientosDelDia() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        try {
            // Obtener movimientos del día desde la BD
            Object[][] movimientos = pagoDAO.obtenerMovimientosDelDia();

            double totalEfectivo = 0.0;
            double totalYape = 0.0;
            String filtroMetodo = (String) comboMetodo.getSelectedItem();

            if (movimientos != null && movimientos.length > 0) {
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");

                for (Object[] mov : movimientos) {
                    // mov[0] = fecha_pago, mov[1] = cliente, mov[2] = concepto,
                    // mov[3] = metodo, mov[4] = monto

                    String hora = sdfHora.format((Date) mov[0]);
                    String cliente = (String) mov[1];
                    String concepto = (String) mov[2];
                    String metodo = (String) mov[3];
                    double monto = (double) mov[4];

                    // Aplicar filtro
                    if (!filtroMetodo.equals("Todos") && !metodo.equalsIgnoreCase(filtroMetodo)) {
                        continue;
                    }

                    modeloTabla.addRow(new Object[] {
                            hora,
                            cliente,
                            concepto,
                            metodo,
                            String.format("S/. %.2f", monto)
                    });

                    // Acumular totales
                    if ("EFECTIVO".equalsIgnoreCase(metodo)) {
                        totalEfectivo += monto;
                    } else if ("YAPE".equalsIgnoreCase(metodo)) {
                        totalYape += monto;
                    }
                }
            }

            // Si no hay datos después del filtro
            if (modeloTabla.getRowCount() == 0) {
                modeloTabla.addRow(new Object[] {
                        "-", "Sin movimientos registrados", "-", "-", "S/. 0.00"
                });
            }

            // Actualizar totales
            lblTotalEfectivo.setText(String.format("S/. %.2f", totalEfectivo));
            lblTotalYape.setText(String.format("S/. %.2f", totalYape));
            lblTotalGeneral.setText(String.format("S/. %.2f", totalEfectivo + totalYape));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar movimientos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
