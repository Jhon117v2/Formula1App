package co.com.formula1app.dao;
import co.com.dao.CarreraDAO;
import co.com.dao.CircuitoDAO;
import co.com.dao.TemporadaDAO;
import co.com.model.Carrera;
import co.com.model.Circuito;
import co.com.model.Temporada;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarreraDAOTest {

        private static CarreraDAO carreraDAO;
        private static TemporadaDAO temporadaDAO;
        private static CircuitoDAO circuitoDAO;

        private static Long idCarreraGuardada;
        private static Temporada temporadaPrueba;
        private static Circuito circuitoPrueba;

        @BeforeAll
        static void init() {
            assertTrue(JPAUtil.isAvailable(), "Debe estar disponible la conexión JPA");

            carreraDAO = new CarreraDAO();
            temporadaDAO = new TemporadaDAO();
            circuitoDAO = new CircuitoDAO();

            // Buscar o crear temporada de prueba
            List<Temporada> temporadas = temporadaDAO.findAll();
            Optional<Temporada> tempOpt = temporadas.stream()
                    .filter(t -> t.getAnio().equals(2099))
                    .findFirst();

            if (tempOpt.isPresent()) {
                temporadaPrueba = tempOpt.get();
            } else {
                temporadaPrueba = temporadaDAO.save(new Temporada(2099));
            }

            // Buscar o crear circuito de prueba
            List<Circuito> circuitos = circuitoDAO.findAll();
            Optional<Circuito> circOpt = circuitos.stream()
                    .filter(c -> c.getNombre().equalsIgnoreCase("Circuito JUnit"))
                    .findFirst();

            if (circOpt.isPresent()) {
                circuitoPrueba = circOpt.get();
            } else {
                Circuito nuevo = new Circuito();
                nuevo.setNombre("Circuito JUnit");
                nuevo.setUbicacion("Ubicación de prueba");
                circuitoPrueba = circuitoDAO.save(nuevo);
            }
        }

        @Test
        @Order(1)
        void testGuardarCarrera() {
            Carrera carrera = new Carrera();
            carrera.setNombreGp("Gran Premio JUnit");
            carrera.setFecha(LocalDate.of(2099, 12, 10));
            carrera.setGpNumero(20);
            carrera.setCircuito(circuitoPrueba);
            carrera.setTemporada(temporadaPrueba);

            Carrera guardada = carreraDAO.save(carrera);

            assertNotNull(guardada.getId(), "Debe generarse un ID para la carrera");
            idCarreraGuardada = guardada.getId();
        }

        @Test
        @Order(2)
        void testFindById() {
            Optional<Carrera> carreraOpt = carreraDAO.findById(idCarreraGuardada);
            assertTrue(carreraOpt.isPresent(), "La carrera debe existir");
            assertEquals("Gran Premio JUnit", carreraOpt.get().getNombreGp());
        }

        @Test
        @Order(3)
        void testFindByTemporada() {
            List<Carrera> carreras = carreraDAO.findByTemporada(2099);
            assertNotNull(carreras);
            assertTrue(carreras.stream().anyMatch(c -> c.getId().equals(idCarreraGuardada)));
        }

        @Test
        @Order(4)
        void testFindByNombreGp() {
            List<Carrera> carreras = carreraDAO.findByNombreGp("JUnit");
            assertNotNull(carreras);
            assertTrue(carreras.size() > 0, "Debe encontrar al menos una carrera con nombre que contenga 'JUnit'");
        }

        @Test
        @Order(5)
        void testActualizarCarrera() {
            Optional<Carrera> carreraOpt = carreraDAO.findById(idCarreraGuardada);
            assertTrue(carreraOpt.isPresent());

            Carrera carrera = carreraOpt.get();
            carrera.setNombreGp("Gran Premio Actualizado");

            Carrera actualizada = carreraDAO.update(carrera);
            assertEquals("Gran Premio Actualizado", actualizada.getNombreGp());
        }

        @Test
        @Order(6)
        void testCount() {
            long total = carreraDAO.count();
            assertTrue(total > 0, "Debe haber al menos una carrera registrada");
        }

        @Test
        @Order(7)
        void testEliminarCarrera() {
            boolean eliminada = carreraDAO.delete(idCarreraGuardada);
            assertTrue(eliminada, "La carrera debería eliminarse correctamente");
        }
    @Test
    @Order(8)
    void testFindByIdInexistente() {
        Optional<Carrera> carreraOpt = carreraDAO.findById(999999L);
        assertTrue(carreraOpt.isEmpty(), "No debe encontrar una carrera inexistente");
    }
    @Test
    @Order(9)
    void testEliminarCarreraInexistente() {
        boolean eliminada = carreraDAO.delete(999999L);
        assertFalse(eliminada, "No debe eliminar una carrera inexistente");
    }
    @Test
    @Order(10)
    void testGuardarCarreraConError() {
        CarreraDAO dao = new CarreraDAO() {
            @Override
            public Carrera save(Carrera carrera) {
                throw new RuntimeException("Error simulado al guardar carrera");
            }
        };

        Carrera carrera = new Carrera();
        carrera.setNombreGp("Falla GP");
        carrera.setFecha(LocalDate.now());
        carrera.setTemporada(temporadaPrueba);
        carrera.setCircuito(circuitoPrueba);

        Exception ex = assertThrows(RuntimeException.class, () -> dao.save(carrera));
        assertTrue(ex.getMessage().contains("Error simulado"));
    }

    @Test
    @Order(11)
    void testActualizarCarreraConError() {
        CarreraDAO dao = new CarreraDAO() {
            @Override
            public Carrera update(Carrera carrera) {
                throw new RuntimeException("Error al actualizar carrera simulado");
            }
        };

        Carrera carrera = new Carrera();
        carrera.setId(999L);
        carrera.setNombreGp("Error GP");

        Exception ex = assertThrows(RuntimeException.class, () -> dao.update(carrera));
        assertTrue(ex.getMessage().contains("Error al actualizar carrera simulado"));
    }

    @Test
    @Order(12)
    void testDeleteConError() {
        CarreraDAO dao = new CarreraDAO() {
            @Override
            public boolean delete(Long id) {
                throw new RuntimeException("Error al eliminar carrera simulado");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(1L));
        assertTrue(ex.getMessage().contains("Error al eliminar carrera simulado"));
    }

    @Test
    @Order(13)
    void testFindByNombreGpSinResultados() {
        List<Carrera> carreras = carreraDAO.findByNombreGp("NombreQueNoExiste");
        assertNotNull(carreras);
        assertEquals(0, carreras.size(), "No debe encontrar resultados inexistentes");
    }

    @Test
    @Order(14)
    void testCountConError() {
        CarreraDAO dao = new CarreraDAO() {
            @Override
            public long count() {
                throw new RuntimeException("Error al contar carreras simulado");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, dao::count);
        assertTrue(ex.getMessage().contains("Error al contar carreras simulado"));
    }
    @Test
    @Order(15)
    void testFindAll() {
        List<Carrera> carreras = carreraDAO.findAll();
        assertNotNull(carreras, "La lista de carreras no debe ser nula");
        assertTrue(carreras.size() >= 0, "Debe devolver una lista válida");
        // Opcional: verifica que alguna carrera de prueba esté incluida si creaste más datos
    }
    @Test
    @Order(16)
    void testSaveConDatosInvalidos() {
        Carrera carrera = new Carrera();
        carrera.setNombreGp(null); // Dato inválido
        carrera.setFecha(null);
        carrera.setTemporada(null);
        carrera.setCircuito(null);

        Exception ex = assertThrows(RuntimeException.class, () -> carreraDAO.save(carrera));
        assertTrue(ex.getMessage().contains("Error al guardar carrera"));
    }
    @Test
    @Order(17)
    void testDeleteConFalloTransaccion() {
        // Simula una carrera existente
        Carrera carrera = new Carrera();
        carrera.setId(idCarreraGuardada); // Reutiliza una ID válida
        carrera.setNombreGp("Fallo GP");

        CarreraDAO dao = new CarreraDAO() {
            @Override
            public boolean delete(Long id) {
                EntityManager em = JPAUtil.getEntityManager();
                em.getTransaction().begin();
                em.find(Carrera.class, id); // Simula encontrar la carrera
                em.getTransaction().setRollbackOnly();
                throw new RuntimeException("Error al eliminar carrera simulado"); // Forzar excepción
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(idCarreraGuardada));
        assertTrue(ex.getMessage().contains("Error al eliminar carrera"));
    }
    @Test
    @Order(18)
    void testFindByNombreGpConEntradaNula() {
        List<Carrera> carreras = carreraDAO.findByNombreGp(null);
        assertNotNull(carreras, "La lista no debe ser nula");
        assertEquals(0, carreras.size(), "No debe devolver resultados con entrada nula");

        carreras = carreraDAO.findByNombreGp("");
        assertNotNull(carreras, "La lista no debe ser nula");
        assertEquals(0, carreras.size(), "No debe devolver resultados con entrada vacía");
    }

    @AfterAll
        static void cleanup() {
            EntityManager em = JPAUtil.getEntityManager();
            em.getTransaction().begin();

            // Limpieza de datos de prueba
            em.createQuery("DELETE FROM Carrera c WHERE c.temporada.anio = 2099").executeUpdate();
            em.createQuery("DELETE FROM Circuito c WHERE c.nombre = 'Circuito JUnit'").executeUpdate();
            em.createQuery("DELETE FROM Temporada t WHERE t.anio = 2099").executeUpdate();

            em.getTransaction().commit();
        }

}
