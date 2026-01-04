package vista;

import servicio.YapePagoProcessor;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;

/**
 * Panel para subir archivos Excel de Yape mediante drag & drop.
 * Procesa los pagos automáticamente y sincroniza con la base de datos.
 */
public class PanelSubirYape extends JPanel {

    private JLabel lblEstado;
    private JTextArea txtResultado;
    private YapePagoProcessor yapeProcesador;
    private JPanel panelDrop;

    public PanelSubirYape() {
        this.yapeProcesador = new YapePagoProcessor();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Procesar Pagos Yape");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(37, 99, 235));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel central con zona de drop
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(Color.WHITE);

        // Zona de arrastrar y soltar
        panelDrop = new JPanel();
        panelDrop.setLayout(new BoxLayout(panelDrop, BoxLayout.Y_AXIS));
        panelDrop.setBackground(new Color(249, 250, 251));
        panelDrop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 2, true),
                new EmptyBorder(40, 40, 40, 40)));
        panelDrop.setPreferredSize(new Dimension(600, 200));

        // Icono
        JLabel lblIcono = new JLabel("📁");
        lblIcono.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDrop.add(lblIcono);

        panelDrop.add(Box.createVerticalStrut(20));

        // Texto principal
        JLabel lblInstruccion = new JLabel("Arrastra el Excel de Yape aquí");
        lblInstruccion.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblInstruccion.setForeground(new Color(55, 65, 81));
        lblInstruccion.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDrop.add(lblInstruccion);

        panelDrop.add(Box.createVerticalStrut(10));

        // Texto secundario
        JLabel lblFormato = new JLabel("o haz clic para seleccionar archivo");
        lblFormato.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFormato.setForeground(new Color(107, 114, 128));
        lblFormato.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDrop.add(lblFormato);

        panelDrop.add(Box.createVerticalStrut(10));

        JLabel lblTipo = new JLabel("Formato: .xlsx (Excel de Yape)");
        lblTipo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTipo.setForeground(new Color(156, 163, 175));
        lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDrop.add(lblTipo);

        // Configurar drag and drop
        configurarDragAndDrop();

        // Click para abrir selector de archivos
        panelDrop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelDrop.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirSelectorArchivos();
            }
        });

        panelCentral.add(panelDrop, BorderLayout.CENTER);

        // Estado
        lblEstado = new JLabel("Listo para procesar archivos");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEstado.setForeground(new Color(107, 114, 128));
        lblEstado.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelCentral.add(lblEstado, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        // Área de resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBackground(Color.WHITE);
        panelResultados.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                "Resultados del Procesamiento",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(55, 65, 81)));

        txtResultado = new JTextArea(10, 50);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtResultado.setEditable(false);
        txtResultado.setBackground(new Color(249, 250, 251));
        txtResultado.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtResultado);
        scroll.setBorder(null);
        panelResultados.add(scroll, BorderLayout.CENTER);

        add(panelResultados, BorderLayout.SOUTH);
    }

    private void configurarDragAndDrop() {
        new DropTarget(panelDrop, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                panelDrop.setBackground(new Color(219, 234, 254));
                panelDrop.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(37, 99, 235), 2, true),
                        new EmptyBorder(40, 40, 40, 40)));
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                panelDrop.setBackground(new Color(249, 250, 251));
                panelDrop.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 2, true),
                        new EmptyBorder(40, 40, 40, 40)));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> archivos = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    if (!archivos.isEmpty()) {
                        File archivo = archivos.get(0);
                        procesarArchivo(archivo);
                    }

                    dtde.dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error al procesar archivo: " + e.getMessage());
                    dtde.dropComplete(false);
                }

                // Restaurar estilo
                dragExit(null);
            }
        });
    }

    private void abrirSelectorArchivos() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Excel de Yape");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Archivos Excel (*.xlsx)";
            }
        });

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            procesarArchivo(archivo);
        }
    }

    private void procesarArchivo(File archivo) {
        // Validar extensión
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            mostrarError("El archivo debe ser un Excel (.xlsx)");
            return;
        }

        // Limpiar resultados anteriores
        txtResultado.setText("");
        lblEstado.setText("Procesando " + archivo.getName() + "...");
        lblEstado.setForeground(new Color(37, 99, 235));

        // Procesar en segundo plano
        SwingWorker<YapePagoProcessor.ResumenProcesamiento, String> worker = new SwingWorker<YapePagoProcessor.ResumenProcesamiento, String>() {
            @Override
            protected YapePagoProcessor.ResumenProcesamiento doInBackground() throws Exception {
                return yapeProcesador.procesarExcel(archivo);
            }

            @Override
            protected void done() {
                try {
                    YapePagoProcessor.ResumenProcesamiento resumen = get();
                    mostrarResumen(resumen, archivo.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarError("Error procesando archivo: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void mostrarResumen(YapePagoProcessor.ResumenProcesamiento resumen, String nombreArchivo) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ Archivo procesado: ").append(nombreArchivo).append("\n\n");
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("RESUMEN DE PROCESAMIENTO\n");
        sb.append("═══════════════════════════════════════════════════\n\n");
        sb.append("📋 Total de filas procesadas: ").append(resumen.totalFilas).append("\n");
        sb.append("✅ Pagos registrados exitosamente: ").append(resumen.pagosRegistrados).append("\n");
        sb.append("Ya procesados (duplicados evitados): ").append(resumen.yaProcesados).append("\n");
        sb.append("Transacciones ignoradas: ").append(resumen.ignorados).append("\n");
        sb.append("📝 Sin DNI en mensaje: ").append(resumen.sinDNI).append("\n");
        sb.append("DNI no encontrado en BD: ").append(resumen.dniNoEncontrado).append("\n");
        sb.append("❌ Errores: ").append(resumen.errores).append("\n\n");

        if (resumen.pagosRegistrados > 0) {
            sb.append("🎉 Los pagos se han sincronizado con la base de datos.\n");
            lblEstado.setText("✅ Procesamiento completado - " + resumen.pagosRegistrados + " pagos registrados");
            lblEstado.setForeground(new Color(22, 163, 74));
        } else if (resumen.yaProcesados > 0) {
            sb.append("Todas las transacciones ya fueron procesadas anteriormente.\n");
            lblEstado.setText("Sin pagos nuevos - " + resumen.yaProcesados + " duplicados evitados");
            lblEstado.setForeground(new Color(59, 130, 246));
        } else {
            sb.append("No se encontraron pagos válidos para procesar.\n");
            lblEstado.setText("No se registraron pagos");
            lblEstado.setForeground(new Color(234, 179, 8));
        }

        txtResultado.setText(sb.toString());
    }

    private void mostrarError(String mensaje) {
        txtResultado.setText("❌ ERROR\n\n" + mensaje);
        lblEstado.setText("❌ Error en el procesamiento");
        lblEstado.setForeground(new Color(239, 68, 68));
    }
}
