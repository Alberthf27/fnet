package vista;

import DAO.ServicioDAO;
import modelo.Servicio;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DialogoServicio extends JDialog {

    private JTextField txtNombre;
    private JSpinner spinVelocidad;
    private JTextField txtPrecio;
    private JCheckBox chkActivo;
    private boolean guardado = false;
    private Servicio servicioActual; // Si es null = Nuevo, si tiene objeto = Editar

    public DialogoServicio(Frame parent, Servicio servicio) {
        super(parent, true);
        this.servicioActual = servicio;
        
        setTitle(servicio == null ? "Nuevo Servicio" : "Editar Servicio");
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        initUI();
        
        if (servicio != null) {
            cargarDatos();
        }
    }

    private void initUI() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Etiquetas y Campos
        crearLabel(panel, "Nombre del Plan:", 20, 20);
        txtNombre = new JTextField();
        txtNombre.setBounds(20, 45, 340, 30);
        panel.add(txtNombre);

        crearLabel(panel, "Velocidad (Megas):", 20, 85);
        spinVelocidad = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 10)); // Def: 10, Min: 1
        spinVelocidad.setBounds(20, 110, 150, 30);
        panel.add(spinVelocidad);

        crearLabel(panel, "Mensualidad (S/.):", 200, 85);
        txtPrecio = new JTextField();
        txtPrecio.setBounds(200, 110, 160, 30);
        panel.add(txtPrecio);

        chkActivo = new JCheckBox("Servicio Activo");
        chkActivo.setBackground(Color.WHITE);
        chkActivo.setSelected(true);
        chkActivo.setBounds(20, 160, 200, 30);
        panel.add(chkActivo);

        // Botones
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(37, 99, 235));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setBounds(20, 240, 170, 40);
        btnGuardar.addActionListener(e -> guardar());
        panel.add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(200, 240, 160, 40);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);

        add(panel, BorderLayout.CENTER);
    }

    private void crearLabel(JPanel p, String txt, int x, int y) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.GRAY);
        l.setBounds(x, y, 200, 20);
        p.add(l);
    }

    private void cargarDatos() {
        txtNombre.setText(servicioActual.getDescripcion());
        spinVelocidad.setValue(servicioActual.getVelocidadMb());
        txtPrecio.setText(String.valueOf(servicioActual.getMensualidad()));
        chkActivo.setSelected(servicioActual.getActivo() == 1);
    }

    private void guardar() {
        // Validaciones básicas
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
            return;
        }
        
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido");
            return;
        }

        int velocidad = (int) spinVelocidad.getValue();
        int activo = chkActivo.isSelected() ? 1 : 0;

        ServicioDAO dao = new ServicioDAO();
        boolean exito;

        if (servicioActual == null) {
            // NUEVO
            Servicio s = new Servicio();
            s.setDescripcion(nombre);
            s.setVelocidadMb(velocidad);
            s.setMensualidad(precio);
            s.setActivo(activo);
            exito = dao.insertar(s);
        } else {
            // EDITAR
            servicioActual.setDescripcion(nombre);
            servicioActual.setVelocidadMb(velocidad);
            servicioActual.setMensualidad(precio);
            servicioActual.setActivo(activo);
            exito = dao.actualizar(servicioActual);
        }

        if (exito) {
            guardado = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar en base de datos.");
        }
    }

    public boolean isGuardado() {
        return guardado;
    }
}