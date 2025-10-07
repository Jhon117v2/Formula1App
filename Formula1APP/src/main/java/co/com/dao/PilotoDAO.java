package co.com.dao;

import co.com.model.Piloto;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar operaciones CRUD de la entidad Piloto.
 */
public class PilotoDAO {
    private static final Logger logger = LoggerFactory.getLogger(PilotoDAO.class);

    /**
     * Obtiene todos los pilotos ordenados por nombre.
     * Utiliza LEFT JOIN FETCH para cargar el constructor asociado.
     *
     * @return Lista de todos los pilotos
     */
    public List<Piloto> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT DISTINCT p FROM Piloto p LEFT JOIN FETCH p.constructor ORDER BY p.nombre",
                    Piloto.class
            );
            List<Piloto> pilotos = query.getResultList();
            logger.info("Se obtuvieron {} pilotos", pilotos.size());
            return pilotos;
        } catch (Exception e) {
            logger.error("Error al listar pilotos", e);
            throw new RuntimeException("Error al obtener pilotos", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un piloto por su ID.
     *
     * @param id ID del piloto
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT p FROM Piloto p LEFT JOIN FETCH p.constructor WHERE p.id = :id",
                    Piloto.class
            );
            query.setParameter("id", id);
            Optional<Piloto> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Piloto encontrado con ID {}: {}", id, result.get().getNombre());
            } else {
                logger.debug("No se encontró piloto con ID: {}", id);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar piloto por ID: " + id, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un piloto por nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre del piloto
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findByNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT p FROM Piloto p LEFT JOIN FETCH p.constructor " +
                            "WHERE LOWER(p.nombre) LIKE LOWER(:nombre)",
                    Piloto.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            query.setMaxResults(1);
            Optional<Piloto> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Piloto encontrado: {}", result.get().getNombre());
            } else {
                logger.debug("No se encontró piloto con nombre: {}", nombre);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar piloto por nombre: " + nombre, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca pilotos por nacionalidad.
     *
     * @param nacionalidad Nacionalidad del piloto
     * @return Lista de pilotos con esa nacionalidad
     */
    public List<Piloto> findByNacionalidad(String nacionalidad) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT p FROM Piloto p LEFT JOIN FETCH p.constructor " +
                            "WHERE LOWER(p.nacionalidad) = LOWER(:nacionalidad) ORDER BY p.nombre",
                    Piloto.class
            );
            query.setParameter("nacionalidad", nacionalidad);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar pilotos por nacionalidad: " + nacionalidad, e);
            throw new RuntimeException("Error al buscar pilotos por nacionalidad", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca pilotos por constructor/equipo.
     *
     * @param constructorId ID del constructor
     * @return Lista de pilotos del constructor
     */
    public List<Piloto> findByConstructor(Long constructorId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT p FROM Piloto p WHERE p.constructor.id = :constructorId ORDER BY p.nombre",
                    Piloto.class
            );
            query.setParameter("constructorId", constructorId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar pilotos por constructor ID: " + constructorId, e);
            throw new RuntimeException("Error al buscar pilotos por constructor", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un piloto por su dorsal.
     *
     * @param dorsal Número de dorsal
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findByDorsal(String dorsal) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Piloto> query = em.createQuery(
                    "SELECT p FROM Piloto p LEFT JOIN FETCH p.constructor WHERE p.dorsal = :dorsal",
                    Piloto.class
            );
            query.setParameter("dorsal", dorsal);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            logger.error("Error al buscar piloto por dorsal: " + dorsal, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda un nuevo piloto en la base de datos.
     *
     * @param piloto Piloto a guardar
     * @return Piloto guardado con ID asignado
     */
    public Piloto save(Piloto piloto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(piloto);
            em.getTransaction().commit();
            logger.info("Piloto guardado exitosamente: {} (ID: {})", piloto.getNombre(), piloto.getId());
            return piloto;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar piloto: " + piloto.getNombre(), e);
            throw new RuntimeException("Error al guardar piloto", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Actualiza un piloto existente.
     *
     * @param piloto Piloto con datos actualizados
     * @return Piloto actualizado
     */
    public Piloto update(Piloto piloto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Piloto updated = em.merge(piloto);
            em.getTransaction().commit();
            logger.info("Piloto actualizado exitosamente: {} (ID: {})", updated.getNombre(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar piloto: " + piloto.getNombre(), e);
            throw new RuntimeException("Error al actualizar piloto", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina un piloto por su ID.
     *
     * @param id ID del piloto a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Piloto piloto = em.find(Piloto.class, id);
            if (piloto != null) {
                em.remove(piloto);
                em.getTransaction().commit();
                logger.info("Piloto eliminado: {} (ID: {})", piloto.getNombre(), id);
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró piloto con ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar piloto con ID: " + id, e);
            throw new RuntimeException("Error al eliminar piloto", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Cuenta el total de pilotos en la base de datos.
     *
     * @return Número total de pilotos
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Piloto p",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error al contar pilotos", e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }
}
