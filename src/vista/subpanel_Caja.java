package vista;

import DAO.PagoDAO;
import DAO.ClienteDAO;
import DAO.SuscripcionDAO;
import modelo.Suscripcion;
import servicio.BoletaPDFService;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_Caja extends JPanel {

    // --- COMPONENTES UI ---
    // Tabla 1: Selección de Cliente (Arriba)
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;

    // Tabla 2: Deudas del Cliente (Abajo)
    private JTable tablaDeudas;
    private DefaultTableModel modeloDeudas;

    private JTextField txtBuscar;

    // Panel Derecho (Cobro)
    private JLabel lblTotalMonto, lblVuelto, lblEstadoPago, lblClienteSeleccionado, lblPlanSeleccionado;
    private JTextField txtRecibido;
    private JButton btnEfectivo, btnYape, btnTransf;

    // --- LÓGICA ---
    private double totalSeleccionado = 0.0;
    private String metodoPagoSeleccionado = "EFECTIVO";

    // Datos del cliente seleccionado actualmente
    private int idSuscripcionActual = -1;
    private String nombreClienteActual = "---";

    // --- NUEVO: Caché para el filtro rápido ---
    private java.util.List<Suscripcion> clientesCache = new java.util.ArrayList<>();

    public subpanel_Caja() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(5, 10, 5, 10));

        initUI();
        // Cargar lista inicial vacía o últimos clientes
        buscarClientes("");
    }

    private void initUI() {
        // =================================================================================
        // 1. SECCIÓN IZQUIERDA (DIVIDIDA EN DOS: LISTA CLIENTES Y LISTA DEUDAS)
        // =================================================================================

        // --- A. PANEL SUPERIOR (BUSCADOR Y BOTONES) ---
        JPanel pnlBusqueda = new JPanel(null);
        pnlBusqueda.setPreferredSize(new Dimension(100, 60));
        pnlBusqueda.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("CAJA Y PAGOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(0, 15, 150, 30);
        pnlBusqueda.add(lblTitulo);

        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "Escribe nombre o DNI...");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setBounds(160, 15, 250, 35);

        // --- LOGICA HÍBRIDA ---
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Enter -> Búsqueda profunda en BD (Trae datos frescos)
                    buscarClientes(txtBuscar.getText());
                } else {
                    // Escribiendo -> Filtro local instantáneo (Usa caché)
                    filtrarLocalmente(txtBuscar.getText());
                }
            }
        });

        pnlBusqueda.add(txtBuscar);

        JButton btnBuscar = new JButton("🔍");
        btnBuscar.setBounds(420, 15, 50, 35);
        estilarBotonSimple(btnBuscar);
        btnBuscar.addActionListener(e -> buscarClientes(txtBuscar.getText()));
        pnlBusqueda.add(btnBuscar);

        JButton btnAdelantar = new JButton("📅 Adelantar Mes");
        btnAdelantar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdelantar.setBackground(new Color(255, 247, 237));
        btnAdelantar.setForeground(new Color(234, 88, 12));
        btnAdelantar.setBorder(new LineBorder(new Color(253, 186, 116), 1));
        btnAdelantar.setFocusPainted(false);
        btnAdelantar.setBounds(490, 15, 130, 35);
        btnAdelantar.addActionListener(e -> adelantarMes());
        pnlBusqueda.add(btnAdelantar);

        // --- B. TABLA DE CLIENTES (RESULTADOS BÚSQUEDA) ---
        String[] colsC = { "ID_S", "CLIENTE", "DNI", "PLAN / SERVICIO", "ESTADO" };
        modeloClientes = new DefaultTableModel(colsC, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloClientes);
        estilarTabla(tablaClientes);
        // Ocultar ID
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scrollClientes = new JScrollPane(tablaClientes);
        scrollClientes.getViewport().setBackground(Color.WHITE);
        scrollClientes.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)), "1. Seleccione Cliente"));

        // Evento: Al hacer clic en un cliente, cargar sus deudas abajo
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                seleccionarCliente();
            }
        });

        // --- C. TABLA DE DEUDAS (DEL CLIENTE SELECCIONADO) ---
        String[] colsD = { "Pagar", "Mes / Concepto", "Vencimiento", "Monto", "ID_F" };
        modeloDeudas = new DefaultTableModel(colsD, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }

            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
        };
        modeloDeudas.addTableModelListener(e -> calcularTotalSeleccionado());

        tablaDeudas = new JTable(modeloDeudas);
        estilarTabla(tablaDeudas);
        tablaDeudas.getColumnModel().getColumn(4).setMinWidth(0);
        tablaDeudas.getColumnModel().getColumn(4).setMaxWidth(0);
        tablaDeudas.getColumnModel().getColumn(0).setPreferredWidth(50);

        JScrollPane scrollDeudas = new JScrollPane(tablaDeudas);
        scrollDeudas.getViewport().setBackground(Color.WHITE);
        scrollDeudas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)), "2. Seleccione Deudas a Pagar"));

        // Unir Búsqueda + Tabla Clientes + Tabla Deudas en el Panel Izquierdo
        JPanel pnlListas = new JPanel(new BorderLayout());
        pnlListas.add(pnlBusqueda, BorderLayout.NORTH);

        // Split vertical entre Clientes (Arriba) y Deudas (Abajo)
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollClientes, scrollDeudas);
        splitVertical.setResizeWeight(0.5); // 50% espacio para cada uno
        splitVertical.setBorder(null);
        splitVertical.setDividerSize(5);
        pnlListas.add(splitVertical, BorderLayout.CENTER);

        // =================================================================================
        // 2. SECCIÓN DERECHA (PANEL DE COBRO FIJO)
        // =================================================================================
        JPanel pnlDerecho = new JPanel(null);
        pnlDerecho.setBackground(new Color(248, 250, 252));
        pnlDerecho.setBorder(new MatteBorder(0, 1, 0, 0, new Color(226, 232, 240)));

        // Info Cliente
        lblClienteSeleccionado = new JLabel("Cliente: ---");
        lblClienteSeleccionado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClienteSeleccionado.setForeground(new Color(15, 23, 42));
        lblClienteSeleccionado.setBounds(20, 20, 360, 20);
        pnlDerecho.add(lblClienteSeleccionado);

        lblPlanSeleccionado = new JLabel("Plan: ---");
        lblPlanSeleccionado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPlanSeleccionado.setForeground(Color.GRAY);
        lblPlanSeleccionado.setBounds(20, 40, 360, 20);
        pnlDerecho.add(lblPlanSeleccionado);

        JLabel lblTotalT = new JLabel("Total a Pagar");
        lblTotalT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotalT.setForeground(Color.GRAY);
        lblTotalT.setBounds(20, 80, 200, 20);
        pnlDerecho.add(lblTotalT);

        lblTotalMonto = new JLabel("S/. 0.00");
        lblTotalMonto.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblTotalMonto.setForeground(new Color(37, 99, 235));
        lblTotalMonto.setBounds(20, 100, 360, 60);
        pnlDerecho.add(lblTotalMonto);

        // Métodos
        JLabel lblMetodo = new JLabel("Método de Pago:");
        lblMetodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMetodo.setBounds(20, 180, 200, 20);
        pnlDerecho.add(lblMetodo);

        btnEfectivo = crearBotonMetodo("💵 Efectivo", 20, 210);
        btnYape = crearBotonMetodo("📱 Yape/Plin", 140, 210);
        btnTransf = crearBotonMetodo("🏦 Banco", 260, 210);

        btnEfectivo.addActionListener(e -> seleccionarMetodo("EFECTIVO"));
        btnYape.addActionListener(e -> seleccionarMetodo("YAPE"));
        btnTransf.addActionListener(e -> seleccionarMetodo("TRANSFERENCIA"));

        pnlDerecho.add(btnEfectivo);
        pnlDerecho.add(btnYape);
        pnlDerecho.add(btnTransf);
        seleccionarMetodo("EFECTIVO");

        // Inputs Cobro
        JLabel lblRecibido = new JLabel("Monto Recibido:");
        lblRecibido.setBounds(20, 280, 150, 20);
        pnlDerecho.add(lblRecibido);

        txtRecibido = new JTextField();
        txtRecibido.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtRecibido.setHorizontalAlignment(SwingConstants.RIGHT);
        txtRecibido.setBounds(20, 305, 160, 50);
        txtRecibido.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calcularVueltoOParcial();
            }
        });
        pnlDerecho.add(txtRecibido);

        JLabel lblVueltoT = new JLabel("Vuelto:");
        lblVueltoT.setBounds(200, 280, 150, 20);
        pnlDerecho.add(lblVueltoT);

        lblVuelto = new JLabel("---");
        lblVuelto.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVuelto.setForeground(new Color(100, 116, 139));
        lblVuelto.setBounds(200, 305, 160, 50);
        pnlDerecho.add(lblVuelto);

        lblEstadoPago = new JLabel("");
        lblEstadoPago.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoPago.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstadoPago.setBounds(20, 365, 340, 20);
        pnlDerecho.add(lblEstadoPago);

        JButton btnConfirmar = new JButton("CONFIRMAR PAGO");
        btnConfirmar.setBackground(new Color(22, 163, 74));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBounds(20, 410, 340, 60);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> accionPagar());
        pnlDerecho.add(btnConfirmar);

        // --- UNIÓN FINAL ---
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlListas, pnlDerecho);
        splitPrincipal.setBorder(null);
        splitPrincipal.setDividerSize(3);
        splitPrincipal.setResizeWeight(1.0); // Prioridad izquierda

        pnlDerecho.setPreferredSize(new Dimension(380, 0));
        pnlDerecho.setMinimumSize(new Dimension(350, 0));

        add(splitPrincipal, BorderLayout.CENTER);
    }

    // --- LÓGICA DE NEGOCIO ---
    private void buscarClientes(String busquedaBD) {
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }
        modeloClientes.setRowCount(0);

        new Thread(() -> {
            SuscripcionDAO dao = new SuscripcionDAO();
            // Traemos TODOS o los filtrados por BD si se requiere,
            // pero para que el filtro local funcione bien, idealmente traemos una lista
            // amplia inicial
            // o lo que el usuario haya buscado con ENTER.
            List<Suscripcion> listaBD = dao.listarTodo(busquedaBD, "NOMBRE (A-Z)");

            // --- GUARDAMOS EN CACHÉ ---
            clientesCache = new ArrayList<>(listaBD);

            SwingUtilities.invokeLater(() -> {
                llenarTablaClientes(clientesCache); // Usamos método auxiliar
                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    private void filtrarLocalmente(String texto) {
        String query = texto.toLowerCase();
        List<Suscripcion> resultados = new ArrayList<>();

        for (Suscripcion s : clientesCache) {
            // Filtramos por Nombre o por Nombre del Servicio (Plan)
            if (s.getNombreCliente().toLowerCase().contains(query)
                    || s.getNombreServicio().toLowerCase().contains(query)) {
                resultados.add(s);
            }
        }
        llenarTablaClientes(resultados);
    }

    private void llenarTablaClientes(List<Suscripcion> lista) {
        modeloClientes.setRowCount(0);
        for (Suscripcion s : lista) {
            modeloClientes.addRow(new Object[] {
                    s.getIdSuscripcion(),
                    s.getNombreCliente(),
                    "---", // DNI (si lo tienes en el modelo Suscripcion, ponlo aqui: s.getDni())
                    s.getNombreServicio(),
                    (s.getActivo() == 1 ? "ACTIVO" : "CORTADO")
            });
        }
    }

    private void seleccionarCliente() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            return;
        }

        // 1. Obtener datos del cliente seleccionado
        this.idSuscripcionActual = Integer.parseInt(modeloClientes.getValueAt(fila, 0).toString());
        this.nombreClienteActual = modeloClientes.getValueAt(fila, 1).toString();
        String plan = modeloClientes.getValueAt(fila, 3).toString();

        // 2. Actualizar UI derecha
        lblClienteSeleccionado.setText("Cliente: " + nombreClienteActual);
        lblPlanSeleccionado.setText("Plan: " + plan);

        // 3. Cargar sus deudas
        cargarDeudasDelCliente(idSuscripcionActual);
    }

    private void cargarDeudasDelCliente(int idSuscripcion) {
        modeloDeudas.setRowCount(0);

        // Mostrar barra de carga
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }

        new Thread(() -> {
            PagoDAO dao = new PagoDAO();
            // Este método debe existir en tu PagoDAO (ver abajo)
            List<Object[]> deudas = dao.buscarDeudasPorSuscripcion(idSuscripcion);

            SwingUtilities.invokeLater(() -> {
                for (Object[] d : deudas) {
                    // d = {idFactura, Mes, Monto, Vence}
                    boolean check = true; // Auto-seleccionar por defecto
                    String mes = d[1].toString();
                    String vence = d[3].toString();
                    String monto = String.format("%.2f", (Double) d[2]);
                    int idFactura = (int) d[0];

                    modeloDeudas.addRow(new Object[] { check, mes, vence, monto, idFactura });
                }
                calcularTotalSeleccionado();

                // Ocultar barra de carga
                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    private void calcularTotalSeleccionado() {
        double total = 0.0;
        for (int i = 0; i < modeloDeudas.getRowCount(); i++) {
            if ((boolean) modeloDeudas.getValueAt(i, 0)) {
                String montoStr = (String) modeloDeudas.getValueAt(i, 3);
                total += Double.parseDouble(montoStr.replace(",", "."));
            }
        }
        this.totalSeleccionado = total;
        lblTotalMonto.setText("S/. " + String.format("%.2f", totalSeleccionado));
        txtRecibido.setText(String.format("%.2f", totalSeleccionado));
        calcularVueltoOParcial();
    }

    private void calcularVueltoOParcial() {
        try {
            double recibido = Double.parseDouble(txtRecibido.getText().replace(",", "."));
            double diferencia = recibido - totalSeleccionado;

            if (totalSeleccionado <= 0) {
                lblVuelto.setText("---");
                lblEstadoPago.setText("");
                return;
            }

            if (diferencia >= -0.01) {
                lblVuelto.setText("S/. " + String.format("%.2f", diferencia));
                lblVuelto.setForeground(new Color(22, 163, 74));
                lblEstadoPago.setText("✅ PAGO COMPLETO");
                lblEstadoPago.setForeground(new Color(22, 163, 74));
            } else {
                lblVuelto.setText("Falta: " + String.format("%.2f", Math.abs(diferencia)));
                lblVuelto.setForeground(new Color(220, 38, 38));
                lblEstadoPago.setText("⚠️ PAGO INCOMPLETO");
                lblEstadoPago.setForeground(new Color(220, 38, 38));
            }
        } catch (NumberFormatException e) {
            lblVuelto.setText("---");
            lblEstadoPago.setText("");
        }
    }

    private void accionPagar() {
        if (totalSeleccionado <= 0) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos una deuda.");
            return;
        }

        try {
            double recibido = Double.parseDouble(txtRecibido.getText().replace(",", "."));
            if (recibido < totalSeleccionado) {
                JOptionPane.showMessageDialog(this, "Monto insuficiente.");
                return;
            }
        } catch (Exception e) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Procesar pago de S/. " + String.format("%.2f", totalSeleccionado) + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Principal.instancia != null) {
                Principal.instancia.mostrarCarga(true);
            }

            // Capturar datos para la boleta antes de modificar la tabla
            final String clienteNombre = nombreClienteActual;
            final String planServicio = lblPlanSeleccionado.getText().replace("Plan: ", "");
            final String metodo = metodoPagoSeleccionado;
            final double montoTotal = totalSeleccionado;

            // Capturar conceptos de los pagos seleccionados
            StringBuilder conceptos = new StringBuilder();
            for (int i = 0; i < modeloDeudas.getRowCount(); i++) {
                if ((boolean) modeloDeudas.getValueAt(i, 0)) {
                    if (conceptos.length() > 0)
                        conceptos.append(", ");
                    conceptos.append(modeloDeudas.getValueAt(i, 1).toString());
                }
            }
            final String conceptoFinal = conceptos.toString();

            new Thread(() -> {
                PagoDAO dao = new PagoDAO();
                boolean error = false;
                int primeraFacturaId = -1;

                for (int i = 0; i < modeloDeudas.getRowCount(); i++) {
                    if ((boolean) modeloDeudas.getValueAt(i, 0)) {
                        int idFactura = (int) modeloDeudas.getValueAt(i, 4);
                        if (primeraFacturaId == -1)
                            primeraFacturaId = idFactura;
                        double monto = Double.parseDouble(modeloDeudas.getValueAt(i, 3).toString().replace(",", "."));
                        if (!dao.realizarCobro(idFactura, monto, 1)) {
                            error = true;
                        }
                    }
                }

                boolean finalError = error;
                final int facturaId = primeraFacturaId;

                // Capturar fecha vencimiento para calcular periodo
                String vencimiento = "";
                for (int i = 0; i < modeloDeudas.getRowCount(); i++) {
                    if ((boolean) modeloDeudas.getValueAt(i, 0)) {
                        vencimiento = modeloDeudas.getValueAt(i, 2).toString();
                        break;
                    }
                }
                final String fechaVenc = vencimiento;

                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                    if (!finalError) {
                        // Generar y abrir boleta PDF
                        try {
                            BoletaPDFService pdfService = new BoletaPDFService();
                            String numeroBoleta = String.format("%06d", facturaId);

                            // Calcular periodo (del 1 al último día del mes)
                            String periodoDesde = "";
                            String periodoHasta = "";
                            if (!fechaVenc.isEmpty()) {
                                try {
                                    // Vencimiento formato yyyy-MM-dd
                                    String[] partes = fechaVenc.split("-");
                                    if (partes.length == 3) {
                                        String anio = partes[0];
                                        String mes = partes[1];
                                        periodoDesde = "01/" + mes + "/" + anio;
                                        // Último día del mes
                                        java.time.YearMonth ym = java.time.YearMonth.of(
                                                Integer.parseInt(anio), Integer.parseInt(mes));
                                        periodoHasta = ym.lengthOfMonth() + "/" + mes + "/" + anio;
                                    }
                                } catch (Exception e) {
                                    periodoDesde = "---";
                                    periodoHasta = "---";
                                }
                            }

                            // Detectar tipo de plan (se puede mejorar leyendo de BD)
                            String tipoPlan = "MENSUAL";

                            pdfService.generarYAbrirBoleta(
                                    numeroBoleta,
                                    clienteNombre,
                                    "CNT-" + idSuscripcionActual,
                                    "", // Dirección (opcional)
                                    conceptoFinal,
                                    planServicio,
                                    tipoPlan,
                                    periodoDesde,
                                    periodoHasta,
                                    montoTotal,
                                    metodo,
                                    "Sistema");
                        } catch (Exception ex) {
                            System.err.println("Error generando boleta: " + ex.getMessage());
                        }

                        JOptionPane.showMessageDialog(this, "✅ Pago exitoso. Boleta generada.");
                        cargarDeudasDelCliente(idSuscripcionActual);
                        txtRecibido.setText("");
                        lblVuelto.setText("---");
                    } else {
                        JOptionPane.showMessageDialog(this, "Hubo un error.");
                    }
                });
            }).start();
        }
    }

    private void adelantarMes() {
        if (idSuscripcionActual == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la lista primero.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Generar recibo adelantado para " + nombreClienteActual + "?", "Adelantar Mes",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Principal.instancia != null) {
                Principal.instancia.mostrarCarga(true);
            }
            new Thread(() -> {
                PagoDAO dao = new PagoDAO();
                boolean ok = dao.generarSiguienteFactura(idSuscripcionActual);
                SwingUtilities.invokeLater(() -> {
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }
                    if (ok) {
                        cargarDeudasDelCliente(idSuscripcionActual); // Ver el nuevo recibo
                        JOptionPane.showMessageDialog(this, "Recibo generado.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al generar.");
                    }
                });
            }).start();
        }
    }

    // --- ESTILOS ---
    private void estilarTabla(JTable t) {
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(230, 230, 230));
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private JButton crearBotonMetodo(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 110, 40);
        estilarBotonPago(btn, false);
        return btn;
    }

    private void estilarBotonPago(JButton btn, boolean activo) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

    private void seleccionarMetodo(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        estilarBotonPago(btnEfectivo, metodo.equals("EFECTIVO"));
        estilarBotonPago(btnYape, metodo.equals("YAPE"));
        estilarBotonPago(btnTransf, metodo.equals("TRANSFERENCIA"));
    }

    private void estilarBotonSimple(JButton btn) {
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
    }

    class ZebraRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setBorder(new EmptyBorder(0, 5, 0, 5));
            if (isSelected) {
                setBackground(new Color(200, 230, 255));
                setForeground(Color.BLACK);
            } else {
                if (row % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(248, 248, 250));
                }
            }
            return this;
        }
    }
}
