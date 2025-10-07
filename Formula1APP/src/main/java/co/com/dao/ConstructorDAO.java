package co.com.dao;

import co.com.model.Constructor;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar operaciones CRUD de la entidad Constructor.
 */
public class ConstructorDAO {
    private static final Logger logger = LoggerFactory.getLogger(ConstructorDAO.class);

    /**
     * Obtiene todos los constructores ordenados por nombre.
     * Carga también la lista de pilotos asociados.
     *
     * @return Lista de todos los constructores
     */
    public List<Constructor> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Constructor> query = em.createQuery(
                    "SELECT DISTINCT c FROM Constructor c LEFT JOIN FETCH c.pilotos ORDER BY c.nombre",
                    Constructor.class
            );
            List<Constructor> constructores = query.getResultList();
            logger.info("Se obtuvieron {} constructores", constructores.size());
            return constructores;
        } catch (Exception e) {
            logger.error("Error al listar constructores", e);
            throw new RuntimeException("Error al obtener constructores", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un constructor por su ID.
     *
     * @param id ID del constructor
     * @return Optional con el constructor si existe
     */
    public Optional<Constructor> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Constructor> query = em.createQuery(
                    "SELECT c FROM Constructor c LEFT JOIN FETCH c.pilotos WHERE c.id = :id",
                    Constructor.class
            );
            query.setParameter("id", id);
            Optional<Constructor> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Constructor encontrado con ID {}: {}", id, result.get().getNombre());
            } else {
                logger.debug("No se encontró constructor con ID: {}", id);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar constructor por ID: " + id, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un constructor por nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre del constructor
     * @return Optional con el constructor si existe
     */
    public Optional<Constructor> findByNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Constructor> query = em.createQuery(
                    "SELECT c FROM Constructor c LEFT JOIN FETCH c.pilotos " +
                            "WHERE LOWER(c.nombre) LIKE LOWER(:nombre)",
                    Constructor.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            query.setMaxResults(1);
            Optional<Constructor> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Constructor encontrado: {}", result.get().getNombre());
            } else {
                logger.debug("No se encontró constructor con nombre: {}", nombre);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar constructor por nombre: " + nombre, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca constructores por nacionalidad.
     *
     * @param nacionalidad Nacionalidad del constructor
     * @return Lista de constructores con esa nacionalidad
     */
    public List<Constructor> findByNacionalidad(String nacionalidad) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Constructor> query = em.createQuery(
                    "SELECT DISTINCT c FROM Constructor c LEFT JOIN FETCH c.pilotos " +
                            "WHERE LOWER(c.nacionalidad) = LOWER(:nacionalidad) ORDER BY c.nombre",
                    Constructor.class
            );
            query.setParameter("nacionalidad", nacionalidad);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar constructores por nacionalidad: " + nacionalidad, e);
            throw new RuntimeException("Error al buscar constructores por nacionalidad", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda un nuevo constructor en la base de datos.
     *
     * @param constructor Constructor a guardar
     * @return Constructor guardado con ID asignado
     */
    public Constructor save(Constructor constructor) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(constructor);
            em.getTransaction().commit();
            logger.info("Constructor guardado exitosamente: {} (ID: {})",
                    constructor.getNombre(), constructor.getId());
            return constructor;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar constructor: " + constructor.getNombre(), e);
            throw new RuntimeException("Error al guardar constructor", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Actualiza un constructor existente.
     *
     * @param constructor Constructor con datos actualizados
     * @return Constructor actualizado
     */
    public Constructor update(Constructor constructor) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Constructor updated = em.merge(constructor);
            em.getTransaction().commit();
            logger.info("Constructor actualizado exitosamente: {} (ID: {})",
                    updated.getNombre(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar constructor: " + constructor.getNombre(), e);
            throw new RuntimeException("Error al actualizar constructor", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina un constructor por su ID.
     * NOTA: Puede fallar si hay pilotos asociados (violación de integridad referencial).
     *
     * @param id ID del constructor a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Constructor constructor = em.find(Constructor.class, id);
            if (constructor != null) {
                em.remove(constructor);
                em.getTransaction().commit();
                logger.info("Constructor eliminado: {} (ID: {})", constructor.getNombre(), id);
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró constructor con ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar constructor con ID: " + id, e);
            throw new RuntimeException("Error al eliminar constructor. Puede tener pilotos asociados.", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Cuenta el total de constructores en la base de datos.
     *
     * @return Número total de constructores
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Constructor c",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error al contar constructores", e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }
}
