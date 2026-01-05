package vista;

import DAO.ConfiguracionDAO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Diálogo para configurar los parámetros del sistema de cobros automáticos.
 */
public class DialogoConfiguracion extends JDialog {

    private final ConfiguracionDAO configDAO;

    private JSpinner spinnerPlazoDias;
    private JSpinner spinnerDiasRecordatorio;
    private JCheckBox chkWhatsAppHabilitado;
    private JCheckBox chkRouterHabilitado;
    private JTextField txtApiKeyCallMeBot;
    private JTextField txtMikroTikIP;
    private JTextField txtMikroTikUsuario;
    private JPasswordField txtMikroTikPassword;

    private static final Color COLOR_FONDO = new Color(30, 30, 36);
    private static final Color COLOR_PANEL = new Color(45, 45, 52);
    private static final Color COLOR_TEXTO = new Color(229, 231, 235);
    private static final Color COLOR_ACENTO = new Color(99, 102, 241);
    private static final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    public DialogoConfiguracion(Frame parent) {
        super(parent, "Configuracion del Sistema", true);
        this.configDAO = new ConfiguracionDAO();

        inicializarUI();
        cargarConfiguraciones();

        setSize(500, 550);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void inicializarUI() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 15));
        panelPrincipal.setBackground(COLOR_FONDO);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Configuración de Cobros Automáticos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(COLOR_TEXTO);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // Panel central con secciones
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(COLOR_FONDO);

        // Sección: Plazos
        panelCentral.add(crearSeccion("Plazos de Pago", crearPanelPlazos()));
        panelCentral.add(Box.createVerticalStrut(15));

        // Sección: WhatsApp
        panelCentral.add(crearSeccion("WhatsApp (CallMeBot)", crearPanelWhatsApp()));
        panelCentral.add(Box.createVerticalStrut(15));

        // Sección: Router
        panelCentral.add(crearSeccion("🌐 Router MikroTik", crearPanelRouter()));

        JScrollPane scroll = new JScrollPane(panelCentral);
        scroll.setBorder(null);
        scroll.setBackground(COLOR_FONDO);
        scroll.getViewport().setBackground(COLOR_FONDO);
        panelPrincipal.add(scroll, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setBackground(COLOR_FONDO);

        JButton btnCancelar = crearBoton("Cancelar", false);
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = crearBoton("Guardar", true);
        btnGuardar.addActionListener(e -> guardarConfiguraciones());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    private JPanel crearSeccion(String titulo, JPanel contenido) {
        JPanel seccion = new JPanel(new BorderLayout(0, 10));
        seccion.setBackground(COLOR_PANEL);
        seccion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        seccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONT_TITULO);
        lblTitulo.setForeground(COLOR_TEXTO);
        seccion.add(lblTitulo, BorderLayout.NORTH);
        seccion.add(contenido, BorderLayout.CENTER);

        return seccion;
    }

    private JPanel crearPanelPlazos() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);

        panel.add(crearLabel("Plazo de pago (días):"));
        spinnerPlazoDias = new JSpinner(new SpinnerNumberModel(21, 1, 60, 1));
        estilizarSpinner(spinnerPlazoDias);
        panel.add(spinnerPlazoDias);

        panel.add(crearLabel("Días para recordatorio:"));
        spinnerDiasRecordatorio = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        estilizarSpinner(spinnerDiasRecordatorio);
        panel.add(spinnerDiasRecordatorio);

        return panel;
    }

    private JPanel crearPanelWhatsApp() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);

        chkWhatsAppHabilitado = new JCheckBox("Habilitar envío");
        chkWhatsAppHabilitado.setFont(FONT_NORMAL);
        chkWhatsAppHabilitado.setForeground(COLOR_TEXTO);
        chkWhatsAppHabilitado.setOpaque(false);
        panel.add(chkWhatsAppHabilitado);
        panel.add(new JLabel()); // Espaciador

        panel.add(crearLabel("API Key:"));
        txtApiKeyCallMeBot = crearCampoTexto();
        panel.add(txtApiKeyCallMeBot);

        return panel;
    }

    private JPanel crearPanelRouter() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 8));
        panel.setOpaque(false);

        chkRouterHabilitado = new JCheckBox("Habilitar corte automático");
        chkRouterHabilitado.setFont(FONT_NORMAL);
        chkRouterHabilitado.setForeground(COLOR_TEXTO);
        chkRouterHabilitado.setOpaque(false);
        panel.add(chkRouterHabilitado);
        panel.add(new JLabel());

        panel.add(crearLabel("IP del Router:"));
        txtMikroTikIP = crearCampoTexto();
        panel.add(txtMikroTikIP);

        panel.add(crearLabel("Usuario:"));
        txtMikroTikUsuario = crearCampoTexto();
        panel.add(txtMikroTikUsuario);

        panel.add(crearLabel("Contraseña:"));
        txtMikroTikPassword = new JPasswordField();
        txtMikroTikPassword.setFont(FONT_NORMAL);
        panel.add(txtMikroTikPassword);

        return panel;
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FONT_NORMAL);
        lbl.setForeground(COLOR_TEXTO);
        return lbl;
    }

    private JTextField crearCampoTexto() {
        JTextField txt = new JTextField();
        txt.setFont(FONT_NORMAL);
        return txt;
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setFont(FONT_NORMAL);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(FONT_NORMAL);
        }
    }

    private JButton crearBoton(String texto, boolean primario) {
        JButton btn = new JButton(texto);
        btn.setFont(FONT_NORMAL);
        btn.setForeground(primario ? Color.WHITE : COLOR_TEXTO);
        btn.setBackground(primario ? COLOR_ACENTO : COLOR_PANEL);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void cargarConfiguraciones() {
        spinnerPlazoDias.setValue(configDAO.obtenerValorInt(ConfiguracionDAO.PLAZO_PAGO_DIAS, 21));
        spinnerDiasRecordatorio.setValue(configDAO.obtenerValorInt(ConfiguracionDAO.DIAS_RECORDATORIO, 0));
        chkWhatsAppHabilitado.setSelected(configDAO.obtenerValorBoolean(ConfiguracionDAO.WHATSAPP_HABILITADO));
        chkRouterHabilitado.setSelected(configDAO.obtenerValorBoolean(ConfiguracionDAO.ROUTER_HABILITADO));

        String apiKey = configDAO.obtenerValor(ConfiguracionDAO.CALLMEBOT_APIKEY);
        txtApiKeyCallMeBot.setText(apiKey != null ? apiKey : "");

        String ip = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_IP);
        txtMikroTikIP.setText(ip != null ? ip : "");

        String usuario = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_USUARIO);
        txtMikroTikUsuario.setText(usuario != null ? usuario : "admin");

        String password = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_PASSWORD);
        txtMikroTikPassword.setText(password != null ? password : "");
    }

    private void guardarConfiguraciones() {
        configDAO.guardarValor(ConfiguracionDAO.PLAZO_PAGO_DIAS, spinnerPlazoDias.getValue().toString());
        configDAO.guardarValor(ConfiguracionDAO.DIAS_RECORDATORIO, spinnerDiasRecordatorio.getValue().toString());
        configDAO.guardarValor(ConfiguracionDAO.WHATSAPP_HABILITADO, chkWhatsAppHabilitado.isSelected() ? "1" : "0");
        configDAO.guardarValor(ConfiguracionDAO.ROUTER_HABILITADO, chkRouterHabilitado.isSelected() ? "1" : "0");
        configDAO.guardarValor(ConfiguracionDAO.CALLMEBOT_APIKEY, txtApiKeyCallMeBot.getText());
        configDAO.guardarValor(ConfiguracionDAO.MIKROTIK_IP, txtMikroTikIP.getText());
        configDAO.guardarValor(ConfiguracionDAO.MIKROTIK_USUARIO, txtMikroTikUsuario.getText());
        configDAO.guardarValor(ConfiguracionDAO.MIKROTIK_PASSWORD, new String(txtMikroTikPassword.getPassword()));

        JOptionPane.showMessageDialog(this,
                "Configuracion guardada correctamente.",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }
}
