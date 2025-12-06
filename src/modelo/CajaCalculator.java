

package modelo;
import modelo.MovimientoCaja;

import java.math.BigDecimal;
import java.util.List;


public class CajaCalculator {

    public static BigDecimal calcularTotalIngresos(List<MovimientoCaja> movimientos) {
        return movimientos.stream()
                .filter(m -> "INGRESO".equals(m.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calcularTotalEgresos(List<MovimientoCaja> movimientos) {
        return movimientos.stream()
                .filter(m -> "EGRESO".equals(m.getTipo()))
                .map(MovimientoCaja::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calcularSaldoCaja(BigDecimal montoInicial,
            BigDecimal totalIngresos,
            BigDecimal totalEgresos) {
        return montoInicial.add(totalIngresos).subtract(totalEgresos);
    }

    public static boolean validarMontoCierre(BigDecimal montoCalculado, BigDecimal montoReal) {
        // Validar que la diferencia no sea mayor a 1 sol
        BigDecimal diferencia = montoCalculado.subtract(montoReal).abs();
        return diferencia.compareTo(new BigDecimal("1.00")) <= 0;
    }
}
