package vista;

import DAO.FinanzasDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.concurrent.CompletableFuture; // <--- VITAL PARA LA VELOCIDAD
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
        setLayout(null);
        finanzasDAO = new FinanzasDAO();
        initUI();
        cargarDatos();
    }

    private void initUI() {
        // Título Principal
        JLabel lblTitulo = new JLabel("Dashboard Financiero & Estratégico");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 15, 500, 30);
        add(lblTitulo);

        // --- SECCIÓN 1: KPIs (Tarjetas de Resumen) ---
        int cardY = 60;
        int cardW = 320;
        int gap = 30;

        lblMRR = new JLabel("Cargando...");
        add(crearTarjeta("MRR (Ingreso Recurrente)", lblMRR, "Salud financiera base", new Color(37, 99, 235), 30, cardY, cardW));

        lblCaja = new JLabel("Cargando...");
        add(crearTarjeta("Flujo de Caja (Mes)", lblCaja, "Liquidez inmediata", new Color(22, 163, 74), 30 + cardW + gap, cardY, cardW));

        lblDeuda = new JLabel("Cargando...");
        add(crearTarjeta("Cartera Vencida", lblDeuda, "Riesgo de impago", new Color(220, 38, 38), 30 + (cardW + gap)*2, cardY, cardW));

        // --- SECCIÓN 2: GRID DE GRÁFICOS (2x2) ---
        // Usamos un panel interno con GridLayout para organizar los 4 gráficos
        JPanel panelGrid = new JPanel(new GridLayout(2, 2, 20, 20)); // 2 filas, 2 columnas, espacio 20px
        panelGrid.setBounds(30, 180, 1020, 500); // Ocupa gran parte de la pantalla
        panelGrid.setBackground(new Color(245, 247, 250));
        add(panelGrid);

        // Inicializamos los paneles contenedores
        pnlGraficoTendencia = crearContenedorGrafico("Tendencia de Crecimiento (MRR)");
        pnlGraficoPastel = crearContenedorGrafico("Distribución de Planes (Mix)");
        pnlGraficoChurn = crearContenedorGrafico("Altas vs Bajas (Crecimiento Neto)");
        pnlGraficoCaja = crearContenedorGrafico("Flujo de Caja (7 días)");

        // Agregamos al Grid
        panelGrid.add(pnlGraficoTendencia); // Arriba Izq
        panelGrid.add(pnlGraficoPastel);    // Arriba Der
        panelGrid.add(pnlGraficoChurn);     // Abajo Izq
        panelGrid.add(pnlGraficoCaja);      // Abajo Der
        
        JButton btnIngreso = new JButton("+ INGRESO");
        estilarBoton(btnIngreso, new Color(22, 163, 74)); // Verde
        btnIngreso.setBounds(800, 15, 120, 35);
        btnIngreso.addActionListener(e -> abrirDialogoMovimiento("INGRESO"));
        add(btnIngreso);

        JButton btnGasto = new JButton("- GASTO");
        estilarBoton(btnGasto, new Color(220, 38, 38)); // Rojo
        btnGasto.setBounds(930, 15, 120, 35);
        btnGasto.addActionListener(e -> abrirDialogoMovimiento("EGRESO"));
        add(btnGasto);
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


    // --- GENERADORES DE GRÁFICOS JFREECHART ---

    // 1. LÍNEA (Tendencia)
    private void generarGraficoLinea(JPanel contenedor, List<Object[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Object[] d : datos) dataset.addValue((Number)d[1], "Ingresos", d[0].toString());

        JFreeChart chart = ChartFactory.createLineChart("", "", "S/.", dataset, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = chart.getCategoryPlot();
        
        // Estilo Línea
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235)); // Azul
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        plot.setRenderer(renderer);
        estilarPlot(plot);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        agregarAlPanel(contenedor, chart);
    }

    // 2. DONUT (Distribución)
    private void generarGraficoDonut(JPanel contenedor, List<Object[]> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Object[] d : datos) dataset.setValue(d[0].toString(), (Number)d[1]);

        JFreeChart chart = ChartFactory.createRingChart("", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        
        plot.setSectionPaint("Fibra Hogar 50MB", new Color(34, 197, 94)); // Verde
        plot.setSectionPaint("Fibra Gamer 200MB", new Color(249, 115, 22)); // Naranja
        plot.setSectionPaint("Cable TV Básico", new Color(59, 130, 246)); // Azul
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(new Color(255,255,255, 200));
        
        agregarAlPanel(contenedor, chart);
    }

    // 3. BARRAS COMPARATIVAS (Churn: Altas vs Bajas)
    private void generarGraficoBarrasApiladas(JPanel contenedor, List<double[]> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] meses = {"Sep", "Oct", "Nov", "Dic"};
        for (int i=0; i<datos.size(); i++) {
            dataset.addValue(datos.get(i)[0], "Nuevos Clientes", (i<meses.length ? meses[i] : ""+i));
            dataset.addValue(datos.get(i)[1], "Bajas (Cancelaciones)", (i<meses.length ? meses[i] : ""+i));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "", "Clientes", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        
        renderer.setSeriesPaint(0, new Color(34, 197, 94)); // Verde (Nuevos)
        renderer.setSeriesPaint(1, new Color(239, 68, 68)); // Rojo (Bajas)
        estilarPlot(plot);
        
        agregarAlPanel(contenedor, chart);
    }

    // 4. BARRAS SIMPLES (Caja)
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

    // UTILIDADES DE ESTILO
    private void estilarPlot(CategoryPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
    }

    private void agregarAlPanel(JPanel contenedor, JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        contenedor.add(cp, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }
    
    // Tarjetas KPI (Mismo código de antes)
    private JPanel crearTarjeta(String titulo, JLabel lblValor, String subtitulo, Color color, int x, int y, int w) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, w, 100);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, color));
        
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblT.setForeground(Color.GRAY);
        lblT.setBounds(15, 10, 200, 20);
        card.add(lblT);
        
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValor.setBounds(15, 35, 250, 35);
        card.add(lblValor);
        
        JLabel lblS = new JLabel(subtitulo);
        lblS.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblS.setForeground(Color.LIGHT_GRAY);
        lblS.setBounds(15, 75, 250, 20);
        card.add(lblS);
        
        return card;
    }
    
    private void abrirDialogoMovimiento(String tipo) {
        // Obtener ventana padre para bloquearla
        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        java.awt.Frame frame = (parent instanceof java.awt.Frame) ? (java.awt.Frame) parent : null;

        DialogoMovimiento dialog = new DialogoMovimiento(frame, tipo);
        dialog.setVisible(true);

        // Si guardó algo, recargamos todo el dashboard para ver el impacto en los gráficos
        if (dialog.isGuardado()) {
            cargarDatos(); 
        }
    }
    
private void cargarDatos() {
        // 1. Mostrar barra de carga
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
        
        // Ejecutamos en un hilo secundario para no congelar la pantalla
        new Thread(() -> {
            try {
                // --- FASE 1: LANZAR TODAS LAS CONSULTAS A LA VEZ (PARALELO) ---
                // Esto es como enviar 7 mensajeros al mismo tiempo en lugar de uno por uno.
                
                // KPIs (Números)
                CompletableFuture<Double> futureMRR = CompletableFuture.supplyAsync(() -> finanzasDAO.calcularMRR());
                CompletableFuture<Double> futureCaja = CompletableFuture.supplyAsync(() -> finanzasDAO.obtenerBalanceMes());
                CompletableFuture<Double> futureDeuda = CompletableFuture.supplyAsync(() -> finanzasDAO.calcularDeudaTotal());

                // Datos de Gráficos (Listas)
                CompletableFuture<List<Object[]>> futureHist = CompletableFuture.supplyAsync(() -> finanzasDAO.obtenerHistorialMRR());
                CompletableFuture<List<Object[]>> futurePlanes = CompletableFuture.supplyAsync(() -> finanzasDAO.obtenerDistribucionPlanes());
                CompletableFuture<List<double[]>> futureChurn = CompletableFuture.supplyAsync(() -> finanzasDAO.obtenerAltasBajasUltimosMeses());
                CompletableFuture<List<double[]>> futureFlujo = CompletableFuture.supplyAsync(() -> finanzasDAO.obtenerFlujoUltimos7Dias());

                // --- FASE 2: ESPERAR RESULTADOS ---
                // .get() espera a que llegue la respuesta. Como se lanzaron juntas,
                // el tiempo total será solo el de la consulta más lenta (ej. 2 seg), no la suma (10 seg).
                double mrr = futureMRR.get();
                double caja = futureCaja.get();
                double deuda = futureDeuda.get();
                List<Object[]> histMRR = futureHist.get();
                List<Object[]> planes = futurePlanes.get();
                List<double[]> churn = futureChurn.get();
                List<double[]> flujo = futureFlujo.get();

                // --- FASE 3: PINTAR EN PANTALLA ---
                SwingUtilities.invokeLater(() -> {
                    // Actualizar KPIs
                    lblMRR.setText("S/. " + String.format("%.2f", mrr));
                    lblCaja.setText("S/. " + String.format("%.2f", caja));
                    lblDeuda.setText("S/. " + String.format("%.2f", deuda));
                    
                    // Actualizar Gráficos
                    try {
                        generarGraficoLinea(pnlGraficoTendencia, histMRR);
                        generarGraficoDonut(pnlGraficoPastel, planes);
                        generarGraficoBarrasApiladas(pnlGraficoChurn, churn);
                        generarGraficoBarrasSimples(pnlGraficoCaja, flujo);
                    } catch (Exception e) {
                        System.err.println("Error pintando gráficos: " + e.getMessage());
                    }

                    // Ocultar barra de carga
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                });

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                });
            }
        }).start();
    }

    // Pega esto al final de la clase PanelFinanzas, antes del último }
    private void estilarBoton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Borde redondeado sutil (Opcional, si quieres que se vea mejor)
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }
}