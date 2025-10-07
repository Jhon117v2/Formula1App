package co.com.dao;

import co.com.model.Carrera;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar operaciones CRUD de la entidad Carrera.
 */
public class CarreraDAO {
    private static final Logger logger = LoggerFactory.getLogger(CarreraDAO.class);

    /**
     * Obtiene todas las carreras ordenadas por fecha.
     *
     * @return Lista de todas las carreras
     */
    public List<Carrera> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Carrera> query = em.createQuery(
                    "SELECT c FROM Carrera c ORDER BY c.fecha",
                    Carrera.class
            );
            List<Carrera> carreras = query.getResultList();
            logger.info("Se obtuvieron {} carreras", carreras.size());
            return carreras;
        } catch (Exception e) {
            logger.error("Error al listar carreras", e);
            throw new RuntimeException("Error al obtener carreras", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene las carreras de una temporada específica ordenadas por número de GP.
     *
     * @param anio Año de la temporada
     * @return Lista de carreras de esa temporada
     */
    public List<Carrera> findByTemporada(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Carrera> query = em.createQuery(
                    "SELECT c FROM Carrera c JOIN FETCH c.circuito JOIN c.temporada t WHERE t.anio = :anio ORDER BY c.gpNumero",
                    Carrera.class
            );
            query.setParameter("anio", anio);
            List<Carrera> carreras = query.getResultList();
            logger.info("Se obtuvieron {} carreras para la temporada {}", carreras.size(), anio);
            return carreras;
        } catch (Exception e) {
            logger.error("Error al listar carreras por temporada: " + anio, e);
            throw new RuntimeException("Error al obtener carreras de la temporada", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca una carrera por su ID.
     *
     * @param id ID de la carrera
     * @return Optional con la carrera si existe
     */
    public Optional<Carrera> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Carrera> query = em.createQuery(
                    "SELECT c FROM Carrera c JOIN FETCH c.temporada JOIN FETCH c.circuito WHERE c.id = :id",
                    Carrera.class
            );
            query.setParameter("id", id);
            Carrera carrera = query.getSingleResult();
            logger.debug("Carrera encontrada con ID {}: {}", id, carrera.getNombreGp());
            return Optional.of(carrera);
        } catch (NoResultException e) {
            logger.debug("No se encontró carrera con ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error al buscar carrera por ID: " + id, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca carreras por nombre de GP (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre del GP
     * @return Lista de carreras que coinciden
     */
    public List<Carrera> findByNombreGp(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Carrera> query = em.createQuery(
                    "SELECT c FROM Carrera c WHERE LOWER(c.nombreGp) LIKE LOWER(:nombre) ORDER BY c.fecha",
                    Carrera.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar carreras por nombre: " + nombre, e);
            throw new RuntimeException("Error al buscar carreras por nombre", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda una nueva carrera en la base de datos.
     *
     * @param carrera Carrera a guardar
     * @return Carrera guardada con ID asignado
     */
    public Carrera save(Carrera carrera) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(carrera);
            em.getTransaction().commit();
            logger.info("Carrera guardada exitosamente: {} (ID: {})",
                    carrera.getNombreGp(), carrera.getId());
            return carrera;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar carrera: " + carrera.getNombreGp(), e);
            throw new RuntimeException("Error al guardar carrera", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Actualiza una carrera existente.
     *
     * @param carrera Carrera con datos actualizados
     * @return Carrera actualizada
     */
    public Carrera update(Carrera carrera) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrera updated = em.merge(carrera);
            em.getTransaction().commit();
            logger.info("Carrera actualizada exitosamente: {} (ID: {})",
                    updated.getNombreGp(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar carrera: " + carrera.getNombreGp(), e);
            throw new RuntimeException("Error al actualizar carrera", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina una carrera por su ID.
     *
     * @param id ID de la carrera a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrera carrera = em.find(Carrera.class, id);
            if (carrera != null) {
                em.remove(carrera);
                em.getTransaction().commit();
                logger.info("Carrera eliminada: {} (ID: {})", carrera.getNombreGp(), id);
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró carrera con ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar carrera con ID: " + id, e);
            throw new RuntimeException("Error al eliminar carrera", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Cuenta el total de carreras en la base de datos.
     *
     * @return Número total de carreras
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Carrera c",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error al contar carreras", e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }
}
