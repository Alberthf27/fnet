package vista;

import DAO.FinanzasDAO;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Imports JFreeChart
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
    private JPanel pnlGraficoTendencia, pnlGraficoPastel, pnlGraficoChurn, pnlGraficoCaja;

    // Pool de hilos para paralelismo controlado (3 hilos simultáneos para no
    // saturar Railway)
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public PanelFinanzas() {
        setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        finanzasDAO = new FinanzasDAO();
        initUI();
        cargarDatosParalelo(); // <--- NUEVO MÉTODO DE CARGA
    }

    private void initUI() {
        // --- HEADER ---
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

        // --- CENTER ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(getBackground());

        // 1. CARDS
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(getBackground());
        cardsPanel.setPreferredSize(new Dimension(0, 120));

        lblMRR = new JLabel("...");
        cardsPanel
                .add(crearTarjeta("MRR (Ingreso Recurrente)", lblMRR, "Salud financiera base", new Color(37, 99, 235)));

        lblCaja = new JLabel("...");
        cardsPanel.add(crearTarjeta("Flujo de Caja (Mes)", lblCaja, "Liquidez inmediata", new Color(22, 163, 74)));

        lblDeuda = new JLabel("...");
        cardsPanel.add(crearTarjeta("Cartera Vencida", lblDeuda, "Riesgo de impago", new Color(220, 38, 38)));

        centerPanel.add(cardsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. GRÁFICOS
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsGrid.setBackground(getBackground());

        pnlGraficoTendencia = crearContenedorGrafico("Tendencia de Crecimiento (MRR)");
        pnlGraficoPastel = crearContenedorGrafico("Distribución de Planes");
        pnlGraficoChurn = crearContenedorGrafico("Altas vs Bajas");
        pnlGraficoCaja = crearContenedorGrafico("Flujo de Caja (7 días)");

        chartsGrid.add(pnlGraficoTendencia);
        chartsGrid.add(pnlGraficoPastel);
        chartsGrid.add(pnlGraficoChurn);
        chartsGrid.add(pnlGraficoCaja);

        centerPanel.add(chartsGrid);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * OPTIMIZADO: Usa UNA SOLA conexión para todos los datos.
     */
    private void cargarDatosParalelo() {
        if (Principal.instancia != null)
            Principal.instancia.mostrarCarga(true);

        new Thread(() -> {
            try {
                // LLAMADA OPTIMIZADA
                Object[] data = finanzasDAO.obtenerFinanzasData();

                // Extraer datos
                double mrr = (double) data[0];
                double balance = (double) data[1];
                double deuda = (double) data[2];
                @SuppressWarnings("unchecked")
                List<Object[]> historialMRR = (List<Object[]>) data[3];
                @SuppressWarnings("unchecked")
                List<Object[]> distribucion = (List<Object[]>) data[4];
                @SuppressWarnings("unchecked")
                List<double[]> altasBajas = (List<double[]>) data[5];
                @SuppressWarnings("unchecked")
                List<double[]> flujo = (List<double[]>) data[6];

                // Actualizar UI en EDT
                SwingUtilities.invokeLater(() -> {
                    lblMRR.setText("S/. " + String.format("%.2f", mrr));
                    lblCaja.setText("S/. " + String.format("%.2f", balance));
                    lblDeuda.setText("S/. " + String.format("%.2f", deuda));

                    generarGraficoLinea(pnlGraficoTendencia, historialMRR);
                    generarGraficoDonut(pnlGraficoPastel, distribucion);
                    generarGraficoBarrasApiladas(pnlGraficoChurn, altasBajas);
                    generarGraficoBarrasSimples(pnlGraficoCaja, flujo);

                    if (Principal.instancia != null)
                        Principal.instancia.mostrarCarga(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null)
                        Principal.instancia.mostrarCarga(false);
                });
            }
        }).start();
    }

    // --- MÉTODOS DE UI (Igual que antes pero encapsulados) ---

    private JPanel crearTarjeta(String titulo, JLabel lblValor, String subtitulo, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(15, 15, 15, 15)));

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
                new EmptyBorder(10, 10, 10, 10)));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lbl, BorderLayout.NORTH);

        // Placeholder de carga
        JLabel loading = new JLabel("Cargando datos...", SwingConstants.CENTER);
        loading.setForeground(Color.LIGHT_GRAY);
        p.add(loading, BorderLayout.CENTER);

        return p;
    }

    private void estilarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void abrirDialogoMovimiento(String tipo) {
        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        java.awt.Frame frame = (parent instanceof java.awt.Frame) ? (java.awt.Frame) parent : null;
        DialogoMovimiento dialog = new DialogoMovimiento(frame, tipo);
        dialog.setVisible(true);
        if (dialog.isGuardado()) {
            cargarDatosParalelo(); // Recargar si hubo cambios
        }
    }

    // --- GENERACIÓN DE JFREECHART ---

    private void generarGraficoLinea(JPanel contenedor, List<Object[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] d : datos)
            dataset.addValue((Number) d[1], "Ingresos", d[0].toString());

        JFreeChart chart = ChartFactory.createLineChart("", "", "S/.", dataset, PlotOrientation.VERTICAL, false, true,
                false);
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
        for (Object[] d : datos)
            dataset.setValue(d[0].toString(), (Number) d[1]);

        JFreeChart chart = ChartFactory.createRingChart("", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        agregarAlPanel(contenedor, chart);
    }

    private void generarGraficoBarrasApiladas(JPanel contenedor, List<double[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] meses = { "Sep", "Oct", "Nov", "Dic" };
        for (int i = 0; i < datos.size(); i++) {
            dataset.addValue(datos.get(i)[0], "Nuevos", (i < meses.length ? meses[i] : "" + i));
            dataset.addValue(datos.get(i)[1], "Bajas", (i < meses.length ? meses[i] : "" + i));
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
        // Generar etiquetas de días dinámicas (opcional) o fijas
        String[] dias = { "L", "M", "M", "J", "V", "S", "D" };
        for (int i = 0; i < datos.size(); i++) {
            String diaLabel = (i < dias.length) ? dias[i] : String.valueOf(i);
            dataset.addValue(datos.get(i)[0], "Ingreso", diaLabel);
            dataset.addValue(datos.get(i)[1], "Gasto", diaLabel);
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
        contenedor.removeAll();
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        contenedor.add(cp, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }
}