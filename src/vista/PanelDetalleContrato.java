package vista;

import modelo.Suscripcion;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.MatteBorder;

public class PanelDetalleContrato extends JPanel {

    private JLabel lblCliente, lblPlan, lblEstado, lblDireccion, lblSector, lblDeudaTotal, lblProximoPago;
    private JLabel lblContratoID, lblMorosidad;
    private JLabel lblInicio, lblGarantia;

    // --- NUEVOS LABELS ---
    private JLabel lblMesAdelantado, lblEquipos;
    // ---------------------

    // Variables para el botón de historial
    private int idSuscripcionActual = 0;
    private String nombreClienteActual = "";
    private double montoMensualActual = 0.0; // NUEVO
    private int diaPagoActual = 15; // NUEVO

    public PanelDetalleContrato() {
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 1, 0, 0, new Color(226, 232, 240)));

        // Aumentamos altura para que quepa todo sin scroll forzado
        setPreferredSize(new Dimension(350, 900));

        initUI();
    }

    private void initUI() {
        // Título
        JLabel lblTitulo = new JLabel("Ficha del Abonado");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(25, 25, 250, 30);
        add(lblTitulo);

        lblContratoID = new JLabel("# ---");
        lblContratoID.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblContratoID.setForeground(new Color(100, 116, 139));
        lblContratoID.setBounds(25, 55, 200, 20);
        add(lblContratoID);

        // Tarjeta Estado
        JPanel pnlEstado = new JPanel(null);
        pnlEstado.setBounds(25, 90, 300, 70);
        pnlEstado.setBackground(new Color(248, 250, 252));
        pnlEstado.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        add(pnlEstado);

        lblEstado = new JLabel("---");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setBounds(10, 10, 280, 25);
        lblEstado.setOpaque(true);
        pnlEstado.add(lblEstado);

        lblMorosidad = new JLabel("Seleccione un cliente");
        lblMorosidad.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblMorosidad.setHorizontalAlignment(SwingConstants.CENTER);
        lblMorosidad.setForeground(Color.GRAY);
        lblMorosidad.setBounds(10, 40, 280, 20);
        pnlEstado.add(lblMorosidad);

        // --- DATOS DEL CLIENTE ---
        int y = 180;
        int gap = 55; // Reduje un poco el espacio para que quepa más info

        add(crearLabelTitulo("Titular:", y));
        lblCliente = crearLabelValor("---", y + 20);
        add(lblCliente);

        y += gap;
        add(crearLabelTitulo("Plan & Precio:", y));
        lblPlan = crearLabelValor("---", y + 20);
        add(lblPlan);

        y += gap;
        add(crearLabelTitulo("Dirección / Sector:", y));
        lblDireccion = crearLabelValor("---", y + 20);
        add(lblDireccion);

        y += gap;
        add(crearLabelTitulo("Fecha Inicio:", y));
        lblInicio = crearLabelValor("---", y + 20);
        add(lblInicio);

        // --- CONDICIONES CONTRACTUALES (SECCIÓN NUEVA) ---
        y += gap + 10;
        JLabel lblSub = new JLabel("Condiciones del Servicio");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSub.setForeground(new Color(37, 99, 235)); // Azul
        lblSub.setBounds(25, y, 200, 20);
        add(lblSub);
        y += 25;

        // 1. Equipos
        add(crearLabelTitulo("Equipos (Router/ONU):", y));
        lblEquipos = crearLabelValor("---", y + 20);
        add(lblEquipos);

        // 2. Modalidad de Pago
        y += gap;
        add(crearLabelTitulo("Modalidad de Cobro:", y));
        lblMesAdelantado = crearLabelValor("---", y + 20);
        add(lblMesAdelantado);

        // 3. Garantía
        y += gap;
        add(crearLabelTitulo("Garantía en Depósito:", y));
        lblGarantia = crearLabelValor("S/. 0.00", y + 20);
        lblGarantia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(lblGarantia);

        // SEPARADOR
        y += gap + 10;
        JSeparator sep = new JSeparator();
        sep.setBounds(25, y, 300, 10);
        add(sep);
        y += 15;

        // --- SECCIÓN FINANCIERA ---
        add(crearLabelTitulo("Ciclo de Facturación:", y));
        lblProximoPago = crearLabelValor("---", y + 20);
        add(lblProximoPago);

        y += gap;
        add(crearLabelTitulo("Deuda Total Acumulada:", y));
        lblDeudaTotal = crearLabelValor("S/. 0.00", y + 20);
        lblDeudaTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblDeudaTotal);

        // BOTÓN HISTORIAL
        JButton btnVerHistorial = new JButton("Ver Historial Completo");
        btnVerHistorial.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerHistorial.setBackground(new Color(241, 245, 249));
        btnVerHistorial.setForeground(new Color(15, 23, 42));
        btnVerHistorial.setFocusPainted(false);

        y += 70; // Posición final del botón
        btnVerHistorial.setBounds(25, y, 200, 40);

        btnVerHistorial.addActionListener(e -> {
            if (idSuscripcionActual > 0) {
                java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
                if (parent instanceof java.awt.Frame) {
                    // Pasar monto y día de pago para funcionalidad completa
                    new DialogoHistorial((java.awt.Frame) parent, idSuscripcionActual,
                            nombreClienteActual, montoMensualActual, diaPagoActual).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un cliente primero.");
            }
        });

        add(btnVerHistorial);
    }

    public void mostrarDatos(Suscripcion s) {
        this.idSuscripcionActual = s.getIdSuscripcion();
        this.nombreClienteActual = s.getNombreCliente();
        this.montoMensualActual = s.getMontoMensual(); // Guardar monto
        this.diaPagoActual = s.getDiaPago(); // Guardar día de pago

        double precio = s.getMontoMensual();
        String nombre = s.getNombreCliente() != null ? s.getNombreCliente() : "SIN NOMBRE";
        String plan = s.getNombreServicio() != null ? s.getNombreServicio() : "SIN PLAN";
        String direccion = s.getDireccionInstalacion() != null ? s.getDireccionInstalacion() : "Sin dirección";
        String codigo = s.getCodigoContrato() != null ? s.getCodigoContrato() : "---";

        lblCliente.setText(nombre);
        lblPlan.setText("<html>" + plan + " <span style='color:gray'>(S/. " + String.format("%.2f", precio)
                + ")</span></html>");
        lblDireccion.setText("<html>" + direccion + "</html>");
        lblContratoID.setText("CONTRATO: " + codigo);
        // Formato de fecha: dd/MM/yyyy
        if (s.getFechaInicio() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            lblInicio.setText(sdf.format(s.getFechaInicio()));
        } else {
            lblInicio.setText("---");
        }
        lblProximoPago.setText("Los dias " + s.getDiaPago() + " de cada mes");

        // --- 1. EQUIPOS PRESTADOS ---
        if (s.isEquiposPrestados()) { // Asegúrate que tu modelo tenga isEquiposPrestados() o getEquiposPrestados() ==
                                      // 1
            lblEquipos.setText("PRESTADOS (Propiedad de la Empresa)");
            lblEquipos.setForeground(new Color(234, 88, 12)); // Naranja (Atención al retirar)
        } else {
            lblEquipos.setText("PROPIOS (Cliente compró equipos)");
            lblEquipos.setForeground(new Color(22, 163, 74)); // Verde
        }

        // --- 2. MES ADELANTADO ---
        if (s.isMesAdelantado()) { // Asegúrate que tu modelo tenga isMesAdelantado()
            lblMesAdelantado.setText("MES ADELANTADO (Prepago)");
            lblMesAdelantado.setForeground(new Color(37, 99, 235)); // Azul
        } else {
            lblMesAdelantado.setText("MES VENCIDO (Postpago)");
            lblMesAdelantado.setForeground(Color.BLACK);
        }

        // --- 3. GARANTÍA ---
        double garantia = s.getGarantia();
        if (garantia > 0) {
            lblGarantia.setText("S/. " + String.format("%.2f", garantia));
            lblGarantia.setForeground(new Color(22, 163, 74));
        } else {
            lblGarantia.setText("S/. 0.00");
            lblGarantia.setForeground(Color.GRAY);
        }

        // --- ESTADO Y DEUDA ---
        int pendientes = s.getFacturasPendientes();
        double deudaTotal = precio * pendientes;

        if (s.getActivo() == 1) {
            lblEstado.setText("SERVICIO ACTIVO");
            lblEstado.setBackground(new Color(220, 252, 231));
            lblEstado.setForeground(new Color(22, 163, 74));

            if (pendientes == 0) {
                lblMorosidad.setText("Cliente al dia - OK");
                lblMorosidad.setForeground(new Color(22, 163, 74));
            } else {
                lblMorosidad.setText("Pendiente: " + pendientes + " mes(es)");
                lblMorosidad.setForeground(new Color(234, 179, 8));
            }
        } else {
            lblEstado.setText("CORTADO");
            lblEstado.setBackground(new Color(254, 226, 226));
            lblEstado.setForeground(new Color(220, 38, 38));
            lblMorosidad.setText("Suspendido por Deuda");
            lblMorosidad.setForeground(Color.RED);
        }

        lblDeudaTotal.setText("S/. " + String.format("%.2f", deudaTotal));
        lblDeudaTotal.setForeground(deudaTotal > 0 ? new Color(220, 38, 38) : new Color(15, 23, 42));
    }

    private JLabel crearLabelTitulo(String texto, int y) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(148, 163, 184));
        l.setBounds(25, y, 200, 15);
        return l;
    }

    private JLabel crearLabelValor(String texto, int y) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Letra un pelín más pequeña para que entre texto largo
        l.setForeground(new Color(15, 23, 42));
        l.setBounds(25, y, 300, 20);
        return l;
    }
}