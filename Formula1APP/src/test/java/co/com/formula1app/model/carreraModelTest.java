package co.com.formula1app.model;
import co.com.model.Carrera;
import co.com.model.Resultado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class carreraModelTest {
        @Test
        void testGettersAndSetters() {
            Carrera carrera = new Carrera();
            carrera.setId(1L);
            carrera.setNombreGp("Gran Premio de Bahrein");
            carrera.setFecha(LocalDate.of(2024, 3, 2));
            carrera.setGpNumero(1);

            assertEquals(1L, carrera.getId());
            assertEquals("Gran Premio de Bahrein", carrera.getNombreGp());
            assertEquals(LocalDate.of(2024, 3, 2), carrera.getFecha());
            assertEquals(1, carrera.getGpNumero());
        }

        @Test
        void testConstructores() {
            Carrera carrera = new Carrera("Gran Premio de Mónaco", LocalDate.of(2024, 5, 26), 7);
            assertEquals("Gran Premio de Mónaco", carrera.getNombreGp());
            assertEquals(LocalDate.of(2024, 5, 26), carrera.getFecha());
            assertEquals(7, carrera.getGpNumero());
        }

        @Test
        void testAddAndRemoveResultado() {
            Carrera carrera = new Carrera();
            Resultado resultado = new Resultado();

            // Agregar
            carrera.addResultado(resultado);
            assertTrue(carrera.getResultados().contains(resultado));
            assertEquals(carrera, resultado.getCarrera());

            // Remover
            carrera.removeResultado(resultado);
            assertFalse(carrera.getResultados().contains(resultado));
            assertNull(resultado.getCarrera());
        }

        @Test
        void testToString() {
            Carrera carrera = new Carrera("Gran Premio de Italia", LocalDate.of(2024, 9, 8), 16);
            String toString = carrera.toString();

            assertTrue(toString.contains("Gran Premio de Italia"));
            assertTrue(toString.contains("2024-09-08"));
        }

        @Test
        void testSetResultadosList() {
            Carrera carrera = new Carrera();
            List<Resultado> lista = new ArrayList<>();
            Resultado r = new Resultado();
            lista.add(r);

            carrera.setResultados(lista);
            assertEquals(1, carrera.getResultados().size());
        }
}
