package co.com.formula1app.model;
import co.com.model.Carrera;
import co.com.model.Temporada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class temporadaModelTest {
        private Temporada temporada;

        @BeforeEach
        void setUp() {
            temporada = new Temporada();
        }

        @Test
        void testConstructorVacio() {
            assertNull(temporada.getId());
            assertNull(temporada.getAnio());
            assertNotNull(temporada.getCarreras());
            assertTrue(temporada.getCarreras().isEmpty());
        }

        @Test
        void testConstructorConParametros() {
            Temporada t = new Temporada(2025);
            assertEquals(2025, t.getAnio());
            assertNotNull(t.getCarreras());
            assertTrue(t.getCarreras().isEmpty());
        }

        @Test
        void testSettersYGetters() {
            temporada.setId(1L);
            temporada.setAnio(2024);

            assertEquals(1L, temporada.getId());
            assertEquals(2024, temporada.getAnio());
        }

        @Test
        void testAddCarrera() {
            Carrera carrera = new Carrera("GP de Brasil", null, 21);

            temporada.addCarrera(carrera);

            List<Carrera> carreras = temporada.getCarreras();
            assertEquals(1, carreras.size());
            assertEquals(temporada, carrera.getTemporada());
        }

        @Test
        void testRemoveCarrera() {
            Carrera carrera = new Carrera("GP de México", null, 20);
            temporada.addCarrera(carrera);

            temporada.removeCarrera(carrera);

            assertTrue(temporada.getCarreras().isEmpty());
            assertNull(carrera.getTemporada());
        }

        @Test
        void testToString() {
            temporada.setId(10L);
            temporada.setAnio(2023);
            String texto = temporada.toString();

            assertTrue(texto.contains("2023"));
            assertTrue(texto.contains("10"));
        }
}
