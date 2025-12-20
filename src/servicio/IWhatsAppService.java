package servicio;

/**
 * Interface para servicio de envío de WhatsApp.
 * Permite cambiar la implementación (CallMeBot, Green-API, Twilio) sin
 * modificar el código de negocio.
 */
public interface IWhatsAppService {

    /**
     * Envía un mensaje de WhatsApp al número especificado.
     * 
     * @param telefono Número de teléfono en formato internacional (ej: 51987654321)
     * @param mensaje  Texto del mensaje a enviar
     * @return true si se envió correctamente, false si hubo error
     */
    boolean enviarMensaje(String telefono, String mensaje);

    /**
     * Verifica si el servicio está configurado y habilitado.
     */
    boolean estaHabilitado();

    /**
     * Obtiene el nombre de la implementación actual.
     */
    String getNombreServicio();
}
