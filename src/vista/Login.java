package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Login extends javax.swing.JFrame {

    // --- PALETA DE COLORES "FLAT" (PLANA) ---
    private final Color COLOR_FONDO = new Color(15, 23, 42);
    private final Color COLOR_ACCENTO = new Color(255, 102, 0);
    private final Color COLOR_ACCENTO_HOVER = new Color(204, 82, 0);
    private final Color COLOR_TEXTO_HINT = new Color(160, 174, 192);

    // Variables globales para poder usarlas dentro de los eventos
    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    public Login() {
        setUndecorated(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponentsPersonalizado();
    }

    private void initComponentsPersonalizado() {
        // 1. PANEL PRINCIPAL
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(COLOR_FONDO);
        setContentPane(mainPanel);

        // Botón Cerrar (X)
        JLabel btnCerrar = new JLabel("X");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setHorizontalAlignment(SwingConstants.CENTER);
        btnCerrar.setBounds(860, 10, 30, 30);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }

            public void mouseEntered(MouseEvent e) {
                btnCerrar.setForeground(COLOR_ACCENTO);
            }

            public void mouseExited(MouseEvent e) {
                btnCerrar.setForeground(Color.WHITE);
            }
        });
        mainPanel.add(btnCerrar);

        // 2. TARJETA CENTRAL
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(275, 100, 350, 420);
        cardPanel.setLayout(null);

        cardPanel.setBorder(new EmptyBorder(0, 0, 0, 0) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, width - 1, height - 1, 15, 15);
            }
        });
        mainPanel.add(cardPanel);

        // --- CONTENIDO DE LA TARJETA ---
        JLabel lblLogo = new JLabel();
        ImageIcon icono = cargarIcono("/img/logo_fibranet.png", 180, 60);
        if (icono != null) {
            lblLogo.setIcon(icono);
        } else {
            lblLogo.setText("FIBRANET");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblLogo.setForeground(COLOR_FONDO);
        }
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(25, 40, 300, 60);
        cardPanel.add(lblLogo);

        JLabel lblTitulo = new JLabel("Acceso al Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(100, 116, 139));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBounds(25, 110, 300, 20);
        cardPanel.add(lblTitulo);

        // Input Usuario
        txtUsuario = crearTextFieldPersonalizado("Usuario");
        txtUsuario.setBounds(40, 160, 270, 45);
        cardPanel.add(txtUsuario);

        // Input Contraseña
        txtPassword = crearPasswordFieldPersonalizado("Contraseña");
        txtPassword.setBounds(40, 220, 270, 45);
        cardPanel.add(txtPassword);

        // BOTÓN INGRESAR
        JButton btnEntrar = new JButton("INGRESAR");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setBackground(COLOR_ACCENTO);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.setBounds(40, 300, 270, 50);

        btnEntrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnEntrar.setBackground(COLOR_ACCENTO_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                btnEntrar.setBackground(COLOR_ACCENTO);
            }
        });

        // ====================================================================
        // AQUÍ ES DONDE SE CONECTA EL BOTÓN (DENTRO DEL MÉTODO DE INICIO)
        // ====================================================================
// Acción del botón INGRESAR
        btnEntrar.addActionListener(e -> {
            String codigo = txtUsuario.getText(); // "0001"
            String pass = new String(txtPassword.getPassword());
            
            // 1. Validaciones
            if(codigo.isEmpty() || codigo.equals("Usuario") || pass.isEmpty() || pass.equals("Contraseña")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Por favor, ingrese código y contraseña.");
                return;
            }

            // 2. Login (Pasamos el código como String)
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            
            DAO.UsuarioDAO dao = new DAO.UsuarioDAO();
            // ¡OJO! Asegúrate de actualizar UsuarioDAO primero
            modelo.Empleado emp = dao.login(codigo, pass); 
            
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            if (emp != null) {
                this.dispose();
                new Principal(emp).setVisible(true);
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Código o contraseña incorrectos.", 
                    "Acceso Denegado", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        cardPanel.add(btnEntrar);

        // Footer
        JLabel lblFooter = new JLabel("v1.0 - Sistema ISP");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(200, 200, 200));
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        lblFooter.setBounds(0, 570, 900, 20);
        mainPanel.add(lblFooter);
    }

    // --- MÉTODOS AUXILIARES ---
    private JTextField crearTextFieldPersonalizado(String placeholder) {
        JTextField txt = new JTextField();
        estilarCampoTexto(txt, placeholder);
        return txt;
    }

    private JPasswordField crearPasswordFieldPersonalizado(String placeholder) {
        JPasswordField txt = new JPasswordField();
        estilarCampoTexto(txt, placeholder);
        txt.setEchoChar('●');
        return txt;
    }

    private void estilarCampoTexto(JTextField txt, String placeholder) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.BLACK);
        txt.setBackground(new Color(248, 250, 252));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        txt.setText(placeholder);
        txt.setForeground(COLOR_TEXTO_HINT);

        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText("");
                    txt.setForeground(Color.BLACK);
                    txt.setBackground(Color.WHITE);
                    txt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COLOR_ACCENTO, 1),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
                }
            }

            public void focusLost(FocusEvent evt) {
                if (txt.getText().isEmpty()) {
                    txt.setForeground(COLOR_TEXTO_HINT);
                    txt.setText(placeholder);
                    txt.setBackground(new Color(248, 250, 252));
                    txt.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(226, 232, 240)),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
                }
            }
        });
    }

    private ImageIcon cargarIcono(String ruta, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(ruta);
            if (url != null) {
                java.awt.Image img = new javax.swing.ImageIcon(url).getImage();
                return new javax.swing.ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}
