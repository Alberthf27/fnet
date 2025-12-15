package vista;

import modelo.Suscripcion;
import java.awt.Color;
import java.awt.Dimension; // Importante para el scroll
import java.awt.Font;
import javax.swing.*;
import javax.swing.border.MatteBorder;

public class PanelDetalleContrato extends JPanel {

    private JLabel lblCliente, lblPlan, lblEstado, lblDireccion, lblSector, lblDeudaTotal, lblProximoPago;
    private JLabel lblContratoID, lblMorosidad;
    private JLabel lblInicio, lblGarantia; // <--- AÑADE lblGarantia

    // Variables para el botón de historial
    private int idSuscripcionActual = 0;
    private String nombreClienteActual = "";

    public PanelDetalleContrato() {
        setLayout(null);
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 1, 0, 0, new Color(226, 232, 240)));

        // Asegurar que el panel sea lo suficientemente alto para mostrar todo
        setPreferredSize(new Dimension(350, 800));

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

        // Datos
        int y = 180;
        int gap = 60;

        add(crearLabelTitulo("Titular:", y));
        lblCliente = crearLabelValor("---", y + 20);
        add(lblCliente);

        y += gap;
        add(crearLabelTitulo("Plan & Precio:", y));
        lblPlan = crearLabelValor("---", y + 20);
        add(lblPlan);

        y += gap;
        add(crearLabelTitulo("Sector / Zona:", y));
        lblSector = crearLabelValor("---", y + 20);
        add(lblSector);

        y += gap;
        add(crearLabelTitulo("Dirección:", y));
        lblDireccion = crearLabelValor("---", y + 20);
        add(lblDireccion);

        y += gap;
        add(crearLabelTitulo("Fecha Inicio:", y));
        lblInicio = crearLabelValor("---", y + 20);
        add(lblInicio);

        // --- INICIO NUEVO CÓDIGO GARANTÍA ---
        y += gap; // Bajamos un escalón más
        add(crearLabelTitulo("Garantía en Resguardo:", y));
        lblGarantia = crearLabelValor("S/. 0.00", y + 20);
        lblGarantia.setForeground(new Color(22, 163, 74)); // Verde bonito
        lblGarantia.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Un poco más negrita para resaltar
        add(lblGarantia);
        // --- FIN NUEVO CÓDIGO GARANTÍA ---

        // Sección Financiera (Nota: Como aumentamos 'y', el separador bajará automáticamente)
        y += gap + 10;
        JSeparator sep = new JSeparator();
        sep.setBounds(25, y, 300, 10);
        add(sep);
        // ... el resto sigue igual
        sep.setBounds(25, y, 300, 10);
        add(sep);
        y += 15;

        add(crearLabelTitulo("Ciclo de Facturación:", y));
        lblProximoPago = crearLabelValor("---", y + 20);
        add(lblProximoPago);

        y += gap;
        add(crearLabelTitulo("Deuda Total:", y));
        lblDeudaTotal = crearLabelValor("S/. 0.00", y + 20);
        lblDeudaTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblDeudaTotal);

        // BOTÓN HISTORIAL (MOVIDO MÁS ABAJO)
        JButton btnVerHistorial = new JButton("Ver Historial Completo");
        btnVerHistorial.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerHistorial.setBackground(new Color(241, 245, 249));
        btnVerHistorial.setForeground(new Color(15, 23, 42));
        btnVerHistorial.setFocusPainted(false);
        // Lo bajamos a Y=630 para que no tape la deuda
        btnVerHistorial.setBounds(25, 700, 200, 40);

        btnVerHistorial.addActionListener(e -> {
            if (idSuscripcionActual > 0) {
                java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
                // Verificación de seguridad por si el parent es null (raro pero posible)
                if (parent instanceof java.awt.Frame) {
                    new DialogoHistorial((java.awt.Frame) parent, idSuscripcionActual, nombreClienteActual).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al abrir historial: Ventana padre no encontrada.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un cliente primero.");
            }
        });

        add(btnVerHistorial);
    }

    public void mostrarDatos(Suscripcion s) {
        // Guardar datos para el botón de historial
        this.idSuscripcionActual = s.getIdSuscripcion();
        this.nombreClienteActual = s.getNombreCliente();

        double precio = s.getMontoMensual();

        String nombre = s.getNombreCliente() != null ? s.getNombreCliente() : "SIN NOMBRE";
        String plan = s.getNombreServicio() != null ? s.getNombreServicio() : "SIN PLAN";
        String direccion = s.getDireccionInstalacion() != null ? s.getDireccionInstalacion() : "Sin dirección";
        String codigo = s.getCodigoContrato() != null ? s.getCodigoContrato() : "---";
        String sector = s.getSector() != null ? s.getSector() : "General";

        lblCliente.setText(nombre);
        lblPlan.setText("<html>" + plan + " <span style='color:gray'>(S/. " + String.format("%.2f", precio) + ")</span></html>");
        lblDireccion.setText("<html>" + direccion + "</html>");
        lblContratoID.setText("CONTRATO: " + codigo);
        lblSector.setText(sector);

        lblInicio.setText(s.getFechaInicio() != null ? s.getFechaInicio().toString() : "---");
        lblProximoPago.setText("Los días " + s.getDiaPago() + " de cada mes");

        // --- AÑADIR ESTO ---
        double garantia = s.getGarantia(); // Asumiendo que ya creaste el getter en tu modelo Suscripcion
        if (garantia > 0) {
            lblGarantia.setText("S/. " + String.format("%.2f", garantia));
            lblGarantia.setForeground(new Color(22, 163, 74)); // Verde (Tiene saldo)
        } else {
            lblGarantia.setText("S/. 0.00 (Sin garantía)");
            lblGarantia.setForeground(Color.GRAY); // Gris (No tiene)
        }
        
        int pendientes = s.getFacturasPendientes();
        double deudaTotal = precio * pendientes;

        // ...
        lblInicio.setText(s.getFechaInicio() != null ? s.getFechaInicio().toString() : "---");
        lblProximoPago.setText("Los días " + s.getDiaPago() + " de cada mes");

        if (s.getActivo() == 1) {
            lblEstado.setText("SERVICIO ACTIVO");
            lblEstado.setBackground(new Color(220, 252, 231));
            lblEstado.setForeground(new Color(22, 163, 74));

            if (pendientes == 0) {
                lblMorosidad.setText("Cliente al día 🌟");
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
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(new Color(15, 23, 42));
        l.setBounds(25, y, 300, 25);
        l.setVerticalAlignment(SwingConstants.TOP);
        return l;
    }
}
