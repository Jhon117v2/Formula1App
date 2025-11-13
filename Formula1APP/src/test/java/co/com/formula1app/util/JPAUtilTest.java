package co.com.formula1app.util;

import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPAUtilTest {

    private static EntityManager em;

    @BeforeAll
    static void init() {
        // Verificamos que la fábrica esté disponible antes de pedir un EntityManager
        assertTrue(JPAUtil.isAvailable(), "El EntityManagerFactory debería estar disponible");
        em = JPAUtil.getEntityManager();
        assertNotNull(em, "El EntityManager no debería ser nulo");
        assertTrue(em.isOpen(), "El EntityManager debería estar abierto");
    }

    @Test
    @Order(1)
    void testEntityManagerFactoryDisponible() {
        assertTrue(JPAUtil.isAvailable(), "El EMF debe estar inicializado y abierto");
    }

    @Test
    @Order(2)
    void testCrearYUsarEntityManager() {
        EntityManager nuevoEm = JPAUtil.getEntityManager();
        assertNotNull(nuevoEm, "El nuevo EntityManager no debe ser nulo");
        assertTrue(nuevoEm.isOpen(), "El nuevo EntityManager debe estar abierto");
        JPAUtil.close(nuevoEm);
        assertFalse(nuevoEm.isOpen(), "El EntityManager debería cerrarse correctamente");
    }

    @Test
    @Order(3)
    void testExecuteInTransactionCommit() {
        assertDoesNotThrow(() -> JPAUtil.executeInTransaction(entityManager -> {
            assertNotNull(entityManager, "EntityManager dentro de la transacción no debe ser nulo");
            assertTrue(entityManager.isOpen(), "EntityManager dentro de la transacción debe estar abierto");
        }));
    }

    @Test
    @Order(4)
    void testExecuteInTransactionRollback() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            JPAUtil.executeInTransaction(entityManager -> {
                throw new IllegalStateException("Error simulado");
            });
        }, "Debe lanzarse RuntimeException envolviendo el error interno");

        assertTrue(ex.getMessage().contains("Error al ejecutar transacción"),
                "El mensaje de la excepción debe indicar 'Error al ejecutar transacción'");
    }

    @AfterAll
    static void cleanup() {
        // Cerramos el EntityManager creado en @BeforeAll si sigue abierto
        if (em != null && em.isOpen()) {
            JPAUtil.close(em);
            assertFalse(em.isOpen(), "El EntityManager debería estar cerrado");
        }

        // IMPORTANTE: NO cerramos aquí el EntityManagerFactory (JPAUtil.closeEntityManagerFactory())
        // porque las demás pruebas de la suite necesitan usarlo. Si cierras la fábrica aquí,
        // las pruebas posteriores fallarán con "EntityManagerFactory no está disponible".
        System.out.println("🧹 Limpieza de JPAUtilTest completada (sin cerrar EMF).");
    }
}
