package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Empleado;

/**
 * Ventana principal para empleados (acceso restringido).
 * Solo tienen acceso a: Mi Cuenta, Pagos, Salir
 */
public class PrincipalEmpleado extends JFrame {

    private Empleado empleadoLogueado;
    private JPanel panelContenido;
    private JButton btnActivo = null;

    // Botones del menú
    private JButton btnCuenta;
    private JButton btnPagos;
    private JButton btnSalir;

    // Colores (igual que Principal.java)
    private static final Color COLOR_SIDEBAR = new Color(15, 23, 42);
    private static final Color COLOR_HOVER = new Color(30, 41, 59);
    private static final Color COLOR_TEXTO_INACTIVO = new Color(148, 163, 184);
    private static final Color COLOR_TEXTO_ACTIVO = Color.WHITE;
    private static final Color COLOR_FONDO = new Color(241, 245, 249);
    private static final Color COLOR_ACTIVO = new Color(37, 99, 235);

    public PrincipalEmpleado(Empleado empleado) {
        this.empleadoLogueado = empleado;

        setTitle("FIBRANET - Portal Empleado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        initUI();
        configurarDisenoModerno();

        // Abrir módulo de Pagos por defecto
        btnPagos.doClick();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- MENÚ LATERAL ---
        JPanel menuLateral = new JPanel();
        menuLateral.setBackground(COLOR_SIDEBAR);
        menuLateral.setPreferredSize(new Dimension(200, 0));
        menuLateral.setLayout(new BorderLayout());

        // Panel superior (logo + botones)
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(COLOR_SIDEBAR);
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));

        // Logo
        JLabel lblLogo = new JLabel();
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(15, 0, 20, 0));
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/img/Sin título (1)_1.png"));
            Image img = icon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblLogo.setText("FIBRANET");
            lblLogo.setForeground(Color.WHITE);
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }
        panelSuperior.add(lblLogo);

        // Botones del menú
        btnCuenta = new JButton("Mi Cuenta");
        btnPagos = new JButton("Pagos");
        btnSalir = new JButton("Salir");

        // Alinear botones a la izquierda (IMPORTANTE)
        btnCuenta.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPagos.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalir.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Agregar botones al panel superior
        panelSuperior.add(btnCuenta);
        panelSuperior.add(btnPagos);

        menuLateral.add(panelSuperior, BorderLayout.NORTH);

        // Panel inferior (usuario + salir)
        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(COLOR_SIDEBAR);
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        // Usuario actual
        JLabel lblUsuario = new JLabel(empleadoLogueado != null ? empleadoLogueado.getNombreCompleto() : "Empleado");
        lblUsuario.setForeground(COLOR_TEXTO_INACTIVO);
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblUsuario.setBorder(new EmptyBorder(10, 20, 5, 10));
        panelInferior.add(lblUsuario);

        btnSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInferior.add(btnSalir);
        panelInferior.add(Box.createVerticalStrut(15));

        menuLateral.add(panelInferior, BorderLayout.SOUTH);

        add(menuLateral, BorderLayout.WEST);

        // --- PANEL DE CONTENIDO ---
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(COLOR_FONDO);
        add(panelContenido, BorderLayout.CENTER);

        // Acciones de los botones
        btnCuenta.addActionListener(e -> {
            marcarBotonActivo(btnCuenta);
            mostrarPanel(new subpanel_MiPerfil(empleadoLogueado));
        });

        btnPagos.addActionListener(e -> {
            marcarBotonActivo(btnPagos);
            mostrarPanel(new subpanel_Caja());
        });

        btnSalir.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "¿Desea cerrar sesión?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                this.dispose();
                new Login().setVisible(true);
            }
        });
    }

    private void configurarDisenoModerno() {
        // Configurar cada botón con su icono
        configurarBoton(btnCuenta, "Mi Cuenta", "/img/usuarios.png");
        configurarBoton(btnPagos, "Pagos", "/img/pagos.png");
        configurarBoton(btnSalir, "Salir", "/img/salir.png");
    }

    private void configurarBoton(JButton btn, String texto, String rutaIcono) {
        // 1. Cargar el Icono
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource(rutaIcono));
            btn.setIcon(icono);
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + rutaIcono);
        }

        // 2. Texto y Espaciado
        btn.setText("   " + texto);
        btn.setIconTextGap(5);

        // 3. Estilo Visual
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(COLOR_TEXTO_INACTIVO);

        // Limpieza de bordes
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(COLOR_SIDEBAR);

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(10, 20, 10, 10));

        // Tamaño fijo para todos los botones
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setPreferredSize(new Dimension(200, 45));

        // 4. Efecto Hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != btnActivo) {
                    btn.setBackground(COLOR_HOVER);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != btnActivo) {
                    btn.setBackground(COLOR_SIDEBAR);
                }
            }
        });
    }

    private void marcarBotonActivo(JButton btn) {
        // Desactivar el botón anterior
        if (btnActivo != null) {
            btnActivo.setBackground(COLOR_SIDEBAR);
            btnActivo.setForeground(COLOR_TEXTO_INACTIVO);
        }

        // Activar el nuevo
        btn.setBackground(COLOR_ACTIVO);
        btn.setForeground(COLOR_TEXTO_ACTIVO);
        btnActivo = btn;
    }

    private void mostrarPanel(JPanel panel) {
        panelContenido.removeAll();
        panelContenido.add(panel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    public Empleado getEmpleadoLogueado() {
        return empleadoLogueado;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PrincipalEmpleado(null).setVisible(true);
        });
    }
}
