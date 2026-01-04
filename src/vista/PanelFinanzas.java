package vista;

import DAO.FinanzasDAO;
import java.awt.*;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel de Finanzas rediseñado para ISP.
 * Muestra métricas clave del negocio de internet.
 */
public class PanelFinanzas extends JPanel {

    private FinanzasDAO finanzasDAO;
    private JLabel lblClientesActivos, lblTasaCobro, lblIngresoMes, lblFacturasPendientes;
    private JPanel pnlGraficoIngresos, pnlGraficoServicios, pnlGraficoEstadoPagos;
    private NumberFormat formatoMoneda;

    public PanelFinanzas() {
        this.finanzasDAO = new FinanzasDAO();
        this.formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        initUI();
        cargarDatos();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(249, 250, 251));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior con título
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(new Color(249, 250, 251));

        JLabel lblTitulo = new JLabel("Dashboard Financiero ISP");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(37, 99, 235));
        panelTitulo.add(lblTitulo, BorderLayout.WEST);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnActualizar.setBackground(new Color(37, 99, 235));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> cargarDatos());
        panelTitulo.add(btnActualizar, BorderLayout.EAST);

        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con todo el contenido
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(new Color(249, 250, 251));

        // KPIs superiores (4 tarjetas)
        JPanel panelKPIs = new JPanel(new GridLayout(1, 4, 15, 0));
        panelKPIs.setBackground(new Color(249, 250, 251));
        panelKPIs.setPreferredSize(new Dimension(0, 120));

        lblClientesActivos = new JLabel("0");
        lblTasaCobro = new JLabel("0%");
        lblIngresoMes = new JLabel("S/. 0.00");
        lblFacturasPendientes = new JLabel("0");

        panelKPIs.add(crearTarjetaKPI("Clientes Activos", lblClientesActivos, new Color(59, 130, 246)));
        panelKPIs.add(crearTarjetaKPI("Tasa de Cobro", lblTasaCobro, new Color(16, 185, 129)));
        panelKPIs.add(crearTarjetaKPI("Ingresos del Mes", lblIngresoMes, new Color(139, 92, 246)));
        panelKPIs.add(crearTarjetaKPI("Facturas Vencidas", lblFacturasPendientes, new Color(239, 68, 68)));

        panelCentral.add(panelKPIs, BorderLayout.NORTH);

        // Gráficos (3 paneles) - LAZY LOADING
        JPanel panelGraficos = new JPanel(new GridLayout(2, 2, 15, 15));
        panelGraficos.setBackground(new Color(249, 250, 251));

        pnlGraficoIngresos = crearContenedorGrafico("Ingresos Ultimos 6 Meses");
        pnlGraficoServicios = crearContenedorGrafico("Distribucion de Servicios");
        pnlGraficoEstadoPagos = crearContenedorGrafico("Estado de Pagos");

        panelGraficos.add(pnlGraficoIngresos);
        panelGraficos.add(pnlGraficoServicios);
        panelGraficos.add(pnlGraficoEstadoPagos);
        panelGraficos.add(crearPanelAlertas());

        panelCentral.add(panelGraficos, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color colorBorde) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, colorBorde),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitulo.setForeground(new Color(107, 114, 128));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValor.setForeground(new Color(17, 24, 39));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(lblValor);

        return tarjeta;
    }

    private JPanel crearContenedorGrafico(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(55, 65, 81));
        panel.add(lblTitulo, BorderLayout.NORTH);

        return panel;
    }

    private JPanel crearPanelAlertas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblTitulo = new JLabel("Alertas Importantes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(55, 65, 81));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(15));

        // Alertas dinámicas (se llenarán con datos reales)
        JLabel lblAlerta1 = new JLabel("Cargando alertas...");
        lblAlerta1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAlerta1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblAlerta1);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private void cargarDatos() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int clientesActivos;
            private double tasaCobro;
            private double ingresoMes;
            private int facturasPendientes;
            private java.util.List<Object[]> datosIngresos;
            private java.util.List<Object[]> datosServicios;
            private java.util.List<Object[]> datosEstadoPagos;

            @Override
            protected Void doInBackground() throws Exception {
                // Cargar todos los datos en paralelo
                clientesActivos = finanzasDAO.obtenerClientesActivos();
                tasaCobro = finanzasDAO.obtenerTasaCobro();
                ingresoMes = finanzasDAO.obtenerIngresosMesActual();
                facturasPendientes = finanzasDAO.obtenerFacturasVencidas();
                datosIngresos = finanzasDAO.obtenerIngresosUltimos6Meses();
                datosServicios = finanzasDAO.obtenerDistribucionServicios();
                datosEstadoPagos = finanzasDAO.obtenerEstadoPagos();
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Actualizar KPIs
                    lblClientesActivos.setText(String.valueOf(clientesActivos));
                    lblTasaCobro.setText(String.format("%.1f%%", tasaCobro));
                    lblIngresoMes.setText(formatoMoneda.format(ingresoMes));
                    lblFacturasPendientes.setText(String.valueOf(facturasPendientes));

                    // Generar gráficos
                    generarGraficoIngresos(datosIngresos);
                    generarGraficoServicios(datosServicios);
                    generarGraficoEstadoPagos(datosEstadoPagos);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PanelFinanzas.this,
                            "Error al cargar datos: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void generarGraficoIngresos(java.util.List<Object[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Object[] fila : datos) {
            String mes = (String) fila[0];
            double monto = (double) fila[1];
            dataset.addValue(monto, "Ingresos", mes);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null,
                "Mes",
                "Ingresos (S/.)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(229, 231, 235));
        plot.setOutlineVisible(false);

        agregarAlPanel(pnlGraficoIngresos, chart);
    }

    private void generarGraficoServicios(java.util.List<Object[]> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Object[] fila : datos) {
            String servicio = (String) fila[0];
            int cantidad = (int) fila[1];
            dataset.setValue(servicio, cantidad);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("10 Mbps", new Color(59, 130, 246));
        plot.setSectionPaint("20 Mbps", new Color(16, 185, 129));
        plot.setSectionPaint("50 Mbps", new Color(139, 92, 246));
        plot.setSectionPaint("100 Mbps", new Color(245, 158, 11));

        agregarAlPanel(pnlGraficoServicios, chart);
    }

    private void generarGraficoEstadoPagos(java.util.List<Object[]> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Object[] fila : datos) {
            String estado = (String) fila[0];
            int cantidad = (int) fila[1];
            dataset.setValue(estado, cantidad);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false);

        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Al día", new Color(16, 185, 129));
        plot.setSectionPaint("Vencidas", new Color(239, 68, 68));

        agregarAlPanel(pnlGraficoEstadoPagos, chart);
    }

    private void agregarAlPanel(JPanel contenedor, JFreeChart chart) {
        contenedor.removeAll();

        JLabel lblTitulo = new JLabel(contenedor.getName());
        if (lblTitulo.getText() == null || lblTitulo.getText().isEmpty()) {
            // Recuperar título del borde si existe
            if (contenedor.getComponentCount() > 0 && contenedor.getComponent(0) instanceof JLabel) {
                lblTitulo = (JLabel) contenedor.getComponent(0);
                contenedor.remove(0);
            }
        }

        contenedor.setLayout(new BorderLayout());
        if (lblTitulo != null && lblTitulo.getText() != null) {
            contenedor.add(lblTitulo, BorderLayout.NORTH);
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        chartPanel.setBackground(Color.WHITE);
        contenedor.add(chartPanel, BorderLayout.CENTER);

        contenedor.revalidate();
        contenedor.repaint();
    }
}