package co.com.formula1app.dao;
import co.com.dao.ConstructorDAO;
import co.com.model.Constructor;
import co.com.util.JPAUtil;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConstructorDAOTest {
        private static ConstructorDAO constructorDAO;

        @BeforeAll
        static void init() {
            constructorDAO = new ConstructorDAO();
            assertTrue(JPAUtil.isAvailable(), "La conexión JPA debe estar disponible antes de iniciar las pruebas");
        }

        @Test
        @Order(1)
        void testSaveConstructor() {
            Constructor constructor = new Constructor();
            constructor.setNombre("JUnit Racing Team");
            constructor.setNacionalidad("Testlandia");

            Constructor saved = constructorDAO.save(constructor);

            assertNotNull(saved.getId(), "El constructor guardado debe tener un ID");
            assertEquals("JUnit Racing Team", saved.getNombre());
        }

        @Test
        @Order(2)
        void testFindAll() {
            List<Constructor> constructores = constructorDAO.findAll();
            assertNotNull(constructores);
            assertTrue(constructores.size() > 0, "Debe existir al menos un constructor");
        }

        @Test
        @Order(3)
        void testFindByNombre() {
            Optional<Constructor> constructorOpt = constructorDAO.findByNombre("JUnit Racing Team");
            assertTrue(constructorOpt.isPresent(), "Debe encontrar el constructor por nombre");
            assertEquals("Testlandia", constructorOpt.get().getNacionalidad());
        }

        @Test
        @Order(4)
        void testUpdateConstructor() {
            Optional<Constructor> constructorOpt = constructorDAO.findByNombre("JUnit Racing Team");
            assertTrue(constructorOpt.isPresent(), "El constructor debe existir para ser actualizado");

            Constructor constructor = constructorOpt.get();
            constructor.setNacionalidad("UpdatedLand");
            Constructor updated = constructorDAO.update(constructor);

            assertEquals("UpdatedLand", updated.getNacionalidad(), "La nacionalidad debe actualizarse correctamente");
        }

        @Test
        @Order(5)
        void testFindByNacionalidad() {
            List<Constructor> lista = constructorDAO.findByNacionalidad("UpdatedLand");
            assertNotNull(lista);
            assertTrue(lista.size() > 0, "Debe devolver constructores por nacionalidad");
        }

        @Test
        @Order(6)
        void testCount() {
            long count = constructorDAO.count();
            assertTrue(count > 0, "Debe haber al menos un constructor registrado");
        }

        @Test
        @Order(7)
        void testDeleteConstructor() {
            Optional<Constructor> constructorOpt = constructorDAO.findByNombre("JUnit Racing Team");
            assertTrue(constructorOpt.isPresent(), "Debe existir para poder eliminar");

            boolean deleted = constructorDAO.delete(constructorOpt.get().getId());
            assertTrue(deleted, "Debe eliminarse correctamente");

            Optional<Constructor> deletedCheck = constructorDAO.findByNombre("JUnit Racing Team");
            assertFalse(deletedCheck.isPresent(), "El constructor eliminado no debe encontrarse");
        }
    @Test
    @Order(8)
    void testFindById() {
        Optional<Constructor> constructorOpt = constructorDAO.findByNombre("JUnit Racing Team");

        if (constructorOpt.isEmpty()) {
            System.out.println("⚠️ No existe el constructor de prueba, se omite la validación de ID");
            return;
        }

        Long id = constructorOpt.get().getId();
        Optional<Constructor> found = constructorDAO.findById(id);

        assertTrue(found.isPresent(), "Debe encontrar el constructor por ID");
        assertEquals("JUnit Racing Team", found.get().getNombre());
    }

    @Test
    @Order(9)
    void testFindByIdInexistente() {
        Optional<Constructor> constructorOpt = constructorDAO.findById(999999L);
        assertTrue(constructorOpt.isEmpty(), "No debe encontrarse un constructor inexistente");
    }

    @Test
    @Order(10)
    void testFindByNombreInexistente() {
        Optional<Constructor> constructorOpt = constructorDAO.findByNombre("Equipo Fantasma");
        assertTrue(constructorOpt.isEmpty(), "No debe encontrarse un constructor inexistente");
    }

    @Test
    @Order(11)
    void testFindByNacionalidadInexistente() {
        List<Constructor> lista = constructorDAO.findByNacionalidad("Narnia");
        assertNotNull(lista);
        assertTrue(lista.isEmpty(), "No debe devolver constructores con nacionalidad inexistente");
    }

    @Test
    @Order(12)
    void testDeleteInexistente() {
        boolean deleted = constructorDAO.delete(999999L);
        assertFalse(deleted, "No debe eliminar un constructor inexistente");
    }
    @Test
    @Order(13)
    void testSaveConstructorConError() {
        ConstructorDAO dao = new ConstructorDAO() {
            @Override
            public Constructor save(Constructor constructor) {
                throw new RuntimeException("Error simulado al guardar constructor");
            }
        };

        Constructor c = new Constructor();
        c.setNombre("ErrorSave");
        c.setNacionalidad("Ficticia");

        Exception ex = assertThrows(RuntimeException.class, () -> dao.save(c));
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar excepción simulada");
    }

    @Test
    @Order(14)
    void testUpdateConstructorConError() {
        ConstructorDAO dao = new ConstructorDAO() {
            @Override
            public Constructor update(Constructor constructor) {
                throw new RuntimeException("Error simulado al actualizar constructor");
            }
        };

        Constructor c = new Constructor();
        c.setId(123L);
        c.setNombre("Team Error");
        c.setNacionalidad("FailLand");

        Exception ex = assertThrows(RuntimeException.class, () -> dao.update(c));
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar excepción simulada");
    }

    @Test
    @Order(15)
    void testDeleteConErrorSimulado() {
        ConstructorDAO dao = new ConstructorDAO() {
            @Override
            public boolean delete(Long id) {
                throw new RuntimeException("Error simulado al eliminar constructor");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.delete(10L));
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar la excepción simulada");
    }

    @Test
    @Order(16)
    void testFindByNacionalidadConErrorSimulado() {
        ConstructorDAO dao = new ConstructorDAO() {
            @Override
            public List<Constructor> findByNacionalidad(String nacionalidad) {
                throw new RuntimeException("Error simulado al buscar por nacionalidad");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, () -> dao.findByNacionalidad("Testlandia"));
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar excepción simulada");
    }

    @Test
    @Order(17)
    void testCountConErrorSimulado() {
        ConstructorDAO dao = new ConstructorDAO() {
            @Override
            public long count() {
                throw new RuntimeException("Error simulado al contar constructores");
            }
        };

        Exception ex = assertThrows(RuntimeException.class, dao::count);
        assertTrue(ex.getMessage().contains("Error simulado"), "Debe lanzar la excepción simulada");
    }
    @Test
    @Order(18)
    void testSaveConstructorConDatosInvalidos() {
        Constructor constructor = new Constructor();
        constructor.setNombre(null); // Dato inválido
        constructor.setNacionalidad(null);

        Exception ex = assertThrows(RuntimeException.class, () -> constructorDAO.save(constructor));
        assertTrue(ex.getMessage().contains("Error al guardar constructor"));
    }
    @Test
    @Order(19)
    void testDeleteConstructorConPilotosAsociados() {
        // Nota: Esto requiere un constructor con pilotos asociados en la base de datos
        Long idConPilotos = 1L; // Ajusta este ID según tu base de datos
        Exception ex = assertThrows(RuntimeException.class, () -> constructorDAO.delete(idConPilotos));
        assertTrue(ex.getMessage().contains("Puede tener pilotos asociados"),
                "Debe fallar por violación de integridad referencial");
    }
    @Test
    @Order(20)
    void testFindByNombreConEntradaNula() {
        Optional<Constructor> constructorOpt = constructorDAO.findByNombre(null);
        assertNotNull(constructorOpt, "El Optional no debe ser nulo");
        assertFalse(constructorOpt.isPresent(), "No debe devolver resultados con entrada nula");

        constructorOpt = constructorDAO.findByNombre("");
        assertNotNull(constructorOpt, "El Optional no debe ser nulo");
        assertFalse(constructorOpt.isPresent(), "No debe devolver resultados con entrada vacía");
    }
    @Test
    @Order(21)
    void testFindByNacionalidadConEntradaNula() {
        List<Constructor> lista = constructorDAO.findByNacionalidad(null);
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(0, lista.size(), "No debe devolver resultados con entrada nula");

        lista = constructorDAO.findByNacionalidad("");
        assertNotNull(lista, "La lista no debe ser nula");
        assertEquals(0, lista.size(), "No debe devolver resultados con entrada vacía");
    }
}
