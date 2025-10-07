package co.com.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para la gestión del EntityManagerFactory y EntityManager de JPA.
 * Implementa el patrón Singleton para el EntityManagerFactory.
 */
public class JPAUtil {
    private static final Logger logger = LoggerFactory.getLogger(JPAUtil.class);
    private static final String PERSISTENCE_UNIT_NAME = "Formula1APP";
    private static EntityManagerFactory emf;

    // Bloque estático para inicializar el EntityManagerFactory
    static {
        try {
            logger.info("Inicializando EntityManagerFactory para unidad de persistencia: {}", PERSISTENCE_UNIT_NAME);

            // Propiedades opcionales que pueden sobrescribir las del persistence.xml
            Map<String, String> properties = new HashMap<>();
            // properties.put("jakarta.persistence.jdbc.url", "jdbc:mysql://localhost:3306/f1_manager");
            // properties.put("jakarta.persistence.jdbc.user", "root");
            // properties.put("jakarta.persistence.jdbc.password", "");

            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);

            logger.info("EntityManagerFactory inicializado correctamente");
            logger.info("Proveedor JPA: {}", emf.getProperties().get("hibernate.dialect"));

            // Registrar shutdown hook para cerrar el EMF al finalizar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Ejecutando shutdown hook para cerrar EntityManagerFactory");
                closeEntityManagerFactory();
            }));

        } catch (Exception e) {
            logger.error("Error crítico al crear EntityManagerFactory", e);
            logger.error("Verifique la configuración en persistence.xml y que MySQL esté ejecutándose");
            throw new ExceptionInInitializerError("Error al inicializar JPA: " + e.getMessage());
        }
    }

    /**
     * Constructor privado para prevenir instanciación
     */
    private JPAUtil() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una nueva instancia de EntityManager.
     * El EntityManager debe ser cerrado después de su uso.
     *
     * @return EntityManager nuevo EntityManager
     * @throws IllegalStateException si el EntityManagerFactory no está disponible
     */
    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            logger.error("EntityManagerFactory no está disponible o ha sido cerrado");
            throw new IllegalStateException("EntityManagerFactory no está disponible. La aplicación no puede continuar.");
        }

        try {
            EntityManager em = emf.createEntityManager();
            logger.debug("EntityManager creado exitosamente");
            return em;
        } catch (Exception e) {
            logger.error("Error al crear EntityManager", e);
            throw new RuntimeException("No se pudo crear EntityManager", e);
        }
    }

    /**
     * Cierra el EntityManagerFactory.
     * Este método debe ser llamado al finalizar la aplicación.
     */
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            try {
                logger.info("Cerrando EntityManagerFactory...");
                emf.close();
                logger.info("EntityManagerFactory cerrado correctamente");
            } catch (Exception e) {
                logger.error("Error al cerrar EntityManagerFactory", e);
            }
        } else {
            logger.debug("EntityManagerFactory ya estaba cerrado o es null");
        }
    }

    /**
     * Cierra un EntityManager de forma segura.
     * Si hay una transacción activa, realiza rollback antes de cerrar.
     *
     * @param em EntityManager a cerrar
     */
    public static void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                // Si hay una transacción activa, hacer rollback
                if (em.getTransaction() != null && em.getTransaction().isActive()) {
                    logger.warn("Transacción activa detectada al cerrar EntityManager. Realizando rollback.");
                    em.getTransaction().rollback();
                }

                em.close();
                logger.debug("EntityManager cerrado correctamente");
            } catch (Exception e) {
                logger.error("Error al cerrar EntityManager", e);
            }
        }
    }

    /**
     * Ejecuta una transacción de forma segura con manejo automático de errores.
     *
     * @param action Acción a ejecutar dentro de la transacción
     * @throws RuntimeException si ocurre un error durante la transacción
     */
    public static void executeInTransaction(TransactionAction action) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            logger.debug("Transacción iniciada");

            action.execute(em);

            em.getTransaction().commit();
            logger.debug("Transacción completada exitosamente");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                logger.error("Error en transacción. Realizando rollback", e);
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al ejecutar transacción", e);
        } finally {
            close(em);
        }
    }

    /**
     * Verifica si el EntityManagerFactory está disponible y abierto.
     *
     * @return true si está disponible, false en caso contrario
     */
    public static boolean isAvailable() {
        return emf != null && emf.isOpen();
    }

    /**
     * Interfaz funcional para ejecutar acciones dentro de una transacción
     */
    @FunctionalInterface
    public interface TransactionAction {
        void execute(EntityManager em) throws Exception;
    }
}
