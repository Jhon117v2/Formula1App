package co.com.formula1app.model;
import co.com.model.Carrera;
import co.com.model.Piloto;
import co.com.model.ResultadoSprint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class resultadoSprintModelTest {
        private ResultadoSprint resultadoSprint;

        @BeforeEach
        void setUp() {
            resultadoSprint = new ResultadoSprint();
        }

        @Test
        void testValoresIniciales() {
            assertNull(resultadoSprint.getId());
            assertNull(resultadoSprint.getCarrera());
            assertNull(resultadoSprint.getPiloto());
            assertNull(resultadoSprint.getPosicionFinal());
            assertNull(resultadoSprint.getPuntosObtenidos());
            assertNull(resultadoSprint.getVueltas());
            assertNull(resultadoSprint.getTiempo());
            assertFalse(resultadoSprint.getRetirado(), "El valor por defecto de 'retirado' debe ser false");
            assertNull(resultadoSprint.getMotivoRetiro());
        }

        @Test
        void testSettersYGetters() {
            Carrera carrera = new Carrera();
            Piloto piloto = new Piloto();

            resultadoSprint.setId(5L);
            resultadoSprint.setCarrera(carrera);
            resultadoSprint.setPiloto(piloto);
            resultadoSprint.setPosicionFinal(1);
            resultadoSprint.setPuntosObtenidos(new BigDecimal("8.00"));
            resultadoSprint.setVueltas(17);
            resultadoSprint.setTiempo("25:40");
            resultadoSprint.setRetirado(true);
            resultadoSprint.setMotivoRetiro("Colisión");

            assertEquals(5L, resultadoSprint.getId());
            assertEquals(carrera, resultadoSprint.getCarrera());
            assertEquals(piloto, resultadoSprint.getPiloto());
            assertEquals(1, resultadoSprint.getPosicionFinal());
            assertEquals(new BigDecimal("8.00"), resultadoSprint.getPuntosObtenidos());
            assertEquals(17, resultadoSprint.getVueltas());
            assertEquals("25:40", resultadoSprint.getTiempo());
            assertTrue(resultadoSprint.getRetirado());
            assertEquals("Colisión", resultadoSprint.getMotivoRetiro());
        }

        @Test
        void testCambioEstadoRetiro() {
            assertFalse(resultadoSprint.getRetirado());
            resultadoSprint.setRetirado(true);
            assertTrue(resultadoSprint.getRetirado());
        }
}
