package vista;

import DAO.ClienteDAO;
import DAO.SuscripcionDAO;
import modelo.Cliente;
import javax.swing.JOptionPane;

public class subpanel_NuevoCliente extends javax.swing.JPanel {

    public subpanel_NuevoCliente() {
        initComponents();
    }

    private void guardarCliente() {
        String dni = txtDni.getText();
        String nombres = txtNombres.getText();
        String apellidos = txtApellidos.getText();
        String direccion = txtDireccion.getText();
        String correo = txtCorreo.getText();
        String telf = txtTelefono.getText();

        if(dni.isEmpty() || nombres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "DNI y Nombres son obligatorios");
            return;
        }

        Cliente c = new Cliente();
        c.setDniCliente(dni);
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setDireccion(direccion);
        c.setCorreo(correo);
        // c.setTelefono(telf); // Asegúrate de agregar setTelefono a tu modelo Cliente

        ClienteDAO dao = new ClienteDAO();
        Long id = dao.insertarCliente(c);
        
        if(id != null) {
            // Crear suscripción por defecto
            new SuscripcionDAO().crearSuscripcionPorDefecto(id, 1L); 
            JOptionPane.showMessageDialog(this, "¡Cliente registrado con éxito!");
            limpiarFormulario();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar");
        }
    }
    
    private void limpiarFormulario() {
        txtDni.setText(""); txtNombres.setText(""); txtApellidos.setText("");
        txtDireccion.setText(""); txtCorreo.setText(""); txtTelefono.setText("");
    }

    private void initComponents() {
        // ... (Aquí iría el código generado por NetBeans con los campos de texto) ...
        // Como es largo, te sugiero crearlo visualmente en NetBeans arrastrando TextFields y Labels
        // Y luego pegar la lógica del botón "Guardar" llamando a guardarCliente()
        
        // Estructura básica sugerida:
        // JLabel "DNI:", JTextField txtDni
        // JLabel "Nombres:", JTextField txtNombres
        // ...
        // JButton btnGuardar (Action -> guardarCliente())
    }
    
    // Variables para los campos (defínelas o usa el editor visual)
    private javax.swing.JTextField txtDni, txtNombres, txtApellidos, txtDireccion, txtCorreo, txtTelefono;
}