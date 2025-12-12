package vista;

import DAO.PagoDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DialogoHistorial extends JDialog {

    public DialogoHistorial(Frame parent, int idSuscripcion, String cliente) {
        super(parent, "Historial: " + cliente, true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Título
        JLabel lblTitulo = new JLabel("Historial de Pagos y Deudas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"Periodo", "Vencimiento", "Monto", "Estado", "Fecha Pago"};
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Renderizador de Colores para Estado
        tabla.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isS, boolean hasF, int row, int col) {
                super.getTableCellRendererComponent(table, value, isS, hasF, row, col);
                String estado = (String) value;
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if ("PENDIENTE".equals(estado)) setForeground(Color.RED);
                else if ("PAGADO".equals(estado)) setForeground(new Color(34, 197, 94)); // Verde
                else setForeground(Color.GRAY);
                return this;
            }
        });

        // Cargar Datos
        PagoDAO dao = new PagoDAO();
        List<Object[]> datos = dao.obtenerHistorialCompleto(idSuscripcion);
        for (Object[] d : datos) {
            modelo.addRow(d);
        }

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        // Botón Cerrar
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel pnlSur = new JPanel();
        pnlSur.add(btnCerrar);
        add(pnlSur, BorderLayout.SOUTH);
    }
}