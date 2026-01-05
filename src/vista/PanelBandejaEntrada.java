package vista;

import DAO.AlertaDAO;
import modelo.AlertaGerente;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel de Bandeja de Entrada para alertas del gerente.
 * Muestra notificaciones del sistema que requieren atención.
 */
public class PanelBandejaEntrada extends JPanel {

    private final AlertaDAO alertaDAO;
    private JPanel panelContenido;
    private JLabel lblContador;
    private JScrollPane scrollPane;

    private static final Color COLOR_FONDO = new Color(30, 30, 36);
    private static final Color COLOR_TARJETA = new Color(45, 45, 52);
    private static final Color COLOR_TARJETA_HOVER = new Color(55, 55, 65);
    private static final Color COLOR_ACENTO = new Color(99, 102, 241);
    private static final Color COLOR_TEXTO = new Color(229, 231, 235);
    private static final Color COLOR_TEXTO_SECUNDARIO = new Color(156, 163, 175);
    private static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_ALERTA_TITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public PanelBandejaEntrada() {
        this.alertaDAO = new AlertaDAO();
        inicializarUI();
        cargarAlertas();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel panelHeader = crearHeader();
        add(panelHeader, BorderLayout.NORTH);

        // Contenido con scroll
        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(COLOR_FONDO);

        scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_FONDO);
        scrollPane.getViewport().setBackground(COLOR_FONDO);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Título con contador
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTitulo.setBackground(COLOR_FONDO);

        JLabel lblTitulo = new JLabel("📬 Bandeja de Entrada");
        lblTitulo.setFont(FONT_TITULO);
        lblTitulo.setForeground(COLOR_TEXTO);
        panelTitulo.add(lblTitulo);

        lblContador = new JLabel("0 pendientes");
        lblContador.setFont(FONT_SMALL);
        lblContador.setForeground(COLOR_TEXTO_SECUNDARIO);
        panelTitulo.add(lblContador);

        panel.add(panelTitulo, BorderLayout.WEST);

        // Botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setBackground(COLOR_FONDO);

        JButton btnRefrescar = crearBoton("Actualizar", e -> cargarAlertas());
        JButton btnMarcarTodas = crearBoton("✓ Marcar todas como leídas", e -> marcarTodasComoLeidas());

        panelAcciones.add(btnMarcarTodas);
        panelAcciones.add(btnRefrescar);
        panel.add(panelAcciones, BorderLayout.EAST);

        return panel;
    }

    private JButton crearBoton(String texto, ActionListener action) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_SMALL);
        btn.setForeground(COLOR_TEXTO);
        btn.setBackground(COLOR_TARJETA);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_ACENTO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_TARJETA);
            }
        });

        btn.addActionListener(action);
        return btn;
    }

    public void cargarAlertas() {
        panelContenido.removeAll();

        List<AlertaGerente> alertas = alertaDAO.obtenerNoLeidas();
        lblContador.setText(alertas.size() + " pendiente" + (alertas.size() != 1 ? "s" : ""));

        if (alertas.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay alertas pendientes");
            lblVacio.setFont(FONT_NORMAL);
            lblVacio.setForeground(COLOR_TEXTO_SECUNDARIO);
            lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblVacio.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            panelContenido.add(Box.createVerticalGlue());
            panelContenido.add(lblVacio);
            panelContenido.add(Box.createVerticalGlue());
        } else {
            for (AlertaGerente alerta : alertas) {
                panelContenido.add(crearTarjetaAlerta(alerta));
                panelContenido.add(Box.createVerticalStrut(10));
            }
        }

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private JPanel crearTarjetaAlerta(AlertaGerente alerta) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 5));
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Mouse hover effect
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(COLOR_TARJETA_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(COLOR_TARJETA);
            }
        });

        // Icono + Título
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);

        JLabel lblIcono = new JLabel(alerta.getIcono() + " ");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel lblTitulo = new JLabel(alerta.getTitulo());
        lblTitulo.setFont(FONT_ALERTA_TITULO);
        lblTitulo.setForeground(COLOR_TEXTO);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
        JLabel lblFecha = new JLabel(sdf.format(alerta.getFechaCreacion()));
        lblFecha.setFont(FONT_SMALL);
        lblFecha.setForeground(COLOR_TEXTO_SECUNDARIO);

        JPanel panelTituloIcono = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelTituloIcono.setOpaque(false);
        panelTituloIcono.add(lblIcono);
        panelTituloIcono.add(lblTitulo);

        panelTop.add(panelTituloIcono, BorderLayout.WEST);
        panelTop.add(lblFecha, BorderLayout.EAST);

        tarjeta.add(panelTop, BorderLayout.NORTH);

        // Mensaje
        JTextArea txtMensaje = new JTextArea(alerta.getMensaje());
        txtMensaje.setFont(FONT_NORMAL);
        txtMensaje.setForeground(COLOR_TEXTO_SECUNDARIO);
        txtMensaje.setBackground(COLOR_TARJETA);
        txtMensaje.setEditable(false);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBorder(BorderFactory.createEmptyBorder(5, 30, 0, 0));
        tarjeta.add(txtMensaje, BorderLayout.CENTER);

        // Botón marcar como leída
        JButton btnMarcar = new JButton("Marcar como leída");
        btnMarcar.setFont(FONT_SMALL);
        btnMarcar.setForeground(COLOR_ACENTO);
        btnMarcar.setBackground(COLOR_TARJETA);
        btnMarcar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        btnMarcar.setContentAreaFilled(false);
        btnMarcar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarcar.addActionListener(e -> {
            alertaDAO.marcarComoLeida(alerta.getIdAlerta());
            cargarAlertas();
        });

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelAcciones.setOpaque(false);
        panelAcciones.add(btnMarcar);
        tarjeta.add(panelAcciones, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void marcarTodasComoLeidas() {
        alertaDAO.marcarTodasComoLeidas();
        cargarAlertas();
    }

    /**
     * Obtiene el conteo de alertas no leídas (para badge).
     */
    public int getConteoNoLeidas() {
        return alertaDAO.contarNoLeidas();
    }
}
