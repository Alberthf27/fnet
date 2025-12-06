
package vista.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CardPanel extends JPanel {
    
    public CardPanel(String titulo, String valor, String subtitulo, String rutaIcono, Color colorIcono) {
        setOpaque(false);
        setLayout(null); // Diseño absoluto interno
        setBackground(Color.WHITE);
        
        // 1. Icono (Esquina superior derecha)
        JLabel lblIcono = new JLabel();
        try {
            // Si tienes iconos, úsalos. Si no, usamos un cuadro de color temporal
            if(rutaIcono != null && !rutaIcono.isEmpty()) {
                lblIcono.setIcon(new javax.swing.ImageIcon(getClass().getResource(rutaIcono)));
            } else {
                lblIcono.setText("●"); // Punto como placeholder
                lblIcono.setForeground(colorIcono);
                lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 20));
            }
        } catch(Exception e) {}
        lblIcono.setBounds(180, 15, 40, 40);
        lblIcono.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        add(lblIcono);

        // 2. Título (Ej: Ingresos de Hoy)
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(new Color(100, 116, 139)); // Gris azulado (Slate 500)
        lblTitulo.setBounds(20, 20, 150, 20);
        add(lblTitulo);
        
        // 3. Valor Principal (Ej: S/. 1,250.00)
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(new Color(15, 23, 42)); // Azul oscuro (Slate 900)
        lblValor.setBounds(20, 45, 200, 30);
        add(lblValor);
        
        // 4. Subtítulo (Ej: +12% vs ayer)
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(colorIcono); // Color verde/rojo según el dato
        lblSub.setBounds(20, 80, 200, 20);
        add(lblSub);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo Blanco con Bordes Redondeados
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        
        // Borde sutil gris (opcional)
        g2.setColor(new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
        
        g2.dispose();
    }
}