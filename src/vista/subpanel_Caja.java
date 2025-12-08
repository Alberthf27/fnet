package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class subpanel_Caja extends JPanel {

    // Componentes globales
    private JLabel lblTotalMonto, txtVuelto, lblEstadoPago;
    private JTextField txtRecibido, txtBuscar;
    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    
    // Variables de Estado
    private double totalSeleccionado = 0.0;
    private String metodoPagoSeleccionado = "EFECTIVO"; // Por defecto
    
    // Botones de método (para cambiarles el color)
    private JButton btnEfectivo, btnYape, btnTransf;

    public subpanel_Caja() {
        setBackground(Color.WHITE);
        setLayout(null);
        initContenido();
        
        // Simular carga de un cliente al iniciar (para que veas el efecto)
        cargarDatosSimulados(); 
    }

    private void initContenido() {
        // --- COLUMNA IZQUIERDA: BÚSQUEDA Y DEUDAS ---
        JLabel lblTitulo = new JLabel("Caja - Registrar Pago");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setBounds(30, 20, 250, 30);
        add(lblTitulo);

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "DNI o Apellido...");
        txtBuscar.setBounds(30, 60, 300, 40);
        add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        estilarBotonSimple(btnBuscar);
        btnBuscar.setBounds(340, 60, 50, 40);
        // Acción de buscar (Simulada)
        btnBuscar.addActionListener(e -> cargarDatosSimulados());
        add(btnBuscar);

        // Tarjeta Info Cliente
        JPanel panelInfo = new JPanel(null);
        panelInfo.setBackground(new Color(248, 250, 252));
        panelInfo.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelInfo.setBounds(30, 120, 410, 80);
        
        JLabel lblCliente = new JLabel("Cliente: Juan Perez");
        lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCliente.setBounds(15, 15, 300, 20);
        panelInfo.add(lblCliente);
        
        JLabel lblPlan = new JLabel("Plan: Fibra 50MB (S/. 50.00)");
        lblPlan.setForeground(Color.GRAY);
        lblPlan.setBounds(15, 40, 300, 20);
        panelInfo.add(lblPlan);
        add(panelInfo);

        // Tabla de Facturas (Selección Múltiple)
        JLabel lblDetalle = new JLabel("Seleccione meses a pagar:");
        lblDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetalle.setBounds(30, 220, 200, 20);
        add(lblDetalle);

        // Columnas: Checkbox, Mes, Vencimiento, Monto
        String[] cols = {"Pagar", "Mes / Concepto", "Vence", "Monto"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            // Solo la columna 0 (Checkbox) es editable
            public boolean isCellEditable(int row, int col) { return col == 0; }
            // Definir tipos para que la col 0 se dibuje como Checkbox
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        
        // Listener: Cada vez que marcas un check, recalcula el total
        modeloTabla.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                calcularTotalSeleccionado();
            }
        });

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setRowHeight(30);
        tablaFacturas.getColumnModel().getColumn(0).setPreferredWidth(50); // Checkbox pequeño
        tablaFacturas.getColumnModel().getColumn(1).setPreferredWidth(150);
        
        JScrollPane scroll = new JScrollPane(tablaFacturas);
        scroll.setBounds(30, 250, 410, 300);
        add(scroll);

        // --- COLUMNA DERECHA: PROCESO DE PAGO ---
        JPanel panelPago = new JPanel(null);
        panelPago.setBackground(new Color(241, 245, 249)); 
        panelPago.setBounds(480, 0, 730, 760);
        add(panelPago);

        JLabel lblTotalT = new JLabel("Total a Pagar");
        lblTotalT.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTotalT.setBounds(50, 50, 200, 30);
        panelPago.add(lblTotalT);

        lblTotalMonto = new JLabel("S/. 0.00");
        lblTotalMonto.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblTotalMonto.setForeground(new Color(37, 99, 235));
        lblTotalMonto.setBounds(50, 80, 300, 60);
        panelPago.add(lblTotalMonto);

        // Selector de Método
        JLabel lblMetodo = new JLabel("Método de Pago:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMetodo.setBounds(50, 170, 200, 20);
        panelPago.add(lblMetodo);

        btnEfectivo = crearBotonMetodo("💵 Efectivo", 50, 200);
        btnYape = crearBotonMetodo("📱 Yape/Plin", 210, 200);
        btnTransf = crearBotonMetodo("🏦 Banco", 370, 200);
        
        // Acciones de botones
        btnEfectivo.addActionListener(e -> seleccionarMetodo("EFECTIVO"));
        btnYape.addActionListener(e -> seleccionarMetodo("YAPE"));
        btnTransf.addActionListener(e -> seleccionarMetodo("TRANSFERENCIA"));
        
        panelPago.add(btnEfectivo); panelPago.add(btnYape); panelPago.add(btnTransf);
        seleccionarMetodo("EFECTIVO"); // Default

        // Inputs Dinero
        JLabel lblRecibido = new JLabel("Monto Recibido / A Cuenta:");
        lblRecibido.setBounds(50, 280, 200, 20);
        panelPago.add(lblRecibido);

        txtRecibido = new JTextField();
        txtRecibido.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtRecibido.setHorizontalAlignment(SwingConstants.RIGHT);
        txtRecibido.setBounds(50, 310, 200, 50);
        txtRecibido.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { calcularVueltoOParcial(); }
        });
        panelPago.add(txtRecibido);

        JLabel lblVueltoT = new JLabel("Vuelto / Restante:");
        lblVueltoT.setBounds(280, 280, 200, 20);
        panelPago.add(lblVueltoT);

        txtVuelto = new JLabel("S/. 0.00");
        txtVuelto.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtVuelto.setForeground(Color.GRAY);
        txtVuelto.setBounds(280, 310, 200, 50);
        panelPago.add(txtVuelto);
        
        // Estado del Pago (Visual)
        lblEstadoPago = new JLabel("");
        lblEstadoPago.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstadoPago.setBounds(50, 370, 300, 20);
        panelPago.add(lblEstadoPago);

        JButton btnConfirmar = new JButton("CONFIRMAR PAGO");
        btnConfirmar.setBackground(new Color(22, 163, 74));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBounds(50, 450, 470, 60);
        btnConfirmar.addActionListener(e -> accionPagar());
        panelPago.add(btnConfirmar);
    }

    // --- LÓGICA DE NEGOCIO ---

    private void cargarDatosSimulados() {
        // Limpiamos tabla
        modeloTabla.setRowCount(0);
        
        // Simulamos 3 deudas. 
        // true/false en la col 0 indica si está seleccionado por defecto
        modeloTabla.addRow(new Object[]{true,  "Febrero 2025", "05/02/25", "50.00"}); // El más antiguo marcado
        modeloTabla.addRow(new Object[]{false, "Marzo 2025",   "05/03/25", "50.00"});
        modeloTabla.addRow(new Object[]{false, "Abril 2025",   "05/04/25", "50.00"});
        
        calcularTotalSeleccionado(); // Actualizar etiqueta inicial
    }

    private void calcularTotalSeleccionado() {
        double total = 0.0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            boolean seleccionado = (boolean) modeloTabla.getValueAt(i, 0);
            if (seleccionado) {
                String montoStr = (String) modeloTabla.getValueAt(i, 3); // "50.00"
                total += Double.parseDouble(montoStr);
            }
        }
        this.totalSeleccionado = total;
        lblTotalMonto.setText("S/. " + String.format("%.2f", totalSeleccionado));
        
        // Si no han escrito nada en recibido, sugerimos el total
        if(txtRecibido.getText().isEmpty()) {
            txtRecibido.setText(String.format("%.2f", totalSeleccionado));
        }
        calcularVueltoOParcial(); // Recalcular vuelto con el nuevo total
    }

    private void calcularVueltoOParcial() {
        try {
            double recibido = Double.parseDouble(txtRecibido.getText().replace(",", "."));
            double diferencia = recibido - totalSeleccionado;

            if (totalSeleccionado == 0) {
                txtVuelto.setText("---");
                lblEstadoPago.setText("");
                return;
            }

            if (diferencia >= 0) {
                // PAGO COMPLETO
                txtVuelto.setText("S/. " + String.format("%.2f", diferencia));
                txtVuelto.setForeground(new Color(22, 163, 74)); // Verde
                lblEstadoPago.setText("✅ PAGO COMPLETO (Entregar Vuelto)");
                lblEstadoPago.setForeground(new Color(22, 163, 74));
            } else {
                // PAGO PARCIAL
                txtVuelto.setText("Falta: S/. " + String.format("%.2f", Math.abs(diferencia)));
                txtVuelto.setForeground(new Color(220, 38, 38)); // Rojo
                lblEstadoPago.setText("⚠️ PAGO PARCIAL (Queda Deuda)");
                lblEstadoPago.setForeground(new Color(234, 88, 12)); // Naranja
            }
        } catch (NumberFormatException e) {
            txtVuelto.setText("---");
            lblEstadoPago.setText("");
        }
    }

    private void seleccionarMetodo(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        // Reset estilos
        estilarBotonPago(btnEfectivo, false);
        estilarBotonPago(btnYape, false);
        estilarBotonPago(btnTransf, false);
        
        // Activar el seleccionado
        if (metodo.equals("EFECTIVO")) estilarBotonPago(btnEfectivo, true);
        if (metodo.equals("YAPE")) estilarBotonPago(btnYape, true);
        if (metodo.equals("TRANSFERENCIA")) estilarBotonPago(btnTransf, true);
    }
    
    private void accionPagar() {
        // Aquí iría la lógica real de guardar en BD
        String mensaje = "Registrando Pago:\n" +
                         "- Método: " + metodoPagoSeleccionado + "\n" +
                         "- Monto: S/." + txtRecibido.getText() + "\n" +
                         "- Estado: " + lblEstadoPago.getText();
        JOptionPane.showMessageDialog(this, mensaje);
    }

    // --- ESTILOS VISUALES ---
    private JButton crearBotonMetodo(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 150, 50);
        estilarBotonPago(btn, false);
        return btn;
    }

    private void estilarBotonPago(JButton btn, boolean activo) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        if (activo) {
            btn.setBackground(new Color(37, 99, 235));
            btn.setForeground(Color.WHITE);
            btn.setBorder(null);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(15, 23, 42));
            btn.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        }
    }
    
    private void estilarBotonSimple(JButton btn) {
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
    }
}