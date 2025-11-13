package co.com.formula1app.dao;
import co.com.dao.*;
import co.com.model.*;
import co.com.util.JPAUtil;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResultadoDAOTest {
        private static ResultadoDAO resultadoDAO;
        private static CarreraDAO carreraDAO;
        private static PilotoDAO pilotoDAO;
        private static ConstructorDAO constructorDAO;

        private static Long carreraIdTest;
        private static Long pilotoIdTest;

        @BeforeAll
        static void init() {
            resultadoDAO = new ResultadoDAO();
            carreraDAO = new CarreraDAO();
            pilotoDAO = new PilotoDAO();
            constructorDAO = new ConstructorDAO();

            // Buscar una carrera existente (usa cualquiera de la temporada actual)
            List<Carrera> carreras = carreraDAO.findByTemporada(2025);
            assertFalse(carreras.isEmpty(), "Debe haber carreras en la temporada 2025");
            carreraIdTest = carreras.get(0).getId();

            // Buscar un piloto existente (usamos el primero disponible)
            List<Piloto> pilotos = pilotoDAO.findAll();
            assertFalse(pilotos.isEmpty(), "Debe haber pilotos en la base de datos");
            pilotoIdTest = pilotos.get(0).getId();
        }

        @Test
        @Order(1)
        void testGuardarResultado() {
            Resultado resultado = new Resultado();
            Carrera carrera = carreraDAO.findById(carreraIdTest).orElseThrow();
            Piloto piloto = pilotoDAO.findById(pilotoIdTest).orElseThrow();

            resultado.setCarrera(carrera);
            resultado.setPiloto(piloto);
            resultado.setPosicionFinal(1);
            resultado.setPuntosObtenidos(BigDecimal.valueOf(25));
            resultado.setVueltas(50);
            resultado.setTiempo("1:30:15.123");
            resultado.setRetirado(false);

            Resultado guardado = resultadoDAO.save(resultado);

            assertNotNull(guardado.getId(), "El resultado debe haberse guardado con ID asignado");
            assertEquals(1, guardado.getPosicionFinal());
            assertEquals(BigDecimal.valueOf(25), guardado.getPuntosObtenidos());
        }

        @Test
        @Order(2)
        void testBuscarPorCarrera() {
            List<Resultado> resultados = resultadoDAO.findByCarrera(carreraIdTest);
            assertNotNull(resultados, "Debe devolver lista de resultados");
            assertTrue(resultados.size() > 0, "Debe haber al menos un resultado");
            assertNotNull(resultados.get(0).getPiloto());
        }

        @Test
        @Order(3)
        void testClasificacionPilotos() {
            List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionPilotos(2025);
            assertNotNull(clasificacion, "Debe devolver la clasificación de pilotos");
            assertTrue(clasificacion.size() > 0, "Debe haber pilotos clasificados");
            assertTrue(clasificacion.get(0).containsKey("puntos"), "Debe contener puntos acumulados");
        }

        @Test
        @Order(4)
        void testClasificacionConstructores() {
            List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionConstructores(2025);
            assertNotNull(clasificacion, "Debe devolver clasificación de constructores");
            assertTrue(clasificacion.size() > 0, "Debe haber constructores clasificados");
            assertTrue(clasificacion.get(0).containsKey("puntos"), "Debe contener puntos acumulados");
        }
    @Test
    @Order(5)
    void testFindByCarreraConError() {
        ResultadoDAO dao = new ResultadoDAO();
        assertThrows(RuntimeException.class, () -> {
            dao.findByCarrera(null); // Forzamos un error porque el parámetro es nulo
        }, "Debe lanzar excepción por carreraId nulo");
    }

    @Test
    @Order(6)
    void testGuardarResultadoConError() {
        ResultadoDAO dao = new ResultadoDAO();
        Resultado resultado = new Resultado();
        // No se asignan carrera ni piloto -> debe fallar
        assertThrows(RuntimeException.class, () -> {
            dao.save(resultado);
        }, "Debe lanzar excepción al intentar guardar sin datos requeridos");
    }

    @Test
    @Order(7)
    void testClasificacionPilotosConError() {
        ResultadoDAO dao = new ResultadoDAO();
        assertThrows(RuntimeException.class, () -> {
            dao.getClasificacionPilotos(null); // Año nulo fuerza el error en el PreparedStatement
        }, "Debe lanzar excepción al pasar año nulo");
    }

    @Test
    @Order(8)
    void testClasificacionConstructoresConError() {
        ResultadoDAO dao = new ResultadoDAO();
        assertThrows(RuntimeException.class, () -> {
            dao.getClasificacionConstructores(null);
        }, "Debe lanzar excepción al pasar año nulo");
    }

    @Test
    @Order(9)
    void testFindByCarreraInexistente() {
        List<Resultado> resultados = resultadoDAO.findByCarrera(999999L);
        assertNotNull(resultados, "La lista no debe ser nula");
        assertTrue(resultados.isEmpty(), "No debe devolver resultados para carrera inexistente");
    }
    @Test
    @Order(10)
    void testClasificacionPilotosSinDatos() {
        List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionPilotos(2099); // Año futuro sin datos
        assertNotNull(clasificacion, "La clasificación no debe ser nula");
        assertTrue(clasificacion.isEmpty(), "Debe devolver lista vacía para año sin datos");
    }
    @Test
    @Order(11)
    void testClasificacionConstructoresSinDatos() {
        List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionConstructores(2099); // Año futuro sin datos
        assertNotNull(clasificacion, "La clasificación no debe ser nula");
        assertTrue(clasificacion.isEmpty(), "Debe devolver lista vacía para año sin datos");
    }


}
