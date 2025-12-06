package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import vista.componentes.CardPanel;

public class panel_Gerente extends javax.swing.JPanel {

    public panel_Gerente() {
        setBackground(new Color(241, 245, 249)); // Fondo Gris
        setLayout(new GridBagLayout()); // <--- EL CAMBIO MÁGICO
        initContenido();
    }

    private void initContenido() {
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Configuración base para el GBC
        gbc.insets = new Insets(10, 20, 10, 20); // Margen externo (Arriba, Izq, Abajo, Der)
        gbc.fill = GridBagConstraints.BOTH; // Estirar en ambos sentidos

        // ====================================================================
        // 1. TÍTULO (Fila 0)
        // ====================================================================
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));
        
        gbc.gridx = 0; // Columna 0
        gbc.gridy = 0; // Fila 0
        gbc.gridwidth = 4; // Ocupa todo el ancho (4 tarjetas)
        gbc.weightx = 1.0; // Estirar horizontalmente
        gbc.weighty = 0.0; // No estirar verticalmente (altura fija)
        add(title, gbc);

        // ====================================================================
        // 2. TARJETAS KPI (Fila 1) - 4 Columnas
        // ====================================================================
        gbc.gridwidth = 1; // Ahora cada tarjeta ocupa 1 columna
        gbc.gridy = 1;     // Fila 1
        gbc.weighty = 0.15; // Ocupan un 15% de la altura disponible
        gbc.insets = new Insets(10, 10, 10, 10); // Margen entre tarjetas

        // Tarjeta 1
        gbc.gridx = 0;
        add(new CardPanel("Ingresos Hoy", "S/. 1,250", "+12%", "/img/money.png", new Color(22, 163, 74)), gbc);

        // Tarjeta 2
        gbc.gridx = 1;
        add(new CardPanel("Deuda Total", "S/. 4,500", "35 morosos", "/img/alert.png", new Color(220, 38, 38)), gbc);

        // Tarjeta 3
        gbc.gridx = 2;
        add(new CardPanel("Clientes", "1,284", "Activos", "/img/users.png", new Color(37, 99, 235)), gbc);

        // Tarjeta 4
        gbc.gridx = 3;
        add(new CardPanel("Cortados", "76", "Sin servicio", "/img/off.png", Color.GRAY), gbc);

        // ====================================================================
        // 3. GRÁFICO y RESUMEN (Fila 2)
        // ====================================================================
        gbc.gridy = 2;
        gbc.weighty = 0.45; // Esta fila es la más alta (45%)

        // Panel Gráfico (Izquierda - Ocupa 3 columnas)
        JPanel chartPanel = crearPanelBlanco("Ingresos Mensuales");
        chartPanel.add(new JLabel("<html><center><h1>📊</h1><br>Gráfico aquí</center></html>")); // Placeholder
        
        gbc.gridx = 0;
        gbc.gridwidth = 3; // Ocupa 3 de 4 partes
        add(chartPanel, gbc);

        // Panel Resumen (Derecha - Ocupa 1 columna)
        JPanel summaryPanel = crearPanelBlanco("Resumen");
        // (Aquí agregarías tus labels de resumen)
        
        gbc.gridx = 3;
        gbc.gridwidth = 1; // Ocupa el resto
        add(summaryPanel, gbc);

        // ====================================================================
        // 4. TABLA (Fila 3)
        // ====================================================================
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4; // Ocupa todo el ancho
        gbc.weighty = 0.4; // 40% de altura restante
        
        // Tabla dentro de Scroll
        JScrollPane scroll = crearTablaEjemplo();
        add(scroll, gbc);
    }

    // Método auxiliar para crear paneles blancos redondeados (Simulado)
    private JPanel crearPanelBlanco(String titulo) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(lbl, java.awt.BorderLayout.NORTH);
        return p;
    }

    private JScrollPane crearTablaEjemplo() {
        String[] cols = {"Hora", "Cliente", "Monto", "Método"};
        Object[][] data = {{"10:00", "Juan", "S/ 50", "Yape"}, {"11:00", "Ana", "S/ 80", "Efectivo"}};
        JTable t = new JTable(new DefaultTableModel(data, cols));
        t.setRowHeight(25);
        return new JScrollPane(t);
    }
}