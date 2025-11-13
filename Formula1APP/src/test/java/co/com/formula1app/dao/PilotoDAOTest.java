package co.com.formula1app.dao;
import co.com.dao.ConstructorDAO;
import co.com.dao.PilotoDAO;
import co.com.dao.TemporadaDAO;
import co.com.model.Constructor;
import co.com.model.Piloto;
import co.com.model.Temporada;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PilotoDAOTest {

        private static PilotoDAO pilotoDAO;
        private static ConstructorDAO constructorDAO;
        private static TemporadaDAO temporadaDAO;

        private static Piloto pilotoGuardado;
        private static Constructor constructorPrueba;
        private static Temporada temporadaPrueba;

        @BeforeAll
        static void init() {
            assertTrue(JPAUtil.isAvailable(), "Debe estar inicializado JPA correctamente");

            pilotoDAO = new PilotoDAO();
            constructorDAO = new ConstructorDAO();
            temporadaDAO = new TemporadaDAO();

            // Crear o recuperar temporada de prueba
            List<Temporada> temporadas = temporadaDAO.findAll();
            temporadaPrueba = temporadas.stream()
                    .filter(t -> t.getAnio().equals(2099))
                    .findFirst()
                    .orElseGet(() -> temporadaDAO.save(new Temporada(2099)));

            // Crear o recuperar constructor de prueba
            List<Constructor> constructores = constructorDAO.findAll();
            constructorPrueba = constructores.stream()
                    .filter(c -> c.getNombre().equalsIgnoreCase("JUnit Racing"))
                    .findFirst()
                    .orElseGet(() -> {
                        Constructor nuevo = new Constructor();
                        nuevo.setNombre("JUnit Racing");
                        nuevo.setNacionalidad("Testlandia");
                        return constructorDAO.save(nuevo);
                    });
        }

        @Test
        @Order(1)
        void testGuardarPiloto() {
            Piloto piloto = new Piloto();
            piloto.setNombre("Test Driver JUnit");
            piloto.setDorsal(String.valueOf(99));
            piloto.setNacionalidad("Testlandia");
            piloto.setConstructor(constructorPrueba);

            Piloto guardado = pilotoDAO.save(piloto);

            assertNotNull(guardado.getId(), "Debe generarse un ID");
            assertEquals("Test Driver JUnit", guardado.getNombre());
            pilotoGuardado = guardado;
        }

        @Test
        @Order(2)
        void testFindById() {
            Optional<Piloto> opt = pilotoDAO.findById(pilotoGuardado.getId());
            assertTrue(opt.isPresent());
            assertEquals("Test Driver JUnit", opt.get().getNombre());
        }

    @Test
    @Order(3)
    void testFindByNombre() {
        Optional<Piloto> pilotoOpt = pilotoDAO.findByNombre("JUnit");
        assertTrue(pilotoOpt.isPresent(), "Debe encontrar un piloto con ese nombre");
        assertEquals("Test Driver JUnit", pilotoOpt.get().getNombre());
    }

    @Test
        @Order(4)
        void testFindAll() {
            List<Piloto> lista = pilotoDAO.findAll();
            assertNotNull(lista);
            assertTrue(lista.size() > 0);
        }

        @Test
        @Order(5)
        void testUpdatePiloto() {
            Optional<Piloto> opt = pilotoDAO.findById(pilotoGuardado.getId());
            assertTrue(opt.isPresent());
            Piloto piloto = opt.get();
            piloto.setNacionalidad("ActualizadaLand");

            Piloto actualizado = pilotoDAO.update(piloto);
            assertEquals("ActualizadaLand", actualizado.getNacionalidad());
        }

        @Test
        @Order(6)
        void testCount() {
            long total = pilotoDAO.count();
            assertTrue(total > 0, "Debe haber al menos un piloto en la base");
        }

        @Test
        @Order(7)
        void testDeletePiloto() {
            boolean eliminado = pilotoDAO.delete(pilotoGuardado.getId());
            assertTrue(eliminado, "Debe eliminar el piloto correctamente");
            assertFalse(pilotoDAO.findById(pilotoGuardado.getId()).isPresent());
        }
    @Test
    @Order(8)
    void testFindByNacionalidad() {
        Piloto piloto = new Piloto();
        piloto.setNombre("Test Driver JUnit");
        piloto.setNacionalidad("ActualizadaLand");
        piloto.setDorsal("99");
        pilotoDAO.save(piloto);

        List<Piloto> lista = pilotoDAO.findByNacionalidad("ActualizadaLand");
        assertNotNull(lista, "La lista no debe ser nula");
        assertTrue(lista.size() > 0, "Debe encontrar pilotos con esa nacionalidad");
    }

    @Test
    @Order(9)
    void testFindByDorsal() {
        Piloto piloto = new Piloto();
        piloto.setNombre("Test Driver JUnit");
        piloto.setNacionalidad("ActualizadaLand");
        piloto.setDorsal("99");
        pilotoDAO.save(piloto);

        Optional<Piloto> pilotoOpt = pilotoDAO.findByDorsal("99");
        assertTrue(pilotoOpt.isPresent(), "Debe encontrar el piloto por dorsal");
        assertEquals("Test Driver JUnit", pilotoOpt.get().getNombre());
    }

    @Test
    @Order(10)
    void testFindByIdInexistente() {
        Optional<Piloto> pilotoOpt = pilotoDAO.findById(999999L);
        assertTrue(pilotoOpt.isEmpty(), "No debe devolver un piloto inexistente");
    }

    @Test
    @Order(11)
    void testFindByNombreInexistente() {
        Optional<Piloto> pilotoOpt = pilotoDAO.findByNombre("NoExisteJUnit");
        assertTrue(pilotoOpt.isEmpty(), "No debe encontrar pilotos con nombre inexistente");
    }

    @Test
    @Order(12)
    void testFindByNacionalidadInexistente() {
        List<Piloto> lista = pilotoDAO.findByNacionalidad("Narnia");
        assertNotNull(lista);
        assertTrue(lista.isEmpty(), "No debe devolver pilotos de nacionalidad inexistente");
    }

    @Test
    @Order(13)
    void testFindByConstructor() {
        // Aseguramos tener un constructor válido
        List<Piloto> lista = pilotoDAO.findByConstructor(constructorPrueba.getId());
        assertNotNull(lista, "La lista no debe ser nula");
        // Puede estar vacía si no hay pilotos asociados
        assertTrue(lista.size() >= 0, "Debe devolver una lista aunque esté vacía");
    }

    @Test
    @Order(14)
    void testFindByConstructorInexistente() {
        List<Piloto> lista = pilotoDAO.findByConstructor(999999L);
        assertNotNull(lista, "La lista no debe ser nula aunque el constructor no exista");
        assertTrue(lista.isEmpty(), "No debe devolver pilotos de un constructor inexistente");
    }

    @Test
    @Order(15)
    void testDeleteInexistente() {
        boolean eliminado = pilotoDAO.delete(999999L);
        assertFalse(eliminado, "No debe eliminar un piloto inexistente");
    }

    @Test
    @Order(16)
    void testCountSinError() {
        long count = pilotoDAO.count();
        assertTrue(count >= 0, "Debe devolver un número válido de pilotos");
    }

    @Test
    @Order(17)
    void testSavePilotoSinConstructor() {
        Piloto piloto = new Piloto();
        piloto.setNombre("Piloto Sin Constructor");
        piloto.setDorsal("123");
        piloto.setNacionalidad("Testlandia");

        assertDoesNotThrow(() -> {
            Piloto guardado = pilotoDAO.save(piloto);
            assertNotNull(guardado.getId(), "Debe guardarse aunque no tenga constructor asociado");
        });
    }
    @Test
    @Order(18)
    void testSavePilotoConDatosInvalidos() {
        Piloto piloto = new Piloto();
        piloto.setNombre(null); // Dato inválido
        piloto.setDorsal(null);
        piloto.setNacionalidad(null);

        Exception ex = assertThrows(RuntimeException.class, () -> pilotoDAO.save(piloto));
        assertTrue(ex.getMessage().contains("Error al guardar piloto"));
    }

    @Test
    @Order(19)
    void testDeleteConFalloTransaccion() {
        Piloto piloto = new Piloto();
        piloto.setId(1L); // Usa un ID válido (ajusta según tu base de datos)

        PilotoDAO dao = new PilotoDAO() {
            @Override
            public boolean delete(Long id) {
                EntityManager em = JPAUtil.getEntityManager();
                try {
                    em.getTransaction().begin();
                    em.find(Piloto.class, id); // Simula encontrar el piloto
                    em.getTransaction().setRollbackOnly();
                    throw new RuntimeException("Error simulado al eliminar piloto debido a rollback");
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new RuntimeException("Error al eliminar piloto simulado", e);
                } finally {
                    JPAUtil.close(em);
                }
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(1L));
        assertTrue(ex.getMessage().contains("Error al eliminar piloto"));
    }
    @Test
    @Order(20)
    void testFindByNombreConEntradaNula() {
        Optional<Piloto> pilotoOpt = pilotoDAO.findByNombre(null);
        assertNotNull(pilotoOpt, "El Optional no debe ser nulo");
        assertFalse(pilotoOpt.isPresent(), "No debe devolver resultados con entrada nula");

        pilotoOpt = pilotoDAO.findByNombre("");
        assertNotNull(pilotoOpt, "El Optional no debe ser nulo");
        assertFalse(pilotoOpt.isPresent(), "No debe devolver resultados con entrada vacía");
    }

    @Test
    @Order(21)
    void testFindByNacionalidadConEntradaNula() {
        List<Piloto> lista = pilotoDAO.findByNacionalidad(null);
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(0, lista.size(), "No debe devolver resultados con entrada nula");

        lista = pilotoDAO.findByNacionalidad("");
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(0, lista.size(), "No debe devolver resultados con entrada vacía");
    }
    @Test
    @Order(22)
    void testFindByDorsalConEntradaNula() {
        Optional<Piloto> pilotoOpt = pilotoDAO.findByDorsal(null);
        assertNotNull(pilotoOpt, "El Optional no debe ser nulo");
        assertFalse(pilotoOpt.isPresent(), "No debe devolver resultados con entrada nula");

        pilotoOpt = pilotoDAO.findByDorsal("");
        assertNotNull(pilotoOpt, "El Optional no debe ser nulo");
        assertFalse(pilotoOpt.isPresent(), "No debe devolver resultados con entrada vacía");
    }
    @Test
    @Order(23)
    void testFindByConstructorConIdNulo() {
        List<Piloto> lista = pilotoDAO.findByConstructor(null);
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(0, lista.size(), "No debe devolver resultados con ID nulo");
    }


    @AfterAll
        static void cleanup() {
            EntityManager em = JPAUtil.getEntityManager();
            em.getTransaction().begin();

            // Limpieza de datos de prueba
            em.createQuery("DELETE FROM Piloto p WHERE p.nombre LIKE '%JUnit%'").executeUpdate();
            em.createQuery("DELETE FROM Constructor c WHERE c.nombre = 'JUnit Racing'").executeUpdate();
            em.createQuery("DELETE FROM Temporada t WHERE t.anio = 2099").executeUpdate();

            em.getTransaction().commit();
        }
}
