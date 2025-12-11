package vista;

import DAO.SuscripcionDAO;
import modelo.Suscripcion;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class subpanel_Suscripciones extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;
    private SuscripcionDAO susDAO;

    // Paginación
    private int paginaActual = 0;
    private final int FILAS = 20;
    private JLabel lblPagina;
    private JButton btnAnt, btnSig;

    public subpanel_Suscripciones() {
        setBackground(Color.WHITE);
        setLayout(null);
        susDAO = new SuscripcionDAO();
        initUI();
        cargarDatos(); // Primera carga automática
    }

    private void initUI() {
        // Título
        JLabel lblTitulo = new JLabel("Gestión de Contratos y Cortes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(15, 23, 42));
        lblTitulo.setBounds(30, 20, 400, 30);
        add(lblTitulo);

        // Botones Globales
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        estilarBoton(btnRefrescar, new Color(241, 245, 249), new Color(15, 23, 42));
        btnRefrescar.setBounds(30, 70, 120, 35);
        btnRefrescar.addActionListener(e -> cargarDatos());
        add(btnRefrescar);

        // Botones de Acción
        JButton btnEditar = new JButton("✐ Modificar Plan");
        estilarBoton(btnEditar, new Color(234, 179, 8), Color.WHITE); // Amarillo
        btnEditar.setBounds(600, 70, 160, 35);
        btnEditar.addActionListener(e -> abrirEdicion());
        add(btnEditar);

        JButton btnCortar = new JButton("✂ CORTAR SERVICIO");
        estilarBoton(btnCortar, new Color(220, 38, 38), Color.WHITE); // Rojo
        btnCortar.setBounds(780, 70, 180, 35);
        btnCortar.addActionListener(e -> cambiarEstadoServicio(0)); // 0 = Cortar
        add(btnCortar);

        JButton btnActivar = new JButton("⚡ RECONECTAR");
        estilarBoton(btnActivar, new Color(22, 163, 74), Color.WHITE); // Verde
        btnActivar.setBounds(970, 70, 150, 35);
        btnActivar.addActionListener(e -> cambiarEstadoServicio(1)); // 1 = Activar
        add(btnActivar);

        // Configuración de Tabla
        String[] cols = {"ID", "Contrato", "Cliente", "Plan", "Dirección", "Inicio", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(248, 250, 252));
        tabla.setShowVerticalLines(false);

        // Colorear filas según estado (ROJO = Suspendido)
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) table.getModel().getValueAt(row, 6);

                if ("SUSPENDIDO".equals(estado)) {
                    setForeground(Color.RED);
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // Negrita para resaltar
                } else {
                    setForeground(Color.BLACK);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                if (isSelected) {
                    setBackground(new Color(220, 230, 255));
                } else {
                    setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(30, 120, 1090, 500);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        add(scroll);

        // Paginación
        btnAnt = new JButton("<");
        estilarBoton(btnAnt, Color.WHITE, Color.BLACK);
        btnAnt.setBounds(30, 640, 50, 30);
        btnAnt.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                cargarDatos();
            }
        });
        add(btnAnt);

        lblPagina = new JLabel("Página 1");
        lblPagina.setBounds(90, 640, 100, 30);
        add(lblPagina);

        btnSig = new JButton(">");
        estilarBoton(btnSig, Color.WHITE, Color.BLACK);
        btnSig.setBounds(160, 640, 50, 30);
        btnSig.addActionListener(e -> {
            paginaActual++;
            cargarDatos();
        });
        add(btnSig);
    }

    private void cargarDatos() {
        if (Principal.instancia != null) {
            Principal.instancia.mostrarCarga(true);
        }

        // TRUCO: Limpiar tabla ANTES de iniciar el hilo para que el usuario vea que "algo pasó"
        modelo.setRowCount(0);

        new Thread(() -> {
            // Ir a la BD
            List<Suscripcion> lista = susDAO.listarPaginado(FILAS, paginaActual * FILAS);

            SwingUtilities.invokeLater(() -> {
                // Verificar que estamos en la UI
                for (Suscripcion s : lista) {
                    modelo.addRow(new Object[]{
                        s.getIdSuscripcion(),
                        s.getCodigoContrato(),
                        s.getNombreCliente(), // Esto se actualizará si cambiaste titular
                        s.getNombreServicio(),
                        s.getDireccionInstalacion(),
                        s.getFechaInicio(),
                        s.getActivo() == 1 ? "ACTIVO" : "SUSPENDIDO"
                    });
                }
                lblPagina.setText("Página " + (paginaActual + 1));

                // Repintar la tabla por si acaso
                tabla.revalidate();
                tabla.repaint();

                if (Principal.instancia != null) {
                    Principal.instancia.mostrarCarga(false);
                }
            });
        }).start();
    }

    // --- 2. MÉTODO DE CORTE / RECONEXIÓN ---
    private void cambiarEstadoServicio(int nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contrato de la tabla.");
            return;
        }

        int id = (int) modelo.getValueAt(fila, 0);
        String cliente = (String) modelo.getValueAt(fila, 2);
        String accion = nuevoEstado == 1 ? "RECONECTAR" : "CORTAR";

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea " + accion + " el servicio de " + cliente + "?",
                "Confirmar Acción", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            // A) ACTIVAR BARRA
            if (Principal.instancia != null) {
                Principal.instancia.mostrarCarga(true);
            }

            new Thread(() -> {
                boolean exito = susDAO.cambiarEstado(id, nuevoEstado);
                SwingUtilities.invokeLater(() -> {

                    // B) APAGAR BARRA
                    if (Principal.instancia != null) {
                        Principal.instancia.mostrarCarga(false);
                    }

                    if (exito) {
                        cargarDatos(); // Recargar tabla automáticamente
                        JOptionPane.showMessageDialog(this, "Estado actualizado: " + accion + " EXITOSO.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al actualizar.");
                    }
                });
            }).start();
        }
    }

    // --- 3. MÉTODO DE EDICIÓN ---
    private void abrirEdicion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un contrato.");
            return;
        }

        // Obtener datos de la tabla
        int idSuscripcion = (int) modelo.getValueAt(fila, 0);
        String plan = (String) modelo.getValueAt(fila, 3);
        String dir = (String) modelo.getValueAt(fila, 4);
        String nombreCliente = (String) modelo.getValueAt(fila, 2);

        // ⚠️ IMPORTANTE: Necesitamos el ID del Cliente actual. 
        // Como no está visible en la tabla, lo mejor es recuperarlo de la suscripción completa
        // O por rapidez, lo sacaremos asumiendo que el objeto SuscripcionDAO lo trae.
        // TRUCO RÁPIDO: Consultar el contrato individualmente o agregarlo oculto en la tabla.
        // Opción Rápida: Buscar ID del cliente por el ID de suscripción en BD
        int idClienteOriginal = susDAO.obtenerIdClienteDeContrato(idSuscripcion);

        java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
        DialogoEditarContrato dialog = new DialogoEditarContrato(
                (java.awt.Frame) parent,
                idSuscripcion,
                plan,
                dir,
                idClienteOriginal,
                nombreCliente
        );

        dialog.setVisible(true);

        if (dialog.isGuardado()) {
            cargarDatos(); // Esto refrescará la tabla y mostrará el nuevo dueño
        }
    }

    private void estilarBoton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
