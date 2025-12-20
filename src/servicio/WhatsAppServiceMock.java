package servicio;

/**
 * Implementación MOCK de WhatsApp para pruebas.
 * Solo imprime los mensajes en consola, no envía nada real.
 */
public class WhatsAppServiceMock implements IWhatsAppService {

    @Override
    public boolean enviarMensaje(String telefono, String mensaje) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📱 [MOCK] SIMULACIÓN DE WHATSAPP");
        System.out.println("───────────────────────────────────────────────────");
        System.out.println("📞 Destinatario: " + telefono);
        System.out.println("📝 Mensaje:");
        System.out.println(mensaje);
        System.out.println("═══════════════════════════════════════════════════");
        return true; // Siempre "exitoso" en modo mock
    }

    @Override
    public boolean estaHabilitado() {
        return true; // Siempre habilitado para pruebas
    }

    @Override
    public String getNombreServicio() {
        return "Mock (Simulación)";
    }
}
