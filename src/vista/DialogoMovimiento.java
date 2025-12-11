package vista;

import DAO.FinanzasDAO;
import modelo.CategoriaMovimiento;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DialogoMovimiento extends JDialog {

    private JComboBox<CategoriaMovimiento> cmbCategoria;
    private JTextField txtMonto, txtDescripcion;
    private String tipoAccion; // "INGRESO" o "EGRESO"
    private boolean guardado = false;

    public DialogoMovimiento(java.awt.Frame parent, String tipo) {
        super(parent, true);
        this.tipoAccion = tipo;
        
        setTitle("Registrar " + tipo);
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panel);

        // Color del tema según la acción
        Color colorTema = tipoAccion.equals("INGRESO") ? new Color(22, 163, 74) : new Color(220, 38, 38);

        JLabel lblTitulo = new JLabel("Nueva Operación: " + tipoAccion);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(colorTema);
        lblTitulo.setBounds(20, 10, 300, 30);
        panel.add(lblTitulo);

        int y = 60;

        // 1. Categoría
        panel.add(crearLabel("Categoría / Concepto:", 20, y));
        cmbCategoria = new JComboBox<>();
        cmbCategoria.setBounds(20, y + 25, 340, 35);
        panel.add(cmbCategoria);
        y += 75;

        // 2. Monto
        panel.add(crearLabel("Monto (S/.):", 20, y));
        txtMonto = new JTextField();
        txtMonto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtMonto.setBounds(20, y + 25, 340, 35);
        // Borde del color del tema
        txtMonto.setBorder(BorderFactory.createLineBorder(colorTema, 2)); 
        panel.add(txtMonto);
        y += 75;

        // 3. Descripción
        panel.add(crearLabel("Nota / Detalle:", 20, y));
        txtDescripcion = new JTextField();
        txtDescripcion.setBounds(20, y + 25, 340, 35);
        txtDescripcion.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(txtDescripcion);
        y += 80;

        // Botón Guardar
        JButton btnGuardar = new JButton("GUARDAR OPERACIÓN");
        btnGuardar.setBackground(colorTema);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBounds(80, y, 220, 45);
        btnGuardar.addActionListener(e -> guardar());
        panel.add(btnGuardar);

        // Cargar categorías
        cargarCategorias();
    }

    private void cargarCategorias() {
        FinanzasDAO dao = new FinanzasDAO();
        List<CategoriaMovimiento> lista = dao.listarCategorias(tipoAccion);
        for (CategoriaMovimiento c : lista) {
            cmbCategoria.addItem(c);
        }
    }

    private void guardar() {
        try {
            double monto = Double.parseDouble(txtMonto.getText());
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.");
                return;
            }

            // Si es EGRESO, lo convertimos a negativo para la BD
            if (tipoAccion.equals("EGRESO")) {
                monto = monto * -1;
            }

            CategoriaMovimiento cat = (CategoriaMovimiento) cmbCategoria.getSelectedItem();
            String desc = txtDescripcion.getText();
            
            // Asumimos Usuario ID 1 (Admin) por defecto, luego podrías pasar el logueado
            int idUsuario = 1; 

            FinanzasDAO dao = new FinanzasDAO();
            if (dao.registrarMovimiento(monto, desc, cat.getIdCategoria(), idUsuario)) {
                JOptionPane.showMessageDialog(this, "Operación registrada correctamente.");
                guardado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido.");
        }
    }

    public boolean isGuardado() { return guardado; }

    private JLabel crearLabel(String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.GRAY);
        l.setBounds(x, y, 200, 20);
        return l;
    }
}