package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
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
    
    // --- VARIABLES PARA EL AUTOCOMPLETADO ---
    private JPopupMenu menuSugerencias;
    private JList<String> listaSugerencias;
    private DefaultListModel<String> modeloSugerencias;
    private List<String[]> clientesCache; // [0]=DNI, [1]=Nombre
    // ----------------------------------------

    private double totalSeleccionado = 0.0;
    private String metodoPagoSeleccionado = "EFECTIVO";
    private JButton btnEfectivo, btnYape, btnTransf;

    public subpanel_Caja() {
        setBackground(Color.WHITE);
        setLayout(null);
        
        // Inicializar caché vacía
        clientesCache = new ArrayList<>();
        
        initContenido();
        initAutocompletado(); // Configurar el buscador inteligente

        // PRECARGA DE DATOS (En segundo plano para velocidad extrema)
        precargarClientes();
    }

    private void precargarClientes() {
        new Thread(() -> {
            DAO.ClienteDAO dao = new DAO.ClienteDAO();
            clientesCache = dao.obtenerListaSimpleClientes();
            // System.out.println("Clientes cargados en caché: " + clientesCache.size());
        }).start();
    }

    private void initAutocompletado() {
        menuSugerencias = new JPopupMenu();
        menuSugerencias.setFocusable(false); // Para no robar el foco al escribir
        
        modeloSugerencias = new DefaultListModel<>();
        listaSugerencias = new JList<>(modeloSugerencias);
        listaSugerencias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Evento al hacer clic en una sugerencia
        listaSugerencias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                escogerSugerencia();
            }
        });

        JScrollPane scrollSugerencias = new JScrollPane(listaSugerencias);
        scrollSugerencias.setBorder(null);
        menuSugerencias.add(scrollSugerencias);

        // Evento al escribir en el buscador
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Si presionan enter, buscar directo
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    menuSugerencias.setVisible(false);
                    buscarDeudas();
                    return;
                }
                // Si presionan flecha abajo, pasar el foco a la lista
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (menuSugerencias.isVisible()) {
                        listaSugerencias.requestFocusInWindow();
                        listaSugerencias.setSelectedIndex(0);
                    }
                    return;
                }
                filtrarSugerencias();
            }
        });
        
        // Soporte para navegar con teclado en la lista
        listaSugerencias.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    escogerSugerencia();
                }
            }
        });
    }

    private void filtrarSugerencias() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        modeloSugerencias.clear();

        if (texto.isEmpty()) {
            menuSugerencias.setVisible(false);
            return;
        }

        // Búsqueda en memoria (Rapidísima)
        for (String[] cliente : clientesCache) {
            String dni = cliente[0];
            String nombre = cliente[1].toLowerCase();
            
            // Coincidencia por DNI o Nombre
            if (dni.startsWith(texto) || nombre.contains(texto)) {
                // Formato visual: "10203040 - Juan Perez"
                modeloSugerencias.addElement(dni + " - " + cliente[1]); 
            }
        }

        if (!modeloSugerencias.isEmpty()) {
            // Mostrar menú debajo del txtBuscar
            menuSugerencias.setPopupSize(txtBuscar.getWidth(), 150); // Alto fijo
            menuSugerencias.show(txtBuscar, 0, txtBuscar.getHeight());
            txtBuscar.requestFocus(); // Mantener foco en el texto
        } else {
            menuSugerencias.setVisible(false);
        }
    }

    private void escogerSugerencia() {
        String seleccionado = listaSugerencias.getSelectedValue();
        if (seleccionado != null) {
            // Extraer solo el DNI (la parte antes del guion) para buscar exacto
            String dni = seleccionado.split(" - ")[0];
            txtBuscar.setText(dni);
            menuSugerencias.setVisible(false);
            buscarDeudas(); // Buscar automáticamente al hacer clic
        }
    }

    private void initContenido() {
        JLabel lblTitulo = new JLabel("Caja - Registrar Pago");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setBounds(30, 20, 250, 30);
        add(lblTitulo);

        // BUSCADOR MEJORADO
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Escribe nombre o DNI...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setBounds(30, 60, 300, 40);
        add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        estilarBotonSimple(btnBuscar);
        btnBuscar.setBounds(340, 60, 50, 40);
        btnBuscar.addActionListener(e -> buscarDeudas());
        add(btnBuscar);

        // Tarjeta Info Cliente
        JPanel panelInfo = new JPanel(null);
        panelInfo.setBackground(new Color(248, 250, 252));
        panelInfo.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        panelInfo.setBounds(30, 120, 410, 80);

        JLabel lblCliente = new JLabel("Cliente: ---"); // Globalizar si quieres actualizarlo
        lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCliente.setBounds(15, 15, 300, 20);
        panelInfo.add(lblCliente);

        JLabel lblPlan = new JLabel("Plan: ---");
        lblPlan.setForeground(Color.GRAY);
        lblPlan.setBounds(15, 40, 300, 20);
        panelInfo.add(lblPlan);
        add(panelInfo);

        // Tabla
        JLabel lblDetalle = new JLabel("Seleccione meses a pagar:");
        lblDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetalle.setBounds(30, 220, 200, 20);
        add(lblDetalle);

        String[] cols = {"Pagar", "Mes / Concepto", "Vence", "Monto", "ID_Oculto"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 0; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };

        modeloTabla.addTableModelListener(e -> calcularTotalSeleccionado());

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setRowHeight(30);
        tablaFacturas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaFacturas.getColumnModel().getColumn(1).setPreferredWidth(150);
        // Ocultar ID
        tablaFacturas.removeColumn(tablaFacturas.getColumnModel().getColumn(4));

        JScrollPane scroll = new JScrollPane(tablaFacturas);
        scroll.setBounds(30, 250, 410, 300);
        add(scroll);

        // --- PANEL DERECHO (Cobro) ---
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

        // Métodos
        JLabel lblMetodo = new JLabel("Método de Pago:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMetodo.setBounds(50, 170, 200, 20);
        panelPago.add(lblMetodo);

        btnEfectivo = crearBotonMetodo("💵 Efectivo", 50, 200);
        btnYape = crearBotonMetodo("📱 Yape/Plin", 210, 200);
        btnTransf = crearBotonMetodo("🏦 Banco", 370, 200);

        btnEfectivo.addActionListener(e -> seleccionarMetodo("EFECTIVO"));
        btnYape.addActionListener(e -> seleccionarMetodo("YAPE"));
        btnTransf.addActionListener(e -> seleccionarMetodo("TRANSFERENCIA"));

        panelPago.add(btnEfectivo); panelPago.add(btnYape); panelPago.add(btnTransf);
        seleccionarMetodo("EFECTIVO");

        // Inputs
        JLabel lblRecibido = new JLabel("Monto Recibido:");
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

    // --- LÓGICA CON DATOS REALES ---

    private void buscarDeudas() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) return;

        // ACTIVAR BARRA DE CARGA
        if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);

        modeloTabla.setRowCount(0); // Limpiar

        new Thread(() -> {
            DAO.PagoDAO dao = new DAO.PagoDAO();
            java.util.List<Object[]> deudas = dao.buscarDeudasPorCliente(texto);

            SwingUtilities.invokeLater(() -> {
                if (deudas.isEmpty()) {
                    // Si no hay deudas, no mostramos mensaje de error si vino del autocompletado
                    // para no ser molestos, pero limpiamos todo.
                    calcularTotalSeleccionado();
                } else {
                    for (Object[] d : deudas) {
                        boolean check = false; 
                        String mes = d[3].toString(); 
                        String vence = d[5].toString();
                        String monto = String.format("%.2f", (Double) d[4]);
                        int idFactura = (int) d[0];
                        modeloTabla.addRow(new Object[]{check, mes, vence, monto, idFactura});
                    }
                    calcularTotalSeleccionado();
                }
                if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
            });
        }).start();
    }

    private void calcularTotalSeleccionado() {
        double total = 0.0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            boolean seleccionado = (boolean) modeloTabla.getValueAt(i, 0);
            if (seleccionado) {
                String montoStr = (String) modeloTabla.getValueAt(i, 3);
                total += Double.parseDouble(montoStr.replace(",", "."));
            }
        }
        this.totalSeleccionado = total;
        lblTotalMonto.setText("S/. " + String.format("%.2f", totalSeleccionado));

        if (txtRecibido.getText().isEmpty()) {
            txtRecibido.setText(String.format("%.2f", totalSeleccionado));
        }
        calcularVueltoOParcial();
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
                txtVuelto.setText("S/. " + String.format("%.2f", diferencia));
                txtVuelto.setForeground(new Color(22, 163, 74));
                lblEstadoPago.setText("✅ PAGO COMPLETO (Entregar Vuelto)");
                lblEstadoPago.setForeground(new Color(22, 163, 74));
            } else {
                txtVuelto.setText("Falta: S/. " + String.format("%.2f", Math.abs(diferencia)));
                txtVuelto.setForeground(new Color(220, 38, 38));
                lblEstadoPago.setText("⚠️ PAGO PARCIAL (Queda Deuda)");
                lblEstadoPago.setForeground(new Color(234, 88, 12));
            }
        } catch (NumberFormatException e) {
            txtVuelto.setText("---");
            lblEstadoPago.setText("");
        }
    }

    private void accionPagar() {
        if (totalSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos una factura.");
            return;
        }
        
        // Validación básica de monto
        double recibido = 0;
        try {
             recibido = Double.parseDouble(txtRecibido.getText().replace(",", "."));
        } catch(Exception e) { return; }

        if (recibido < totalSeleccionado) {
             JOptionPane.showMessageDialog(this, "Monto insuficiente.");
             return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Confirmar cobro de S/. " + String.format("%.2f", totalSeleccionado) + "?", 
            "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (Principal.instancia != null) Principal.instancia.mostrarCarga(true);
            
            new Thread(() -> {
                DAO.PagoDAO dao = new DAO.PagoDAO();
                boolean error = false;
                
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    if ((boolean) modeloTabla.getValueAt(i, 0)) {
                        int idFactura = (int) modeloTabla.getValueAt(i, 4);
                        double monto = Double.parseDouble(modeloTabla.getValueAt(i, 3).toString().replace(",", "."));
                        if (!dao.realizarCobro(idFactura, monto, 1)) error = true;
                    }
                }
                
                boolean finalError = error;
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) Principal.instancia.mostrarCarga(false);
                    if (!finalError) {
                        JOptionPane.showMessageDialog(this, "Pago registrado con éxito.");
                        buscarDeudas(); // Refrescar
                        txtRecibido.setText("");
                        txtVuelto.setText("---");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al registrar algunos pagos.");
                    }
                });
            }).start();
        }
    }

    // --- ESTILOS ---
    private void seleccionarMetodo(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        estilarBotonPago(btnEfectivo, metodo.equals("EFECTIVO"));
        estilarBotonPago(btnYape, metodo.equals("YAPE"));
        estilarBotonPago(btnTransf, metodo.equals("TRANSFERENCIA"));
    }

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