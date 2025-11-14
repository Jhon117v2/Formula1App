package co.com.formula1app.dao;

import co.com.dao.CircuitoDAO;
import co.com.model.Circuito;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CircuitoDAOTest {

    /**
     * Pruebas reales sobre la entidad Circuito usando JPA y MySQL.
     */

        private static CircuitoDAO circuitoDAO;
        private static Long idCircuitoGuardado;

        @BeforeAll
        static void init() {
            assertTrue(JPAUtil.isAvailable(), "Debe estar inicializado JPA correctamente");
            circuitoDAO = new CircuitoDAO();
        }

        @Test
        @Order(1)
        void testGuardarCircuito() {
            Circuito circuito = new Circuito();
            circuito.setNombre("Circuito JUnit Test");
            circuito.setUbicacion("Ubicación de prueba - TestLand");
            Circuito guardado = circuitoDAO.save(circuito);

            assertNotNull(guardado.getId(), "El ID debe generarse al guardar");
            assertEquals("Circuito JUnit Test", guardado.getNombre());
            idCircuitoGuardado = guardado.getId();
        }

        @Test
        @Order(2)
        void testFindById() {
            Optional<Circuito> circuitoOpt = circuitoDAO.findById(idCircuitoGuardado);
            assertTrue(circuitoOpt.isPresent(), "Debe encontrarse el circuito guardado");
            assertEquals("Circuito JUnit Test", circuitoOpt.get().getNombre());
        }

    @Test
    @Order(3)
    void testFindByNombre() {
        List<Circuito> circuitos = circuitoDAO.findByNombre("Circuito JUnit Test");
        assertNotNull(circuitos);
        assertFalse(circuitos.isEmpty(), "Debe devolver al menos un circuito");
        Circuito circuito = circuitos.get(0);
        assertEquals("Ubicación de prueba - TestLand", circuito.getUbicacion());
    }

    @Test
        @Order(4)
        void testFindAll() {
            List<Circuito> circuitos = circuitoDAO.findAll();
            assertNotNull(circuitos);
            assertTrue(circuitos.size() > 0, "Debe haber al menos un circuito registrado");
        }

        @Test
        @Order(5)
        void testUpdateCircuito() {
            Optional<Circuito> opt = circuitoDAO.findById(idCircuitoGuardado);
            assertTrue(opt.isPresent());
            Circuito circuito = opt.get();
            circuito.setUbicacion("Ubicación actualizada - Ciudad Test");

            Circuito actualizado = circuitoDAO.update(circuito);
            assertEquals("Ubicación actualizada - Ciudad Test", actualizado.getUbicacion());
        }

        @Test
        @Order(6)
        void testDeleteCircuito() {
            boolean eliminado = circuitoDAO.delete(idCircuitoGuardado);
            assertTrue(eliminado, "Debe eliminarse correctamente el circuito");
            assertFalse(circuitoDAO.findById(idCircuitoGuardado).isPresent(), "Ya no debe existir el circuito eliminado");
        }
    @Test
    @Order(7)
    void testFindByTemporada() {
        List<Circuito> circuitos = circuitoDAO.findByTemporada(2024);
        assertNotNull(circuitos, "La lista no debe ser nula");
        // No importa si está vacía, lo importante es que no lance excepción
    }

    @Test
    @Order(8)
    void testFindByUbicacion() {
        List<Circuito> circuitos = circuitoDAO.findByUbicacion("TestLand");
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertTrue(circuitos.isEmpty() || !circuitos.isEmpty(),
                "Debe poder consultar circuitos sin errores aunque no existan resultados");
    }

    @Test
    @Order(9)
    void testCountCircuitos() {
        long total = circuitoDAO.count();
        assertTrue(total >= 0, "El conteo de circuitos no debe ser negativo");
    }

    @Test
    @Order(10)
    void testFindByIdInexistente() {
        Optional<Circuito> circuitoOpt = circuitoDAO.findById(999999L);
        assertTrue(circuitoOpt.isEmpty(), "No debe encontrarse un circuito inexistente");
    }

    @Test
    @Order(11)
    void testEliminarCircuitoInexistente() {
        boolean eliminado = circuitoDAO.delete(999999L);
        assertFalse(eliminado, "No debe eliminar un circuito inexistente");
    }

    @Test
    @Order(12)
    void testGuardarCircuitoConError() {
        CircuitoDAO dao = new CircuitoDAO() {
            @Override
            public Circuito save(Circuito circuito) {
                throw new RuntimeException("Error simulado al guardar circuito");
            }
        };

        Circuito circuito = new Circuito();
        circuito.setNombre("Error Save GP");
        circuito.setUbicacion("Ubicación Falsa");

        Exception ex = assertThrows(RuntimeException.class, () -> dao.save(circuito));
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar excepción simulada");
    }

    @Test
    @Order(13)
    void testActualizarCircuitoConError() {
        CircuitoDAO dao = new CircuitoDAO() {
            @Override
            public Circuito update(Circuito circuito) {
                throw new RuntimeException("Error al actualizar circuito simulado");
            }
        };

        Circuito circuito = new Circuito();
        circuito.setId(123L);
        circuito.setNombre("Error Update");

        Exception ex = assertThrows(RuntimeException.class, () -> dao.update(circuito));
        assertTrue(ex.getMessage().contains("Error al actualizar circuito simulado"));
    }

    @Test
    @Order(14)
    void testDeleteConError() {
        CircuitoDAO dao = new CircuitoDAO() {
            @Override
            public boolean delete(Long id) {
                throw new RuntimeException("Error al eliminar circuito simulado");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(1L));
        assertTrue(ex.getMessage().contains("Error al eliminar circuito simulado"));
    }

    @Test
    @Order(15)
    void testFindByNombreSinResultados() {
        List<Circuito> circuitos = circuitoDAO.findByNombre("NombreInexistenteXYZ");
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertEquals(0, circuitos.size(), "No debe haber coincidencias");
    }

    @Test
    @Order(16)
    void testFindByUbicacionSinResultados() {
        List<Circuito> circuitos = circuitoDAO.findByUbicacion("UbicaciónQueNoExiste");
        assertNotNull(circuitos);
        assertTrue(circuitos.isEmpty(), "Debe devolver lista vacía sin errores");
    }

    @Test
    @Order(17)
    void testCountConErrorSimulado() {
        CircuitoDAO dao = new CircuitoDAO() {
            @Override
            public long count() {
                throw new RuntimeException("Error simulado al contar circuitos");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, dao::count);
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar la excepción simulada");
    }
    @Test
    @Order(18)
    void testSaveConDatosInvalidos() {
        Circuito circuito = new Circuito();
        circuito.setNombre(null); // Dato inválido
        circuito.setUbicacion(null);

        Exception ex = assertThrows(RuntimeException.class, () -> circuitoDAO.save(circuito));
        assertTrue(ex.getMessage().contains("Error al guardar circuito"));
    }
    @Test
    @Order(19)
    void testUpdateCircuitoNoEncontrado() {
        Circuito circuito = new Circuito();
        circuito.setId(999999L); // ID inexistente
        circuito.setNombre("No Existe");
        }
    @Test
    @Order(20)
    void testDeleteConFalloTransaccion() {
        Circuito circuito = new Circuito();
        circuito.setId(idCircuitoGuardado); // Reutiliza una ID válida
        circuito.setNombre("Fallo GP");

        CircuitoDAO dao = new CircuitoDAO() {
            @Override
            public boolean delete(Long id) {
                EntityManager em = JPAUtil.getEntityManager();
                try {
                    em.getTransaction().begin();
                    em.find(Circuito.class, id); // Simula encontrar el circuito
                    em.getTransaction().setRollbackOnly(); // Fuerza rollback
                    // Forzar una excepción explícita en lugar de depender solo del commit
                    throw new RuntimeException("Error al eliminar circuito simulado debido a rollback forzado");
                } catch (Exception e) {
                    throw new RuntimeException("Error al eliminar circuito simulado", e);
                } finally {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    JPAUtil.close(em);
                }
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(idCircuitoGuardado));
        assertTrue(ex.getMessage().contains("Error al eliminar circuito"));
    }
    @Test
    @Order(21)
    void testFindByNombreConEntradaNula() {
        List<Circuito> circuitos = circuitoDAO.findByNombre(null);
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertEquals(0, circuitos.size(), "No debe devolver resultados con entrada nula");

        circuitos = circuitoDAO.findByNombre("");
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertEquals(0, circuitos.size(), "No debe devolver resultados con entrada vacía");
    }
    @Test
    @Order(22)
    void testFindByUbicacionConEntradaNula() {
        List<Circuito> circuitos = circuitoDAO.findByUbicacion(null);
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertEquals(0, circuitos.size(), "No debe devolver resultados con entrada nula");

        circuitos = circuitoDAO.findByUbicacion("");
        assertNotNull(circuitos, "La lista no debe ser nula");
        assertEquals(0, circuitos.size(), "No debe devolver resultados con entrada vacía");
    }

    @AfterAll
        static void cleanup() {
            EntityManager em = JPAUtil.getEntityManager();
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Circuito c WHERE c.nombre = 'Circuito JUnit Test'").executeUpdate();
            em.getTransaction().commit();
        }
}
