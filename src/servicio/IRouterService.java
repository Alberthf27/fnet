package servicio;

/**
 * Interface para servicio de control de Router.
 * Permite cortar y reconectar servicios de internet.
 */
public interface IRouterService {

    /**
     * Corta el servicio de internet para una IP específica.
     * 
     * @param ipCliente IP del cliente a cortar (ej: 192.168.1.100)
     * @return true si se cortó correctamente, false si hubo error
     */
    boolean cortarServicio(String ipCliente);

    /**
     * Reconecta el servicio de internet para una IP.
     * 
     * @param ipCliente IP del cliente a reconectar
     * @return true si se reconectó correctamente
     */
    boolean reconectarServicio(String ipCliente);

    /**
     * Verifica si el router está accesible.
     */
    boolean verificarConexion();

    /**
     * Obtiene el nombre del tipo de router.
     */
    String getTipoRouter();
}
