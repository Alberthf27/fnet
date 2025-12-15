package vista.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CardPanel extends JPanel {
    
    // 1. Declaramos las etiquetas como variables globales de la clase
    private JLabel lblValor;
    private JLabel lblSubtitulo;
    private JLabel lblIcono;

    public CardPanel(String titulo, String valor, String subtitulo, String rutaIcono, Color colorIcono) {
        setOpaque(false);
        setLayout(null); 
        setBackground(Color.WHITE);
        
        // --- Icono ---
        lblIcono = new JLabel();
        try {
            if(rutaIcono != null && !rutaIcono.isEmpty()) {
                lblIcono.setIcon(new javax.swing.ImageIcon(getClass().getResource(rutaIcono)));
            } else {
                lblIcono.setText("●");
                lblIcono.setForeground(colorIcono);
                lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 20));
            }
        } catch(Exception e) {}
        lblIcono.setBounds(180, 15, 40, 40);
        lblIcono.setHorizontalAlignment(SwingConstants.RIGHT);
        add(lblIcono);

        // --- Título ---
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(100, 116, 139));
        lblTitulo.setBounds(20, 20, 150, 20);
        add(lblTitulo);
        
        // --- Valor Principal (Instanciamos la variable global) ---
        lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(new Color(15, 23, 42));
        lblValor.setBounds(20, 45, 200, 30);
        add(lblValor);
        
        // --- Subtítulo (Instanciamos la variable global) ---
        lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(colorIcono);
        lblSubtitulo.setBounds(20, 80, 200, 20);
        add(lblSubtitulo);
    }

    // 2. MÉTODO QUE FALTABA: Permite actualizar los datos desde panel_Gerente
    public void setData(String nuevoValor, String nuevoSubtitulo) {
        // Ejecutar en el hilo de Swing para evitar parpadeos o errores
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblValor.setText(nuevoValor);
            if(nuevoSubtitulo != null && !nuevoSubtitulo.isEmpty()) {
                lblSubtitulo.setText(nuevoSubtitulo);
            }
            repaint(); // Forzar repintado visual
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo Blanco con Bordes Redondeados
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        
        // Borde sutil gris
        g2.setColor(new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
        
        g2.dispose();
    }
}