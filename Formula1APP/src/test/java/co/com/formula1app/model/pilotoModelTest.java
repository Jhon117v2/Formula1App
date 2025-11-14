package co.com.formula1app.model;

import co.com.model.Constructor;
import co.com.model.Piloto;
import co.com.model.Resultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class pilotoModelTest {
        private Piloto piloto;

        @BeforeEach
        void setUp() {
            piloto = new Piloto("Lewis Hamilton", "Reino Unido", "44");
        }

        @Test
        void testConstructorConParametros() {
            assertEquals("Lewis Hamilton", piloto.getNombre());
            assertEquals("Reino Unido", piloto.getNacionalidad());
            assertEquals("44", piloto.getDorsal());
        }

        @Test
        void testSettersYGetters() {
            piloto.setId(1L);
            piloto.setNombre("Charles Leclerc");
            piloto.setNacionalidad("Mónaco");
            piloto.setDorsal("16");

            assertEquals(1L, piloto.getId());
            assertEquals("Charles Leclerc", piloto.getNombre());
            assertEquals("Mónaco", piloto.getNacionalidad());
            assertEquals("16", piloto.getDorsal());
        }

        @Test
        void testRelacionConConstructor() {
            Constructor constructor = new Constructor("Ferrari", "Italia");
            piloto.setConstructor(constructor);

            assertEquals(constructor, piloto.getConstructor());
        }

        @Test
        void testSetYGetResultados() {
            Resultado r1 = new Resultado();
            Resultado r2 = new Resultado();

            List<Resultado> resultados = new ArrayList<>();
            resultados.add(r1);
            resultados.add(r2);

            piloto.setResultados(resultados);

            assertEquals(2, piloto.getResultados().size());
            assertTrue(piloto.getResultados().contains(r1));
            assertTrue(piloto.getResultados().contains(r2));
        }

        @Test
        void testToString() {
            piloto.setId(10L);
            String texto = piloto.toString();

            assertTrue(texto.contains("Piloto"));
            assertTrue(texto.contains("Lewis Hamilton"));
            assertTrue(texto.contains("Reino Unido"));
            assertTrue(texto.contains("44"));
        }

        @Test
        void testConstructorVacio() {
            Piloto p = new Piloto();
            assertNull(p.getId());
            assertNull(p.getNombre());
            assertNull(p.getNacionalidad());
            assertNull(p.getDorsal());
        }
}
