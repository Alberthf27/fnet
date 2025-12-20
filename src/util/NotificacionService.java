package util;

/**
 * @deprecated Esta clase ya no se usa.
 *             El envío de WhatsApp ahora se hace directamente desde
 *             CobrosAutomaticoService
 *             usando CallMeBotWhatsAppService.
 * 
 *             Puedes eliminar este archivo de forma segura.
 */
@Deprecated
public class NotificacionService {

    /**
     * @deprecated Usar CallMeBotWhatsAppService.enviarMensaje() en su lugar.
     */
    @Deprecated
    public boolean enviarAlerta(String telefono, String mensaje) {
        System.err.println("⚠️ NotificacionService está deprecado. Usar CallMeBotWhatsAppService.");
        return false;
    }
}