package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import vista.componentes.CardPanel;

public class panel_Gerente extends javax.swing.JPanel {

    // Variables globales para poder moverlas luego
    private CardPanel card1, card2, card3, card4;
    private JPanel chartPanel, summaryPanel;
    private JScrollPane scrollPane;
    private JLabel lblTableTitle;

public panel_Gerente() {
        setBackground(new Color(241, 245, 249)); 
        setLayout(null); 
        
        initContenido();
        
        cargarGraficoIngresos(); // <--- LLAMADA PARA PINTAR EL GRÁFICO
        // --------------------------------

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

        // 2. CREAR TARJETAS (Posiciones iniciales temporales)
        card1 = new CardPanel("Ingresos de Hoy", "S/. 1,250.00", "+12% vs ayer", "", new Color(22, 163, 74));
        card2 = new CardPanel("Deuda Total", "S/. 4,500.00", "35 clientes morosos", "", new Color(220, 38, 38));
        card3 = new CardPanel("Clientes Activos", "1,284", "Red operativa", "", new Color(37, 99, 235));
        card4 = new CardPanel("Clientes Cortados", "76", "Último corte: hace 2h", "", new Color(100, 116, 139));
        
        add(card1); add(card2); add(card3); add(card4);

        // 3. GRÁFICO
        chartPanel = new JPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setLayout(null);
        
        JLabel lblChartTitle = new JLabel("Ingresos Mensuales");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblChartTitle.setBounds(20, 15, 200, 20);
        chartPanel.add(lblChartTitle);
        
        JLabel lblChartPlaceholder = new JLabel("<html><div style='text-align:center; color:gray;'>[ AQUI VA EL GRÁFICO ]</div></html>");
        lblChartPlaceholder.setBounds(0, 50, 650, 200); 
        lblChartPlaceholder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        chartPanel.add(lblChartPlaceholder);
        add(chartPanel);

        // 4. RESUMEN LATERAL
        summaryPanel = new JPanel();
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setLayout(null);
        
        JLabel lblSummaryTitle = new JLabel("Resumen de Clientes");
        lblSummaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSummaryTitle.setBounds(20, 15, 200, 20);
        summaryPanel.add(lblSummaryTitle);
        
        agregarDatoResumen(summaryPanel, "Nuevos este mes", "48", 50);
        agregarDatoResumen(summaryPanel, "Bajas voluntarias", "8", 90);
        agregarDatoResumen(summaryPanel, "Cortes por deuda", "21", 130);
        add(summaryPanel);

        // 5. TABLA
        lblTableTitle = new JLabel("Últimos Pagos Registrados");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        add(lblTableTitle);

        String[] columnas = {"Hora", "Cliente", "Monto", "Método"};
        Object[][] datos = {
            {"10:45", "Juan Perez - Plan 50MB", "S/. 50.00", "Yape"},
            {"11:20", "Maria Lopez - Plan 100MB", "S/. 80.00", "Efectivo"},
            {"12:05", "Carlos Ruiz - Plan Duo", "S/. 120.00", "Transferencia"}
        };
        
        DefaultTableModel model = new DefaultTableModel(datos, columnas);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.setShowVerticalLines(false);
        
        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scrollPane);
    }
    
    // MÉTODO MATEMÁTICO PARA ADAPTAR EL TAMAÑO
    private void recalcularPosiciones() {
        int anchoTotal = getWidth();
        if (anchoTotal == 0) return; // Aún no visible
        
        // Márgenes
        int margenX = 30;
        int gap = 20;
        
        // Ancho útil disponible (restamos márgenes)
        int anchoUtil = anchoTotal - (margenX * 2);
        
        // 1. TARJETAS (Dividimos ancho entre 4)
        int cardW = (anchoUtil - (gap * 3)) / 4; 
        int cardH = 120;
        int cardY = 90;
        
        card1.setBounds(margenX, cardY, cardW, cardH);
        card2.setBounds(margenX + cardW + gap, cardY, cardW, cardH);
        card3.setBounds(margenX + (cardW + gap)*2, cardY, cardW, cardH);
        card4.setBounds(margenX + (cardW + gap)*3, cardY, cardW, cardH);
        
        // 2. GRÁFICO Y RESUMEN
        int chartY = 230;
        int chartH = 280;
        // El gráfico ocupa el 70% del ancho, el resumen el 30%
        int chartW = (int) (anchoUtil * 0.70) - gap;
        int summaryW = anchoUtil - chartW - gap;
        
        chartPanel.setBounds(margenX, chartY, chartW, chartH);
        summaryPanel.setBounds(margenX + chartW + gap, chartY, summaryW, chartH);
        
        // 3. TABLA (Ancho total)
        int tableY = 530;
        lblTableTitle.setBounds(margenX, tableY, 300, 20);
        scrollPane.setBounds(margenX, tableY + 30, anchoUtil, 200); // 200 altura tabla
        
        // Forzar repintado
        revalidate();
        repaint();
    }
    
    private void agregarDatoResumen(JPanel panel, String label, String valor, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.GRAY);
        lbl.setBounds(20, y, 200, 20);
        panel.add(lbl);
        
        JLabel val = new JLabel(valor);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        val.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        val.setBounds(200, y, 80, 20); // Ajustado para que no se salga
        panel.add(val);
        
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new Color(240,240,240));
        sep.setBounds(20, y+30, 280, 10);
        panel.add(sep);
    }
    
    // Importaciones necesarias al inicio del archivo:
    // import org.jfree.chart.ChartFactory;
    // import org.jfree.chart.ChartPanel;
    // import org.jfree.chart.JFreeChart;
    // import org.jfree.chart.plot.PlotOrientation;
    // import org.jfree.data.category.DefaultCategoryDataset;
    // import java.awt.BorderLayout;

    private void cargarGraficoIngresos() {
        // 1. Obtener datos reales (Usando FinanzasDAO)
        // Usamos un hilo para no congelar la UI
        new Thread(() -> {
            DAO.FinanzasDAO dao = new DAO.FinanzasDAO();
            java.util.List<Object[]> historial = dao.obtenerHistorialMRR(); // [Mes, Monto]

            // 2. Crear Dataset para el gráfico
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Object[] dato : historial) {
                dataset.addValue((Number) dato[1], "Ingresos", dato[0].toString());
            }

            // 3. Crear el Gráfico (Estilo Plano)
            JFreeChart chart = ChartFactory.createAreaChart(
                "",                 // Título (Ya tienes un JLabel fuera)
                "",                 // Eje X
                "",                 // Eje Y
                dataset,            // Datos
                PlotOrientation.VERTICAL,
                false,              // Leyenda
                true,               // Tooltips
                false               // URLs
            );

            // Personalización Visual (Flat Design)
            chart.setBackgroundPaint(Color.WHITE);
            org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(new Color(230, 230, 230)); // Líneas guía suaves
            
            // Color del Área (Azul corporativo con transparencia)
            org.jfree.chart.renderer.category.AreaRenderer renderer = (org.jfree.chart.renderer.category.AreaRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(37, 99, 235, 180)); // Azul #2563EB
            renderer.setSeriesOutlinePaint(0, new Color(37, 99, 235));

            // 4. Insertar en el panel existente (chartPanel)
            javax.swing.SwingUtilities.invokeLater(() -> {
                chartPanel.removeAll(); // Limpiar el texto "[ AQUI VA EL GRAFICO ]"
                chartPanel.setLayout(new java.awt.BorderLayout()); // Necesario para que el chart se expanda
                
                // Agregamos el título que ya tenías para no perderlo
                JLabel lblChartTitle = new JLabel(" Ingresos Mensuales");
                lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
                lblChartTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
                chartPanel.add(lblChartTitle, java.awt.BorderLayout.NORTH);

                ChartPanel cp = new ChartPanel(chart);
                cp.setMouseWheelEnabled(false);
                chartPanel.add(cp, java.awt.BorderLayout.CENTER);
                
                chartPanel.revalidate();
                chartPanel.repaint();
            });
        }).start();
    }
}