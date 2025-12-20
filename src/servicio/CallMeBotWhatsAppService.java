package servicio;

import DAO.ConfiguracionDAO;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Implementación de WhatsApp usando CallMeBot API.
 * Es gratuita y funciona con HTTP GET simple.
 * 
 * Configuración requerida:
 * 1. Enviar al +34 644 51 95 23: "I allow callmebot to send me messages"
 * 2. Guardar el apikey recibido en configuracion_sistema
 */
public class CallMeBotWhatsAppService implements IWhatsAppService {

    private final ConfiguracionDAO configDAO;
    private static final String API_URL = "https://api.callmebot.com/whatsapp.php";

    public CallMeBotWhatsAppService() {
        this.configDAO = new ConfiguracionDAO();
    }

    @Override
    public boolean enviarMensaje(String telefono, String mensaje) {
        if (!estaHabilitado()) {
            System.out.println("⚠️ WhatsApp no está habilitado en configuración.");
            return false;
        }

        String apiKey = configDAO.obtenerValor(ConfiguracionDAO.CALLMEBOT_APIKEY);
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("⚠️ API Key de CallMeBot no configurada.");
            return false;
        }

        try {
            // Limpiar número de teléfono (solo dígitos)
            String telefonoLimpio = telefono.replaceAll("[^0-9]", "");

            // Agregar código de país si no lo tiene
            if (!telefonoLimpio.startsWith("51") && telefonoLimpio.length() == 9) {
                telefonoLimpio = "51" + telefonoLimpio; // Perú por defecto
            }

            // Construir URL
            String urlStr = String.format("%s?phone=%s&text=%s&apikey=%s",
                    API_URL,
                    telefonoLimpio,
                    URLEncoder.encode(mensaje, "UTF-8"),
                    URLEncoder.encode(apiKey, "UTF-8"));

            System.out.println("📱 Enviando WhatsApp a: " + telefonoLimpio);

            // Hacer la petición HTTP GET
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();

            // Leer respuesta
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            boolean exito = responseCode == 200 &&
                    response.toString().toLowerCase().contains("message queued");

            if (exito) {
                System.out.println("✅ Mensaje enviado correctamente.");
            } else {
                System.out.println("❌ Error al enviar: " + response.toString());
            }

            return exito;

        } catch (Exception e) {
            System.err.println("❌ Error al enviar WhatsApp: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean estaHabilitado() {
        return configDAO.obtenerValorBoolean(ConfiguracionDAO.WHATSAPP_HABILITADO);
    }

    @Override
    public String getNombreServicio() {
        return "CallMeBot";
    }
}
