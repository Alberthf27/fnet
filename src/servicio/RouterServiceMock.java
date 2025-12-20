package servicio;

/**
 * Implementación MOCK del servicio de router para pruebas.
 * Solo imprime las acciones en consola, no hace nada real.
 */
public class RouterServiceMock implements IRouterService {

    @Override
    public boolean cortarServicio(String ipCliente) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🔴 [MOCK] SIMULACIÓN DE CORTE DE SERVICIO");
        System.out.println("───────────────────────────────────────────────────");
        System.out.println("📍 IP Cliente: " + ipCliente);
        System.out.println("⚡ Acción: Agregar a lista de bloqueados");
        System.out.println("═══════════════════════════════════════════════════");
        return true;
    }

    @Override
    public boolean reconectarServicio(String ipCliente) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("🟢 [MOCK] SIMULACIÓN DE RECONEXIÓN DE SERVICIO");
        System.out.println("───────────────────────────────────────────────────");
        System.out.println("📍 IP Cliente: " + ipCliente);
        System.out.println("⚡ Acción: Eliminar de lista de bloqueados");
        System.out.println("═══════════════════════════════════════════════════");
        return true;
    }

    @Override
    public boolean verificarConexion() {
        return true;
    }

    @Override
    public String getTipoRouter() {
        return "Mock (Simulación)";
    }
}
