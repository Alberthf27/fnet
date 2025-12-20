package vista;

import DAO.FinanzasDAO;
import DAO.PagoDAO; // You'll need this for the table
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import vista.componentes.CardPanel;

public class panel_Gerente extends JPanel {

    // Global variables for updating UI
    private CardPanel card1, card2, card3, card4;
    private JPanel chartPanel, summaryPanel;
    private JTable tablePagos;
    private DefaultTableModel modelPagos;
    private JLabel lblTableTitle;
    private FinanzasDAO finanzasDAO;
    private PagoDAO pagoDAO;
    private int cortadosReal = 0;
    private int nuevosReal = 0;
    private int bajasReal = 0;

    public panel_Gerente() {
        setBackground(new Color(241, 245, 249));
        // Using BorderLayout for the main structure to allow scroll if needed,
        // but keeping null layout for internal flexible positioning if desired
        // OR shifting to GridBagLayout for true responsiveness.
        // Given your previous code used null layout for specific pixel placement,
        // I will use a responsive null layout with component listener logic
        // BUT I strongly recommend MigLayout or GridBagLayout for production.
        // For this optimization, let's stick to your resizing logic but improve the
        // data loading.
        setLayout(null);

        finanzasDAO = new FinanzasDAO();
        pagoDAO = new PagoDAO(); // Assuming you have this

        initContenido();

        // --- OPTIMIZATION: LOAD DATA ASYNCHRONOUSLY ---
        cargarDatosDashboard();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcularPosiciones();
            }
        });
    }

    private void initContenido() {
        // 1. TÍTULO (Fijo)
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));
        title.setBounds(30, 20, 200, 30);
        add(title);

        JLabel subtitle = new JLabel("Resumen general de tu red y facturación");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setBounds(30, 50, 300, 20);
        add(subtitle);

        // 2. CREAR TARJETAS (Placeholders initially)
        card1 = new CardPanel("Ingresos de Hoy", "Cargando...", "...", "", new Color(22, 163, 74));
        card2 = new CardPanel("Deuda Total", "Cargando...", "...", "", new Color(220, 38, 38));
        card3 = new CardPanel("Clientes Activos", "Cargando...", "...", "", new Color(37, 99, 235));
        card4 = new CardPanel("Clientes Cortados", "Cargando...", "...", "", new Color(100, 116, 139));

        add(card1);
        add(card2);
        add(card3);
        add(card4);

        // 3. GRÁFICO CONTAINER
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        JLabel lblChartTitle = new JLabel(" Ingresos Mensuales");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChartTitle.setBorder(new EmptyBorder(10, 15, 5, 10));
        chartPanel.add(lblChartTitle, BorderLayout.NORTH);

        // Placeholder label while chart loads
        JLabel lblChartLoading = new JLabel("Cargando gráfico...", SwingConstants.CENTER);
        lblChartLoading.setForeground(Color.GRAY);
        chartPanel.add(lblChartLoading, BorderLayout.CENTER);

        add(chartPanel);

        // 4. RESUMEN LATERAL
        summaryPanel = new JPanel(null); // Keep null layout for internal labels if you prefer
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        JLabel lblSummaryTitle = new JLabel("Resumen de Clientes");
        lblSummaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSummaryTitle.setBounds(20, 15, 200, 20);
        summaryPanel.add(lblSummaryTitle);

        // Initial static placeholders, will update later
        // Note: You might want to make these labels class members to update them
        add(summaryPanel);

        // 5. TABLA
        lblTableTitle = new JLabel("Últimos Pagos Registrados");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        add(lblTableTitle);

        String[] columnas = { "ID", "Fecha/Hora", "Cliente", "Monto", "Método" };
        modelPagos = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablePagos = new JTable(modelPagos);
        tablePagos.setRowHeight(35);
        tablePagos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablePagos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablePagos.getTableHeader().setBackground(new Color(248, 250, 252));
        tablePagos.setShowVerticalLines(false);
        tablePagos.setGridColor(new Color(240, 240, 240));

        // Hide ID column
        tablePagos.getColumnModel().getColumn(0).setMinWidth(0);
        tablePagos.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(tablePagos);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // IMPORTANT: We need to store scrollPane as a class member to resize it
        // but your original code declared it locally in initContenido but globally too?
        // Let's use the global one.
        this.scrollPane = scrollPane;
        add(scrollPane);
    }

    // Global ScrollPane reference for resizing
    private JScrollPane scrollPane;

    // --- DATA LOADING LOGIC (OPTIMIZADO: UNA SOLA CONEXIÓN) ---
    private void cargarDatosDashboard() {
        // Show loading state
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }

        // Un solo hilo, una sola conexión
        new Thread(() -> {
            try {
                // LLAMADA OPTIMIZADA: Todo en una conexión
                Object[] data = finanzasDAO.obtenerDashboardData();

                // Extraer datos
                double ingHoy = (double) data[0];
                double deuTot = (double) data[1];
                int cliAct = (int) data[2];
                int cliCor = (int) data[3];
                int nuevos = (int) data[4];
                int bajas = (int) data[5];
                @SuppressWarnings("unchecked")
                List<Object[]> histData = (List<Object[]>) data[6];
                @SuppressWarnings("unchecked")
                List<Object[]> pagosList = (List<Object[]>) data[7];

                // --- UPDATE UI ON EDT ---
                SwingUtilities.invokeLater(() -> {
                    this.nuevosReal = nuevos;
                    this.bajasReal = bajas;
                    this.cortadosReal = cliCor;

                    NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
                    card1.setData(currency.format(ingHoy), "Hoy");
                    card2.setData(currency.format(deuTot), "Total vencido");
                    card3.setData(String.valueOf(cliAct), "En línea");
                    card4.setData(String.valueOf(cliCor), "Suspendidos");

                    actualizarResumenVisual(summaryPanel.getWidth());
                    actualizarGrafico(histData);

                    // Update Table
                    modelPagos.setRowCount(0);
                    for (Object[] row : pagosList) {
                        modelPagos.addRow(row);
                    }

                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                });
            }
        }).start();
    }

    private void actualizarGrafico(List<Object[]> historial) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] dato : historial) {
            // Assuming dato[0] is Label (String), dato[1] is Value (Number)
            dataset.addValue((Number) dato[1], "Ingresos", dato[0].toString());
        }

        JFreeChart chart = ChartFactory.createAreaChart(
                "", "", "", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        // Visual Styling
        chart.setBackgroundPaint(Color.WHITE);
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(230, 230, 230));

        org.jfree.chart.renderer.category.AreaRenderer renderer = (org.jfree.chart.renderer.category.AreaRenderer) plot
                .getRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235, 180));
        renderer.setSeriesOutlinePaint(0, new Color(37, 99, 235));

        // Add to panel
        chartPanel.removeAll();
        // Re-add title
        JLabel lblChartTitle = new JLabel(" Ingresos Mensuales");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChartTitle.setBorder(new EmptyBorder(10, 15, 5, 10));
        chartPanel.add(lblChartTitle, BorderLayout.NORTH);

        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        cp.setBorder(new EmptyBorder(0, 10, 10, 10));
        chartPanel.add(cp, BorderLayout.CENTER);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    // --- RESIZING LOGIC (Your existing math) ---
    private void recalcularPosiciones() {
        int anchoTotal = getWidth();
        if (anchoTotal == 0) {
            return;
        }

        int margenX = 30;
        int gap = 20;
        int anchoUtil = anchoTotal - (margenX * 2);

        // 1. CARDS
        int cardW = (anchoUtil - (gap * 3)) / 4;
        int cardH = 100; // Slightly smaller height
        int cardY = 90;

        card1.setBounds(margenX, cardY, cardW, cardH);
        card2.setBounds(margenX + cardW + gap, cardY, cardW, cardH);
        card3.setBounds(margenX + (cardW + gap) * 2, cardY, cardW, cardH);
        card4.setBounds(margenX + (cardW + gap) * 3, cardY, cardW, cardH);

        // 2. CHART & SUMMARY
        int chartY = 220;
        int chartH = 300;
        int chartW = (int) (anchoUtil * 0.70) - gap;
        int summaryW = anchoUtil - chartW - gap;

        chartPanel.setBounds(margenX, chartY, chartW, chartH);
        summaryPanel.setBounds(margenX + chartW + gap, chartY, summaryW, chartH);

        // Re-draw summary data since absolute positioning might need refresh if width
        // changes drastically
        actualizarResumenVisual(summaryW); // Optional helper

        // 3. TABLE
        int tableY = 550;
        lblTableTitle.setBounds(margenX, tableY, 300, 20);

        // Dynamic height for table based on window height
        int windowHeight = getHeight();
        int tableH = Math.max(200, windowHeight - tableY - 50); // Use remaining space

        if (scrollPane != null) {
            scrollPane.setBounds(margenX, tableY + 30, anchoUtil, tableH);
        }

        revalidate();
        repaint();
    }

    private void actualizarResumenVisual(int width) {
        summaryPanel.removeAll();

        JLabel lbl = new JLabel("Resumen de Clientes");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setBounds(20, 15, 200, 20);
        summaryPanel.add(lbl);

        // Ideally these values come from the async load too.
        // For now using static or class member variables.
        agregarDatoResumen(summaryPanel, "Nuevos este mes", String.valueOf(this.nuevosReal), 60, width);

        agregarDatoResumen(summaryPanel, "Bajas voluntarias", String.valueOf(this.bajasReal), 110, width);

        agregarDatoResumen(summaryPanel, "Cortes por deuda", String.valueOf(this.cortadosReal), 160, width);

    }

    private void agregarDatoResumen(JPanel panel, String label, String valor, int y, int panelWidth) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.GRAY);
        lbl.setBounds(20, y, 150, 20);
        panel.add(lbl);

        JLabel val = new JLabel(valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        val.setBounds(panelWidth - 100, y, 80, 20);
        panel.add(val);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(240, 240, 240));
        sep.setBounds(20, y + 35, panelWidth - 40, 10);
        panel.add(sep);
    }
}
