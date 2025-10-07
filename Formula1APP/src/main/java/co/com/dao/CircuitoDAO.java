package co.com.dao;

import co.com.model.Circuito;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar operaciones CRUD de la entidad Circuito.
 */
public class CircuitoDAO {
    private static final Logger logger = LoggerFactory.getLogger(CircuitoDAO.class);

    /**
     * Obtiene todos los circuitos ordenados por nombre.
     *
     * @return Lista de todos los circuitos
     */
    public List<Circuito> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Circuito> query = em.createQuery(
                    "SELECT c FROM Circuito c ORDER BY c.nombre",
                    Circuito.class
            );
            List<Circuito> circuitos = query.getResultList();
            logger.info("Se obtuvieron {} circuitos", circuitos.size());
            return circuitos;
        } catch (Exception e) {
            logger.error("Error al listar circuitos", e);
            throw new RuntimeException("Error al obtener circuitos", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene los circuitos de una temporada específica ordenados por GP.
     *
     * @param anio Año de la temporada
     * @return Lista de circuitos de esa temporada
     */
    public List<Circuito> findByTemporada(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Circuito> query = em.createQuery(
                    "SELECT DISTINCT ci FROM Circuito ci " +
                            "JOIN ci.carreras ca " +
                            "JOIN ca.temporada t " +
                            "WHERE t.anio = :anio " +
                            "ORDER BY ca.gpNumero",
                    Circuito.class
            );
            query.setParameter("anio", anio);
            List<Circuito> circuitos = query.getResultList();
            logger.info("Se obtuvieron {} circuitos para la temporada {}", circuitos.size(), anio);
            return circuitos;
        } catch (Exception e) {
            logger.error("Error al listar circuitos por temporada: " + anio, e);
            throw new RuntimeException("Error al obtener circuitos de la temporada", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca un circuito por su ID.
     *
     * @param id ID del circuito
     * @return Optional con el circuito si existe
     */
    public Optional<Circuito> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Circuito circuito = em.find(Circuito.class, id);
            if (circuito != null) {
                logger.debug("Circuito encontrado con ID {}: {}", id, circuito.getNombre());
            } else {
                logger.debug("No se encontró circuito con ID: {}", id);
            }
            return Optional.ofNullable(circuito);
        } catch (Exception e) {
            logger.error("Error al buscar circuito por ID: " + id, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca circuitos por nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre del circuito
     * @return Lista de circuitos que coinciden
     */
    public List<Circuito> findByNombre(String nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Circuito> query = em.createQuery(
                    "SELECT c FROM Circuito c WHERE LOWER(c.nombre) LIKE LOWER(:nombre) ORDER BY c.nombre",
                    Circuito.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar circuitos por nombre: " + nombre, e);
            throw new RuntimeException("Error al buscar circuitos por nombre", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca circuitos por ubicación.
     *
     * @param ubicacion Ubicación del circuito
     * @return Lista de circuitos en esa ubicación
     */
    public List<Circuito> findByUbicacion(String ubicacion) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Circuito> query = em.createQuery(
                    "SELECT c FROM Circuito c WHERE LOWER(c.ubicacion) LIKE LOWER(:ubicacion) ORDER BY c.nombre",
                    Circuito.class
            );
            query.setParameter("ubicacion", "%" + ubicacion + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al buscar circuitos por ubicación: " + ubicacion, e);
            throw new RuntimeException("Error al buscar circuitos por ubicación", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda un nuevo circuito en la base de datos.
     *
     * @param circuito Circuito a guardar
     * @return Circuito guardado con ID asignado
     */
    public Circuito save(Circuito circuito) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(circuito);
            em.getTransaction().commit();
            logger.info("Circuito guardado exitosamente: {} (ID: {})",
                    circuito.getNombre(), circuito.getId());
            return circuito;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar circuito: " + circuito.getNombre(), e);
            throw new RuntimeException("Error al guardar circuito", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Actualiza un circuito existente.
     *
     * @param circuito Circuito con datos actualizados
     * @return Circuito actualizado
     */
    public Circuito update(Circuito circuito) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Circuito updated = em.merge(circuito);
            em.getTransaction().commit();
            logger.info("Circuito actualizado exitosamente: {} (ID: {})",
                    updated.getNombre(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar circuito: " + circuito.getNombre(), e);
            throw new RuntimeException("Error al actualizar circuito", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina un circuito por su ID.
     *
     * @param id ID del circuito a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Circuito circuito = em.find(Circuito.class, id);
            if (circuito != null) {
                em.remove(circuito);
                em.getTransaction().commit();
                logger.info("Circuito eliminado: {} (ID: {})", circuito.getNombre(), id);
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró circuito con ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar circuito con ID: " + id, e);
            throw new RuntimeException("Error al eliminar circuito", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Cuenta el total de circuitos en la base de datos.
     *
     * @return Número total de circuitos
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Circuito c",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error al contar circuitos", e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }
}
