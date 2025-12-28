package vista;

import DAO.PagoDAO;
import java.awt.*;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

/**
 * Dialogo de Historial de Pagos EDITABLE.
 * Permite ver, crear, editar y eliminar facturas de un cliente.
 * Integra automaticamente los pagos con movimiento_caja.
 */
public class DialogoHistorial extends JDialog {

    private JTable tabla;
    private DefaultTableModel modelo;
    private PagoDAO pagoDAO;
    private int idSuscripcion;
    private double montoMensual;
    private int diaPago;
    private int idUsuario = 1; // TODO: Obtener del usuario logueado

    public DialogoHistorial(Frame parent, int idSuscripcion, String cliente, double monto, int diaPago) {
        super(parent, "Historial: " + cliente, true);
        this.idSuscripcion = idSuscripcion;
        this.montoMensual = monto;
        this.diaPago = diaPago;
        this.pagoDAO = new PagoDAO();

        setSize(1050, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        initUI();
        cargarDatos();
    }

    // Constructor alternativo para compatibilidad con codigo existente
    public DialogoHistorial(Frame parent, int idSuscripcion, String cliente) {
        this(parent, idSuscripcion, cliente, 50.0, 15); // Valores por defecto
    }

    private void initUI() {
        // --- PANEL SUPERIOR (Header) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(15, 15, 10, 15));

        JLabel lblTitulo = new JLabel("Historial de Pagos y Deudas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(15, 23, 42));
        topPanel.add(lblTitulo, BorderLayout.WEST);

        // Panel de botones de accion
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlAcciones.setBackground(Color.WHITE);

        JButton btnAgregar = new JButton("+ Agregar Mes");
        estilarBoton(btnAgregar, new Color(37, 99, 235), Color.WHITE);
        btnAgregar.addActionListener(e -> abrirDialogoAgregarMes());
        pnlAcciones.add(btnAgregar);

        JButton btnGenerarAnio = new JButton("Generar Anio Completo");
        estilarBoton(btnGenerarAnio, new Color(22, 163, 74), Color.WHITE);
        btnGenerarAnio.addActionListener(e -> abrirDialogoGenerarAnio());
        pnlAcciones.add(btnGenerarAnio);

        topPanel.add(pnlAcciones, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLA ---
        String[] cols = { "ID", "Periodo", "Rango", "F. Creación", "Monto", "Estado", "Fecha Pago", "" };
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false; // No editable directamente
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(40);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setShowVerticalLines(true);
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setSelectionBackground(new Color(219, 234, 254));

        // Ocultar columna ID
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        // Anchos
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Periodo
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Rango (NUEVO)
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90); // Vencimiento
        tabla.getColumnModel().getColumn(4).setPreferredWidth(80); // Monto
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100); // Estado
        tabla.getColumnModel().getColumn(6).setPreferredWidth(90); // Fecha Pago
        tabla.getColumnModel().getColumn(7).setPreferredWidth(180); // Acciones

        // Renderizador de Estado con colores
        tabla.getColumnModel().getColumn(5).setCellRenderer(new EstadoRenderer());

        // Renderizador de Acciones (botones reales dentro de panel)
        tabla.getColumnModel().getColumn(7).setCellRenderer(new AccionesRenderer());

        // Listener para clicks en la tabla
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());

                if (fila >= 0) {
                    int idFactura = (int) modelo.getValueAt(fila, 0);
                    String estado = modelo.getValueAt(fila, 5).toString();

                    if (col == 5) { // Click en Estado -> Alternar
                        alternarEstado(fila, idFactura);
                    } else if (col == 7) { // Click en Acciones
                        // Determinar que boton se clickeo por posicion X
                        Rectangle cellRect = tabla.getCellRect(fila, col, true);
                        int cellX = e.getX() - cellRect.x;
                        int btnWidth = 65; // Ancho de cada boton (60 + padding)

                        if (cellX < btnWidth) {
                            editarFactura(fila, idFactura);
                        } else if (cellX < btnWidth * 2) {
                            // Boleta - solo si NO es pendiente
                            if (!estado.contains("PENDIENTE")) {
                                DialogoHistorial.this.imprimirBoleta(idFactura);
                            } else {
                                JOptionPane.showMessageDialog(DialogoHistorial.this,
                                        "Solo puede imprimir boletas de facturas PAGADAS",
                                        "Factura Pendiente", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            eliminarFactura(fila, idFactura);
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        // --- PANEL INFERIOR ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 15, 0));

        JButton btnRefrescar = new JButton("Refrescar");
        estilarBoton(btnRefrescar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnRefrescar.addActionListener(e -> cargarDatos());
        bottomPanel.add(btnRefrescar);

        JButton btnCerrar = new JButton("Cerrar");
        estilarBoton(btnCerrar, new Color(100, 116, 139), Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        bottomPanel.add(btnCerrar);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);

        // Usar hilo para no bloquear UI
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            List<Object[]> datos = pagoDAO.obtenerHistorialEditable(idSuscripcion);

            SwingUtilities.invokeLater(() -> {
                for (Object[] d : datos) {
                    modelo.addRow(new Object[] {
                            d[0], // ID Factura
                            d[1], // Periodo
                            d[2] != null && !d[2].toString().isEmpty() ? d[2] : "---", // Rango (NUEVO)
                            d[3], // Vencimiento
                            "S/. " + String.format("%.2f", d[4]), // Monto
                            d[5], // Estado
                            d[6] != null ? d[6] : "---", // Fecha Pago
                            "" // Columna de acciones (vacia, el renderer dibuja los botones)
                    });
                }
                setCursor(Cursor.getDefaultCursor());
            });
        }).start();
    }

    private void alternarEstado(int fila, int idFactura) {
        String estadoActual = (String) modelo.getValueAt(fila, 4);
        int nuevoEstado = "PAGADO".equals(estadoActual) ? 1 : 2;
        String accion = nuevoEstado == 2 ? "marcar como PAGADO" : "marcar como PENDIENTE";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Desea " + accion + " este mes?\n" +
                        (nuevoEstado == 2 ? "Se registrara en caja como ingreso."
                                : "NO se eliminara el registro de caja anterior."),
                "Confirmar Cambio de Estado",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new Thread(() -> {
                java.sql.Date fechaPago = new java.sql.Date(System.currentTimeMillis());
                boolean exito = pagoDAO.actualizarEstadoFactura(idFactura, nuevoEstado, fechaPago, idUsuario);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    if (exito) {
                        cargarDatos();
                        JOptionPane.showMessageDialog(this, "Estado actualizado correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar el estado.");
                    }
                });
            }).start();
        }
    }

    private void editarFactura(int fila, int idFactura) {
        String periodoActual = (String) modelo.getValueAt(fila, 1);
        String montoStr = ((String) modelo.getValueAt(fila, 3)).replace("S/. ", "").replace(",", ".");
        double montoActual = Double.parseDouble(montoStr);

        JTextField txtPeriodo = new JTextField(periodoActual);
        JTextField txtMonto = new JTextField(String.format("%.2f", montoActual));
        JDateChooser dateVenc = new JDateChooser();
        dateVenc.setDateFormatString("dd/MM/yyyy");
        dateVenc.setDate((java.util.Date) modelo.getValueAt(fila, 2));

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Periodo (ej: Enero 2024):"));
        panel.add(txtPeriodo);
        panel.add(new JLabel("Monto S/.:"));
        panel.add(txtMonto);
        panel.add(new JLabel("Fecha Vencimiento:"));
        panel.add(dateVenc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Factura",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nuevoPeriodo = txtPeriodo.getText().trim();
                double nuevoMonto = Double.parseDouble(txtMonto.getText().replace(",", "."));
                java.sql.Date nuevaFechaVenc = new java.sql.Date(dateVenc.getDate().getTime());

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new Thread(() -> {
                    boolean exito = pagoDAO.actualizarFactura(idFactura, nuevoPeriodo, nuevoMonto, nuevaFechaVenc);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        if (exito) {
                            cargarDatos();
                            JOptionPane.showMessageDialog(this, "Factura actualizada.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al actualizar.");
                        }
                    });
                }).start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos invalidos: " + ex.getMessage());
            }
        }
    }

    private void eliminarFactura(int fila, int idFactura) {
        String periodo = (String) modelo.getValueAt(fila, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminar la factura de " + periodo + "?\n\n" +
                        "ADVERTENCIA: Los movimientos de caja asociados NO seran eliminados.\n" +
                        "Las boletas PDF asociadas tambien seran eliminadas.",
                "Confirmar Eliminacion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new Thread(() -> {
                // Eliminar boletas PDF asociadas
                servicio.BoletaPDFService pdfService = new servicio.BoletaPDFService();
                int boletasEliminadas = pdfService.eliminarBoletasDeFactura(idFactura);

                // Eliminar factura de la BD
                boolean exito = pagoDAO.eliminarFactura(idFactura);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    if (exito) {
                        cargarDatos();
                        String msg = "Factura eliminada.";
                        if (boletasEliminadas > 0) {
                            msg += "\n" + boletasEliminadas + " boleta(s) PDF eliminada(s).";
                        }
                        JOptionPane.showMessageDialog(this, msg);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al eliminar.");
                    }
                });
            }).start();
        }
    }

    /**
     * Regenera e imprime la boleta PDF para una factura existente.
     */
    private void imprimirBoleta(int idFactura) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            try {
                servicio.BoletaPDFService pdfService = new servicio.BoletaPDFService();
                String ruta = pdfService.regenerarBoletaDesdeFactura(idFactura);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    if (ruta != null) {
                        // El PDF se abre automaticamente
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo generar la boleta.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this,
                            "Error: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void abrirDialogoAgregarMes() {
        // Meses en español
        String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };

        Calendar cal = Calendar.getInstance();
        int mesActual = cal.get(Calendar.MONTH);
        int anioActual = cal.get(Calendar.YEAR);

        // Componentes del diálogo
        JComboBox<String> cmbMes = new JComboBox<>(meses);
        cmbMes.setSelectedIndex(mesActual);

        JSpinner spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, 2020, 2030, 1));
        JTextField txtMonto = new JTextField(String.format("%.2f", montoMensual));

        // Calcular fecha de vencimiento automáticamente
        JLabel lblFechaVenc = new JLabel();
        Runnable actualizarFecha = () -> {
            int mes = cmbMes.getSelectedIndex();
            int anio = (int) spnAnio.getValue();
            int diaVenc = Math.min(diaPago, getDiasEnMes(mes, anio));
            Calendar calVenc = Calendar.getInstance();
            calVenc.set(anio, mes, diaVenc);
            lblFechaVenc.setText(String.format("%02d/%02d/%d", diaVenc, mes + 1, anio));
        };
        actualizarFecha.run();

        cmbMes.addActionListener(e -> actualizarFecha.run());
        spnAnio.addChangeListener(e -> actualizarFecha.run());

        JComboBox<String> cmbEstado = new JComboBox<>(new String[] { "PENDIENTE", "PAGADO" });
        JCheckBox chkRegistrarCaja = new JCheckBox("Registrar en caja (si esta pagado)", false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.add(new JLabel("Mes:"));
        panel.add(cmbMes);
        panel.add(new JLabel("Año:"));
        panel.add(spnAnio);
        panel.add(new JLabel("Monto S/.:"));
        panel.add(txtMonto);
        panel.add(new JLabel("Vencimiento (día " + diaPago + "):"));
        panel.add(lblFechaVenc);
        panel.add(new JLabel("Estado:"));
        panel.add(cmbEstado);
        panel.add(new JLabel(""));
        panel.add(chkRegistrarCaja);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Mes",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int mesSelec = cmbMes.getSelectedIndex();
                int anio = (int) spnAnio.getValue();
                String periodo = meses[mesSelec] + " " + anio;
                double monto = Double.parseDouble(txtMonto.getText().replace(",", "."));

                // Calcular fecha de vencimiento
                int diaVenc = Math.min(diaPago, getDiasEnMes(mesSelec, anio));
                Calendar calVenc = Calendar.getInstance();
                calVenc.set(anio, mesSelec, diaVenc);
                java.sql.Date fechaVenc = new java.sql.Date(calVenc.getTimeInMillis());

                int estado = cmbEstado.getSelectedIndex() == 0 ? 1 : 2;
                boolean registrarEnCaja = chkRegistrarCaja.isSelected();

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new Thread(() -> {
                    boolean exito = pagoDAO.crearFacturaManual(idSuscripcion, periodo, monto,
                            estado, fechaVenc, registrarEnCaja, idUsuario);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        if (exito) {
                            cargarDatos();
                            JOptionPane.showMessageDialog(this, "Mes agregado correctamente.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al agregar (puede que ya exista).");
                        }
                    });
                }).start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos invalidos: " + ex.getMessage());
            }
        }
    }

    // Helper para calcular días en un mes
    private int getDiasEnMes(int mes, int anio) {
        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void abrirDialogoGenerarAnio() {
        Calendar cal = Calendar.getInstance();
        int anioActual = cal.get(Calendar.YEAR);

        JSpinner spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, 2020, 2030, 1));
        JTextField txtMonto = new JTextField(String.format("%.2f", montoMensual));
        JSpinner spnDia = new JSpinner(new SpinnerNumberModel(diaPago, 1, 31, 1));
        JComboBox<String> cmbEstado = new JComboBox<>(new String[] { "Todos PENDIENTES", "Todos PAGADOS" });
        JCheckBox chkRegistrarCaja = new JCheckBox("Registrar en caja (si estan pagados)", false);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Anio a generar:"));
        panel.add(spnAnio);
        panel.add(new JLabel("Monto mensual S/.:"));
        panel.add(txtMonto);
        panel.add(new JLabel("Dia de vencimiento:"));
        panel.add(spnDia);
        panel.add(new JLabel("Estado por defecto:"));
        panel.add(cmbEstado);
        panel.add(new JLabel(""));
        panel.add(chkRegistrarCaja);

        int result = JOptionPane.showConfirmDialog(this, panel, "Generar Anio Completo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int anio = (int) spnAnio.getValue();
                double monto = Double.parseDouble(txtMonto.getText().replace(",", "."));
                int dia = (int) spnDia.getValue();
                int estado = cmbEstado.getSelectedIndex() == 0 ? 1 : 2;
                boolean registrarEnCaja = chkRegistrarCaja.isSelected();

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                new Thread(() -> {
                    int mesesCreados = pagoDAO.generarAnioCompleto(idSuscripcion, anio, dia,
                            monto, estado, registrarEnCaja, idUsuario);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        cargarDatos();

                        if (mesesCreados > 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Se generaron " + mesesCreados + " meses para el anio " + anio + ".\n" +
                                            "(Los meses que ya existian fueron omitidos)");
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "No se generaron nuevos meses.\nEs posible que todos ya existan.");
                        }
                    });
                }).start();
            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // --- RENDERIZADORES ---
    class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isS, boolean hasF, int row, int col) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            String estado = (String) value;
            if ("PAGADO".equals(estado)) {
                lbl.setText("[PAGADO]");
                lbl.setForeground(new Color(22, 163, 74));
                lbl.setBackground(isS ? new Color(219, 234, 254) : new Color(220, 252, 231));
            } else if ("PENDIENTE".equals(estado)) {
                lbl.setText("[PENDIENTE]");
                lbl.setForeground(new Color(220, 38, 38));
                lbl.setBackground(isS ? new Color(219, 234, 254) : new Color(254, 226, 226));
            } else {
                lbl.setText("[" + estado + "]");
                lbl.setForeground(Color.GRAY);
                lbl.setBackground(isS ? new Color(219, 234, 254) : Color.WHITE);
            }
            return lbl;
        }
    }

    class AccionesRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isS, boolean hasF, int row, int col) {

            // Panel con tres botones visuales
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 5));
            panel.setBackground(isS ? new Color(219, 234, 254) : Color.WHITE);

            // Obtener el estado de la factura (columna 5)
            String estado = table.getValueAt(row, 5).toString();
            boolean esPendiente = estado.contains("PENDIENTE");

            // Boton Editar
            JButton btnEditar = new JButton("Editar");
            btnEditar.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnEditar.setBackground(new Color(59, 130, 246));
            btnEditar.setForeground(Color.WHITE);
            btnEditar.setFocusPainted(false);
            btnEditar.setPreferredSize(new Dimension(60, 24));
            panel.add(btnEditar);

            // Boton Boleta (para reimprimir) - DESHABILITADO si está pendiente
            JButton btnBoleta = new JButton("Boleta");
            btnBoleta.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            if (esPendiente) {
                btnBoleta.setBackground(new Color(156, 163, 175)); // Gris
                btnBoleta.setEnabled(false);
                btnBoleta.setToolTipText("Solo disponible para facturas pagadas");
            } else {
                btnBoleta.setBackground(new Color(22, 163, 74)); // Verde
            }
            btnBoleta.setForeground(Color.WHITE);
            btnBoleta.setFocusPainted(false);
            btnBoleta.setPreferredSize(new Dimension(60, 24));
            panel.add(btnBoleta);

            // Boton Eliminar
            JButton btnEliminar = new JButton("Eliminar");
            btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnEliminar.setBackground(new Color(239, 68, 68));
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setFocusPainted(false);
            btnEliminar.setPreferredSize(new Dimension(60, 24));
            panel.add(btnEliminar);

            return panel;
        }
    }
}