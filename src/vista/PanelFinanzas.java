package vista;

import DAO.FinanzasDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// JFreeChart Imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class PanelFinanzas extends JPanel {

    private FinanzasDAO finanzasDAO;
    private JLabel lblMRR, lblCaja, lblDeuda;

    // Contenedores para los 4 gráficos
    private JPanel pnlGraficoTendencia, pnlGraficoPastel, pnlGraficoChurn, pnlGraficoCaja;

    public PanelFinanzas() {
        setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(20, 20)); // Use BorderLayout for main structure
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Margin around the whole panel

        finanzasDAO = new FinanzasDAO();
        initUI();
        cargarDatos();
    }

    private void initUI() {
        // --- HEADER PANEL (Title + Buttons) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getBackground());
        headerPanel.setPreferredSize(new Dimension(0, 50));

        JLabel lblTitulo = new JLabel("Dashboard Financiero & Estratégico");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        headerPanel.add(lblTitulo, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(getBackground());

        JButton btnIngreso = new JButton("+ INGRESO");
        estilarBoton(btnIngreso, new Color(22, 163, 74));
        btnIngreso.addActionListener(e -> abrirDialogoMovimiento("INGRESO"));
        btnPanel.add(btnIngreso);

        JButton btnGasto = new JButton("- GASTO");
        estilarBoton(btnGasto, new Color(220, 38, 38));
        btnGasto.addActionListener(e -> abrirDialogoMovimiento("EGRESO"));
        btnPanel.add(btnGasto);

        headerPanel.add(btnPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- CENTER PANEL (KPIs + Charts) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(getBackground());

        // 1. KPI CARDS SECTION
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0)); // 1 row, 3 cols, gap 20
        cardsPanel.setBackground(getBackground());
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // Fixed height for cards
        cardsPanel.setPreferredSize(new Dimension(0, 120));

        lblMRR = new JLabel("Cargando...");
        cardsPanel.add(crearTarjeta("MRR (Ingreso Recurrente)", lblMRR, "Salud financiera base", new Color(37, 99, 235)));

        lblCaja = new JLabel("Cargando...");
        cardsPanel.add(crearTarjeta("Flujo de Caja (Mes)", lblCaja, "Liquidez inmediata", new Color(22, 163, 74)));

        lblDeuda = new JLabel("Cargando...");
        cardsPanel.add(crearTarjeta("Cartera Vencida", lblDeuda, "Riesgo de impago", new Color(220, 38, 38)));

        centerPanel.add(cardsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacer

        // 2. CHARTS GRID SECTION
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsGrid.setBackground(getBackground());

        pnlGraficoTendencia = crearContenedorGrafico("Tendencia de Crecimiento (MRR)");
        pnlGraficoPastel = crearContenedorGrafico("Distribución de Planes (Mix)");
        pnlGraficoChurn = crearContenedorGrafico("Altas vs Bajas (Crecimiento Neto)");
        pnlGraficoCaja = crearContenedorGrafico("Flujo de Caja (7 días)");

        chartsGrid.add(pnlGraficoTendencia);
        chartsGrid.add(pnlGraficoPastel);
        chartsGrid.add(pnlGraficoChurn);
        chartsGrid.add(pnlGraficoCaja);

        centerPanel.add(chartsGrid);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo, JLabel lblValor, String subtitulo, Color color) {
        JPanel card = new JPanel(new BorderLayout()); // Changed to BorderLayout for resizing
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(Color.GRAY);
        card.add(lblT, BorderLayout.NORTH);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValor.setForeground(Color.BLACK);
        card.add(lblValor, BorderLayout.CENTER);

        JLabel lblS = new JLabel(subtitulo);
        lblS.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblS.setForeground(Color.LIGHT_GRAY);
        card.add(lblS, BorderLayout.SOUTH);

        return card;
    }

    private JPanel crearContenedorGrafico(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lbl, BorderLayout.NORTH);

        return p;
    }

    // --- GENERADORES DE GRÁFICOS (Keep mostly same logic, simplified styles) ---

    private void generarGraficoLinea(JPanel contenedor, List<Object[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] d : datos) dataset.addValue((Number)d[1], "Ingresos", d[0].toString());

        JFreeChart chart = ChartFactory.createLineChart("", "", "S/.", dataset, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        plot.setRenderer(renderer);
        estilarPlot(plot);
        agregarAlPanel(contenedor, chart);
    }

    private void generarGraficoDonut(JPanel contenedor, List<Object[]> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Object[] d : datos) dataset.setValue(d[0].toString(), (Number)d[1]);

        JFreeChart chart = ChartFactory.createRingChart("", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        // Simple color cycle or specific logic if names are known
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(new Color(255,255,255, 200));
        agregarAlPanel(contenedor, chart);
    }

    private void generarGraficoBarrasApiladas(JPanel contenedor, List<double[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] meses = {"Sep", "Oct", "Nov", "Dic"}; // Example labels, should ideally come from DB
        for (int i=0; i<datos.size(); i++) {
            dataset.addValue(datos.get(i)[0], "Nuevos", (i<meses.length ? meses[i] : ""+i));
            dataset.addValue(datos.get(i)[1], "Bajas", (i<meses.length ? meses[i] : ""+i));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "", "Clientes", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(34, 197, 94));
        renderer.setSeriesPaint(1, new Color(239, 68, 68));
        estilarPlot(plot);
        agregarAlPanel(contenedor, chart);
    }

    private void generarGraficoBarrasSimples(JPanel contenedor, List<double[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] dias = {"L", "M", "M", "J", "V", "S", "D"};
        for (int i=0; i<datos.size(); i++) {
            dataset.addValue(datos.get(i)[0], "Ingreso", (i<dias.length?dias[i]:""+i));
            dataset.addValue(datos.get(i)[1], "Gasto", (i<dias.length?dias[i]:""+i));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "", "", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, new Color(34, 197, 94));
        renderer.setSeriesPaint(1, new Color(239, 68, 68));
        estilarPlot(plot);
        agregarAlPanel(contenedor, chart);
    }

    private void estilarPlot(CategoryPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
    }

    private void agregarAlPanel(JPanel contenedor, JFreeChart chart) {
        contenedor.removeAll(); // Clear previous chart if reloading
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        contenedor.add(cp, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }

    private void abrirDialogoMovimiento(String tipo) {
        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        java.awt.Frame frame = (parent instanceof java.awt.Frame) ? (java.awt.Frame) parent : null;
        DialogoMovimiento dialog = new DialogoMovimiento(frame, tipo);
        dialog.setVisible(true);
        if (dialog.isGuardado()) {
            cargarDatos();
        }
    }

    private void estilarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void cargarDatos() {
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        new Thread(() -> {
            try {
                // Async data loading logic (Keep your original CompletableFuture logic here)
                // ... (Use the same logic from your provided snippet)
                // Ensure finanzasDAO methods exist (see below)

                 // Mocking data for visual test if DAO is empty
                 // In production, uncomment your DAO calls
                 double mrr = finanzasDAO.calcularMRR();
                 double caja = finanzasDAO.obtenerBalanceMes();
                 double deuda = finanzasDAO.calcularDeudaTotal();
                 List<Object[]> histMRR = finanzasDAO.obtenerHistorialMRR();
                 List<Object[]> planes = finanzasDAO.obtenerDistribucionPlanes();
                 List<double[]> churn = finanzasDAO.obtenerAltasBajasUltimosMeses();
                 List<double[]> flujo = finanzasDAO.obtenerFlujoUltimos7Dias();

                SwingUtilities.invokeLater(() -> {
                    lblMRR.setText("S/. " + String.format("%.2f", mrr));
                    lblCaja.setText("S/. " + String.format("%.2f", caja));
                    lblDeuda.setText("S/. " + String.format("%.2f", deuda));
                    
                    try {
                        generarGraficoLinea(pnlGraficoTendencia, histMRR);
                        generarGraficoDonut(pnlGraficoPastel, planes);
                        generarGraficoBarrasApiladas(pnlGraficoChurn, churn);
                        generarGraficoBarrasSimples(pnlGraficoCaja, flujo);
                    } catch (Exception e) { e.printStackTrace(); }
                    
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                });
            }
        }).start();
    }
}