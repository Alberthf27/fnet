package servicio;

import DAO.ConfiguracionDAO;
import java.io.*;
import java.net.Socket;

/**
 * Implementación de control de router MikroTik usando la API de RouterOS.
 * Usa el protocolo API en puerto 8728.
 * 
 * Funciona igual que Winbox: solo necesita IP, usuario y contraseña.
 */
public class MikroTikRouterService implements IRouterService {

    private final ConfiguracionDAO configDAO;
    private static final int API_PORT = 8728;

    public MikroTikRouterService() {
        this.configDAO = new ConfiguracionDAO();
    }

    @Override
    public boolean cortarServicio(String ipCliente) {
        if (!verificarConfiguracion())
            return false;

        System.out.println("🔴 Cortando servicio para IP: " + ipCliente);

        // El corte se hace agregando la IP a una Address List de bloqueo
        // o deshabilitando la regla de queue/firewall del cliente
        String[] comandos = {
                "/ip/firewall/address-list/add",
                "=list=bloqueados",
                "=address=" + ipCliente,
                "=comment=Corte automático por falta de pago"
        };

        return ejecutarComando(comandos);
    }

    @Override
    public boolean reconectarServicio(String ipCliente) {
        if (!verificarConfiguracion())
            return false;

        System.out.println("🟢 Reconectando servicio para IP: " + ipCliente);

        // Primero buscamos el ID de la entrada en address-list
        // y luego la eliminamos
        String[] comandosBuscar = {
                "/ip/firewall/address-list/print",
                "?list=bloqueados",
                "?address=" + ipCliente
        };

        // Simplificación: Intentamos eliminar por dirección
        String[] comandosEliminar = {
                "/ip/firewall/address-list/remove",
                "=.id=*" // Se debe obtener el ID real primero
        };

        // Por ahora, usamos el método find
        String comandoCompleto = String.format(
                "/ip/firewall/address-list/remove [find address=\"%s\" list=\"bloqueados\"]",
                ipCliente);

        return ejecutarComandoSimple(comandoCompleto);
    }

    @Override
    public boolean verificarConexion() {
        String ip = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_IP);
        if (ip == null || ip.isEmpty())
            return false;

        try (Socket socket = new Socket(ip, API_PORT)) {
            socket.setSoTimeout(5000);
            return socket.isConnected();
        } catch (Exception e) {
            System.err.println("❌ No se puede conectar al router: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getTipoRouter() {
        return "MikroTik RouterOS";
    }

    /**
     * Verifica que la configuración del router esté completa.
     */
    private boolean verificarConfiguracion() {
        String ip = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_IP);
        String usuario = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_USUARIO);

        if (ip == null || ip.isEmpty()) {
            System.err.println("⚠️ IP del router MikroTik no configurada.");
            return false;
        }
        if (usuario == null || usuario.isEmpty()) {
            System.err.println("⚠️ Usuario del router MikroTik no configurado.");
            return false;
        }
        return true;
    }

    /**
     * Ejecuta un comando en el router usando la API de RouterOS.
     * Esta es una implementación simplificada. Para producción se recomienda
     * usar una librería como mikrotik-java-api.
     */
    private boolean ejecutarComando(String[] comandos) {
        String ip = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_IP);
        String usuario = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_USUARIO);
        String password = configDAO.obtenerValor(ConfiguracionDAO.MIKROTIK_PASSWORD);
        if (password == null)
            password = "";

        try (Socket socket = new Socket(ip, API_PORT)) {
            socket.setSoTimeout(10000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 1. Login
            enviarPalabra(out, "/login");
            enviarPalabra(out, "=name=" + usuario);
            enviarPalabra(out, "=password=" + password);
            enviarPalabra(out, ""); // Fin del comando

            // Esperar respuesta de login
            String respuestaLogin = leerRespuesta(in);
            if (!respuestaLogin.contains("!done")) {
                System.err.println("❌ Error de autenticación con MikroTik");
                return false;
            }

            // 2. Ejecutar comandos
            for (String cmd : comandos) {
                enviarPalabra(out, cmd);
            }
            enviarPalabra(out, ""); // Fin del bloque de comandos

            // 3. Leer respuesta
            String respuesta = leerRespuesta(in);
            boolean exito = respuesta.contains("!done") && !respuesta.contains("!trap");

            if (exito) {
                System.out.println("✅ Comando ejecutado correctamente en MikroTik");
            } else {
                System.err.println("❌ Error en MikroTik: " + respuesta);
            }

            return exito;

        } catch (Exception e) {
            System.err.println("❌ Error conectando a MikroTik: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ejecuta un comando simple usando SSH como alternativa.
     */
    private boolean ejecutarComandoSimple(String comando) {
        // TODO: Implementar via SSH o librería mikrotik-java-api
        System.out.println("⚙️ Ejecutando comando MikroTik: " + comando);
        return ejecutarComando(new String[] { comando });
    }

    /**
     * Envía una "palabra" en el formato de la API de RouterOS.
     */
    private void enviarPalabra(DataOutputStream out, String palabra) throws IOException {
        byte[] bytes = palabra.getBytes("UTF-8");
        int len = bytes.length;

        if (len < 0x80) {
            out.write(len);
        } else if (len < 0x4000) {
            out.write((len >> 8) | 0x80);
            out.write(len);
        } else if (len < 0x200000) {
            out.write((len >> 16) | 0xC0);
            out.write(len >> 8);
            out.write(len);
        } else if (len < 0x10000000) {
            out.write((len >> 24) | 0xE0);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len);
        } else {
            out.write(0xF0);
            out.write(len >> 24);
            out.write(len >> 16);
            out.write(len >> 8);
            out.write(len);
        }
        out.write(bytes);
        out.flush();
    }

    /**
     * Lee la respuesta del router.
     */
    private String leerRespuesta(DataInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                int len = leerLongitud(in);
                if (len == 0)
                    break;

                byte[] buffer = new byte[len];
                in.readFully(buffer);
                String palabra = new String(buffer, "UTF-8");
                sb.append(palabra).append("\n");

                if (palabra.equals("!done") || palabra.startsWith("!trap")) {
                    break;
                }
            }
        } catch (Exception e) {
            // Timeout o final de datos
        }
        return sb.toString();
    }

    /**
     * Lee la longitud de la siguiente palabra en formato RouterOS.
     */
    private int leerLongitud(DataInputStream in) throws IOException {
        int first = in.read();
        if (first < 0)
            return 0;

        if ((first & 0x80) == 0) {
            return first;
        } else if ((first & 0xC0) == 0x80) {
            return ((first & 0x3F) << 8) | in.read();
        } else if ((first & 0xE0) == 0xC0) {
            return ((first & 0x1F) << 16) | (in.read() << 8) | in.read();
        } else if ((first & 0xF0) == 0xE0) {
            return ((first & 0x0F) << 24) | (in.read() << 16) | (in.read() << 8) | in.read();
        } else {
            return (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | in.read();
        }
    }
}
