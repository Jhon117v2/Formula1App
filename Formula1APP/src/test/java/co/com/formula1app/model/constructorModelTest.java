package co.com.formula1app.model;
import co.com.model.Constructor;
import co.com.model.Piloto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class constructorModelTest {
        private Constructor constructor;

        @BeforeEach
        void setUp() {
            constructor = new Constructor("Red Bull Racing", "Austria");
        }

        @Test
        void testConstructorConParametros() {
            assertEquals("Red Bull Racing", constructor.getNombre());
            assertEquals("Austria", constructor.getNacionalidad());
        }

        @Test
        void testSettersYGetters() {
            constructor.setId(1L);
            constructor.setNombre("Ferrari");
            constructor.setNacionalidad("Italia");

            assertEquals(1L, constructor.getId());
            assertEquals("Ferrari", constructor.getNombre());
            assertEquals("Italia", constructor.getNacionalidad());
        }

        @Test
        void testAddPiloto() {
            Piloto piloto = new Piloto();
            piloto.setNombre("Max Verstappen");

            constructor.addPiloto(piloto);

            assertTrue(constructor.getPilotos().contains(piloto));
            assertEquals(constructor, piloto.getConstructor());
        }

        @Test
        void testRemovePiloto() {
            Piloto piloto = new Piloto();
            piloto.setNombre("Sergio Pérez");

            constructor.addPiloto(piloto);
            constructor.removePiloto(piloto);

            assertFalse(constructor.getPilotos().contains(piloto));
            assertNull(piloto.getConstructor());
        }

        @Test
        void testSetPilotosListaCompleta() {
            Piloto p1 = new Piloto();
            Piloto p2 = new Piloto();
            List<Piloto> lista = new ArrayList<>();
            lista.add(p1);
            lista.add(p2);

            constructor.setPilotos(lista);

            assertEquals(2, constructor.getPilotos().size());
        }

        @Test
        void testToString() {
            constructor.setId(5L);
            String result = constructor.toString();

            assertTrue(result.contains("Constructor"));
            assertTrue(result.contains("Red Bull Racing"));
            assertTrue(result.contains("Austria"));
        }
}
