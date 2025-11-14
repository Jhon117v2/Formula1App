package co.com.formula1app.constantsTest;
import co.com.constants.F1PointsSystem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class F1PointsSystemTest {

        @Test
        void testCalculatePointsForValidPositions() {
            // 1er lugar debe tener 25 puntos
            assertEquals(BigDecimal.valueOf(25), F1PointsSystem.calculatePoints(1, false));

            // 2do lugar debe tener 18 puntos
            assertEquals(BigDecimal.valueOf(18), F1PointsSystem.calculatePoints(2, false));

            // 10mo lugar debe tener 1 punto
            assertEquals(BigDecimal.valueOf(1), F1PointsSystem.calculatePoints(10, false));
        }

        @Test
        void testCalculatePointsForInvalidPosition() {
            // Posiciones > 10 no dan puntos
            assertEquals(BigDecimal.ZERO, F1PointsSystem.calculatePoints(11, false));
            assertEquals(BigDecimal.ZERO, F1PointsSystem.calculatePoints(20, false));
            assertEquals(BigDecimal.ZERO, F1PointsSystem.calculatePoints(0, false));
            assertEquals(BigDecimal.ZERO, F1PointsSystem.calculatePoints(-3, false));
        }

        @Test
        void testCalculatePointsWithFastestLap() {
            // El piloto con vuelta rápida suma +1 punto si está en top 10
            BigDecimal puntosConFastLap = F1PointsSystem.calculatePoints(5, true);
            BigDecimal puntosSinFastLap = F1PointsSystem.calculatePoints(5, false);

            assertEquals(puntosSinFastLap.add(BigDecimal.ONE), puntosConFastLap);
        }

        @Test
        void testFastestLapNoPointsIfOutsideTop10() {
            // Vuelta rápida fuera del top 10 no debe sumar punto
            assertEquals(BigDecimal.ZERO, F1PointsSystem.calculatePoints(11, true));
        }
}
