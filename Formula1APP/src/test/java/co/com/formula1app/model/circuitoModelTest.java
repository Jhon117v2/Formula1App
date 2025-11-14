package co.com.formula1app.model;
import co.com.model.Carrera;
import co.com.model.Circuito;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class circuitoModelTest {
        @Test
        void testGettersAndSetters() {
            Circuito circuito = new Circuito();
            circuito.setId(1L);
            circuito.setNombre("Monza");
            circuito.setUbicacion("Italia");

            assertEquals(1L, circuito.getId());
            assertEquals("Monza", circuito.getNombre());
            assertEquals("Italia", circuito.getUbicacion());
        }

        @Test
        void testConstructorConParametros() {
            Circuito circuito = new Circuito("Silverstone", "Reino Unido");

            assertEquals("Silverstone", circuito.getNombre());
            assertEquals("Reino Unido", circuito.getUbicacion());
        }

        @Test
        void testSetYGetCarreras() {
            Circuito circuito = new Circuito();
            Carrera carrera = new Carrera();
            carrera.setNombreGp("Gran Premio de Italia");

            List<Carrera> lista = new ArrayList<>();
            lista.add(carrera);

            circuito.setCarreras(lista);

            assertEquals(1, circuito.getCarreras().size());
            assertEquals("Gran Premio de Italia", circuito.getCarreras().get(0).getNombreGp());
        }

        @Test
        void testToString() {
            Circuito circuito = new Circuito("Suzuka", "Japón");
            circuito.setId(10L);

            String texto = circuito.toString();

            assertTrue(texto.contains("Suzuka"));
            assertTrue(texto.contains("Japón"));
            assertTrue(texto.contains("10"));
        }
}
