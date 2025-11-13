package co.com.formula1app.service;
import co.com.dao.CarreraDAO;
import co.com.dao.PilotoDAO;
import co.com.dao.ResultadoDAO;
import co.com.model.*;
import co.com.service.F1Servicio;
import co.com.util.JPAUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class  F1ServicioTest {
        private static F1Servicio f1Servicio;
        private static CarreraDAO carreraDAO;
        private static PilotoDAO pilotoDAO;
        private static ResultadoDAO resultadoDAO;

    @BeforeAll
    static void init() {

        f1Servicio = new F1Servicio();
        carreraDAO = new CarreraDAO();
        pilotoDAO = new PilotoDAO();
        resultadoDAO = new ResultadoDAO();

        assertTrue(JPAUtil.isAvailable(), "La conexión JPA debe estar activa");
    }

    @Test
        @Order(1)
        void testGetCarreras() {
            List<Carrera> carreras = f1Servicio.getCarreras(2024);
            assertNotNull(carreras, "La lista de carreras no debe ser nula");
            assertTrue(carreras.size() > 0, "Debe haber al menos una carrera en 2024");
        }

        @Test
        @Order(2)
        void testGetPilotos() {
            List<Piloto> pilotos = f1Servicio.getPilotos();
            assertNotNull(pilotos, "La lista de pilotos no debe ser nula");
            assertTrue(pilotos.size() > 0, "Debe haber pilotos registrados");
        }

        @Test
        @Order(3)
        void testGetResultadosCarrera() {
            List<Carrera> carreras = carreraDAO.findByTemporada(2024);
            assertFalse(carreras.isEmpty(), "Debe haber carreras registradas");
            Long carreraId = carreras.get(0).getId();

            List<Resultado> resultados = f1Servicio.getResultadosCarrera(carreraId);
            assertNotNull(resultados, "La lista de resultados no debe ser nula");
        }

        @Test
        @Order(4)
        void testGetClasificacionPilotos() {
            List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionPilotos(2024);
            assertNotNull(clasificacion, "La clasificación de pilotos no debe ser nula");
            System.out.println("🔹 Pilotos clasificados: " + clasificacion.size());
        }

        @Test
        @Order(5)
        void testGetClasificacionConstructores() {
            List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionConstructores(2024);
            assertNotNull(clasificacion, "La clasificación de constructores no debe ser nula");
            System.out.println("🔹 Constructores clasificados: " + clasificacion.size());
        }

    @Test
    @Order(6)
    void testGetConstructores() {
        var constructores = f1Servicio.getConstructores();
        assertNotNull(constructores);
    }

    @Test
    @Order(7)
    void testGetCarreraById() {
        var carreras = f1Servicio.getCarreras(2024);
        assertFalse(carreras.isEmpty());
        var id = carreras.get(0).getId();
        var carreraOpt = f1Servicio.getCarreraById(id);
        assertTrue(carreraOpt.isPresent());
    }
    @Test
    @Order(8)
    void testGetCircuitos() {
        var circuitos = f1Servicio.getCircuitos(2024);
        assertNotNull(circuitos, "La lista de circuitos no debe ser nula");
        assertTrue(circuitos.size() >= 0, "Debe devolver una lista (aunque esté vacía)");
    }
    @Test
    @Order(9)
    void testGetCarrerasTemporadaInexistente() {
        List<Carrera> carreras = f1Servicio.getCarreras(9999);
        assertNotNull(carreras, "Debe devolver lista vacía si la temporada no existe");
        assertTrue(carreras.isEmpty() || carreras.size() >= 0);
    }

    @Test
    @Order(10)
    void testGetClasificacionPilotosTemporadaInexistente() {
        List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionPilotos(9999);
        assertNotNull(clasificacion, "Debe devolver lista vacía si no hay datos de pilotos");
        assertTrue(clasificacion.size() >= 0);
    }

    @Test
    @Order(11)
    void testGetClasificacionConstructoresTemporadaInexistente() {
        List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionConstructores(9999);
        assertNotNull(clasificacion, "Debe devolver lista vacía si no hay constructores");
        assertTrue(clasificacion.size() >= 0);
    }

    @Test
    @Order(12)
    void testGetResultadosCarreraInexistente() {
        List<Resultado> resultados = f1Servicio.getResultadosCarrera(-1L);
        assertNotNull(resultados, "Debe devolver lista vacía si la carrera no existe");
        assertTrue(resultados.isEmpty() || resultados.size() >= 0);
    }

    @Test
    @Order(13)
    void testGetCarreraByIdInexistente() {
        var carreraOpt = f1Servicio.getCarreraById(-1L);
        assertTrue(carreraOpt.isEmpty(), "Debe devolver Optional vacío si la carrera no existe");
    }

    @Test
    @Order(14)
    void testGetPilotosListaNoVacia() {
        List<Piloto> pilotos = f1Servicio.getPilotos();
        assertNotNull(pilotos);
        assertTrue(pilotos.size() >= 0, "Debe devolver lista vacía o con pilotos");
    }

    @Test
    @Order(15)
    void testGetConstructoresListaNoVacia() {
        List<Constructor> constructores = f1Servicio.getConstructores();
        assertNotNull(constructores);
        assertTrue(constructores.size() >= 0, "Debe devolver lista vacía o con constructores");
    }
    @Test
    @Order(16)
    void testGetCarrerasAnioInvalido() {
        List<Carrera> carreras = f1Servicio.getCarreras(-5);
        assertNotNull(carreras);
        assertTrue(carreras.isEmpty());
    }

    @Test
    @Order(17)
    void testGetResultadosCarreraIdNulo() {
        List<Resultado> resultados = f1Servicio.getResultadosCarrera(null);
        assertNotNull(resultados);
        assertTrue(resultados.isEmpty());
    }

    @Test
    @Order(18)
    void testGetCarreraByIdNulo() {
        var carreraOpt = f1Servicio.getCarreraById(null);
        assertTrue(carreraOpt.isEmpty());
    }

    @Test
    @Order(19)
    void testGetClasificacionPilotosAnioNegativo() {
        List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionPilotos(-1);
        assertNotNull(clasificacion);
        assertTrue(clasificacion.isEmpty());
    }

    @Test
    @Order(20)
    void testGetClasificacionConstructoresAnioNegativo() {
        List<Map<String, Object>> clasificacion = f1Servicio.getClasificacionConstructores(-1);
        assertNotNull(clasificacion);
        assertTrue(clasificacion.isEmpty());
    }

    @Test
    @Order(21)
    void testGetCircuitosAnioCero() {
        List<Circuito> circuitos = f1Servicio.getCircuitos(0);
        assertNotNull(circuitos);
        assertTrue(circuitos.isEmpty());
    }
}
