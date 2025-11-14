package co.com.formula1app.config;
import co.com.config.ConfiguracionTemporada;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ConfiguracionTemporadaTest {

        @Test
        void testEsDespuesDeCongelacion() {
            LocalDate antes = LocalDate.of(2024, 12, 31);
            LocalDate igual = ConfiguracionTemporada.FECHA_CONGELACION;
            LocalDate despues = LocalDate.of(2025, 1, 8);

            assertFalse(ConfiguracionTemporada.esDespuesDeCongelacion(antes));
            assertFalse(ConfiguracionTemporada.esDespuesDeCongelacion(igual));
            assertTrue(ConfiguracionTemporada.esDespuesDeCongelacion(despues));
        }

        @Test
        void testPermiteIngresoManual() {
            LocalDate antes = LocalDate.of(2024, 12, 31);
            LocalDate igual = ConfiguracionTemporada.FECHA_CONGELACION;
            LocalDate despues = LocalDate.of(2025, 2, 1);

            assertFalse(ConfiguracionTemporada.permiteIngresoManual(antes));
            assertTrue(ConfiguracionTemporada.permiteIngresoManual(igual));
            assertTrue(ConfiguracionTemporada.permiteIngresoManual(despues));
        }

        @Test
        void testGetMensajeCongelacion() {
            String mensaje = ConfiguracionTemporada.getMensajeCongelacion();

            assertNotNull(mensaje);
            assertTrue(mensaje.contains("Fecha de congelación del proyecto"));
            assertTrue(mensaje.contains("2025"));
        }

        @Test
        void testConstantesTemporada() {
            assertEquals(2024, ConfiguracionTemporada.TEMPORADA_2024);
            assertEquals(2025, ConfiguracionTemporada.TEMPORADA_2025);
            assertEquals(ConfiguracionTemporada.TEMPORADA_2025, ConfiguracionTemporada.TEMPORADA_ACTUAL);
        }
    }
