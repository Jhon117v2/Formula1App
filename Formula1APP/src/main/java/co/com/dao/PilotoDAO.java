package co.com.dao;

import co.com.model.Piloto;
import co.com.util.DatabaseManager;  // Mejora: Importar la nueva clase de gestión de conexiones
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar operaciones CRUD de la entidad Piloto.
 */
public class PilotoDAO {
    private static final Logger logger = LoggerFactory.getLogger(PilotoDAO.class);
    private final DatabaseManager dbManager = new DatabaseManager();  // Mejora: Inyectar DatabaseManager para DIP y SRP

    /**
     * Obtiene todos los pilotos ordenados por nombre.
     * Utiliza LEFT JOIN FETCH para cargar el constructor asociado.
     *
     * @return Lista de todos los pilotos
     */
    public List<Piloto> findAll() {
        EntityManager em = dbManager.getEntityManager();
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Busca un piloto por su ID.
     *
     * @param id ID del piloto
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findById(Long id) {
        EntityManager em = dbManager.getEntityManager();
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Busca un piloto por nombre (búsqueda parcial, case-insensitive).
     *
     * @param nombre Nombre o parte del nombre del piloto
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findByNombre(String nombre) {
        EntityManager em = dbManager.getEntityManager();
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                logger.debug("Entrada nula o vacía, devolviendo Optional vacío");
                return Optional.empty();
            }
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Busca pilotos por nacionalidad.
     *
     * @param nacionalidad Nacionalidad del piloto
     * @return Lista de pilotos con esa nacionalidad
     */
    public List<Piloto> findByNacionalidad(String nacionalidad) {
        EntityManager em = dbManager.getEntityManager();
        try {
            if (nacionalidad == null || nacionalidad.trim().isEmpty()) {
                logger.debug("Entrada nula o vacía, devolviendo lista vacía");
                return Collections.emptyList();
            }
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Busca pilotos por constructor/equipo.
     *
     * @param constructorId ID del constructor
     * @return Lista de pilotos del constructor
     */
    public List<Piloto> findByConstructor(Long constructorId) {
        EntityManager em = dbManager.getEntityManager();
        try {
            if (constructorId == null) {
                logger.debug("ID de constructor nulo, devolviendo lista vacía");
                return Collections.emptyList();
            }
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Busca un piloto por su dorsal.
     *
     * @param dorsal Número de dorsal
     * @return Optional con el piloto si existe
     */
    public Optional<Piloto> findByDorsal(String dorsal) {
        EntityManager em = dbManager.getEntityManager();
        try {
            if (dorsal == null || dorsal.trim().isEmpty()) {
                logger.debug("Entrada nula o vacía, devolviendo Optional vacío");
                return Optional.empty();
            }
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Guarda un nuevo piloto en la base de datos.
     *
     * @param piloto Piloto a guardar
     * @return Piloto guardado con ID asignado
     */
    public Piloto save(Piloto piloto) {
        EntityManager em = dbManager.getEntityManager();
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Actualiza un piloto existente.
     *
     * @param piloto Piloto con datos actualizados
     * @return Piloto actualizado
     */
    public Piloto update(Piloto piloto) {
        EntityManager em = dbManager.getEntityManager();
        try {
            // Validación de parámetros
            if (piloto == null) {
                throw new RuntimeException("El piloto no puede ser nulo");
            }
            if (piloto.getId() == null) {
                throw new RuntimeException("El piloto debe tener un ID válido para actualizar");
            }

            em.getTransaction().begin();

            // Verificar que el piloto existe antes de actualizar
            Piloto existingPiloto = em.find(Piloto.class, piloto.getId());
            if (existingPiloto == null) {
                em.getTransaction().rollback();
                logger.warn("No se encontró piloto con ID: {}", piloto.getId());
                throw new RuntimeException("No se puede actualizar un piloto que no existe (ID: " + piloto.getId() + ")");
            }

            // Actualizar los datos
            existingPiloto.setNombre(piloto.getNombre());
            if (piloto.getDorsal() != null) {
                existingPiloto.setDorsal(piloto.getDorsal());
            }
            if (piloto.getNacionalidad() != null) {
                existingPiloto.setNacionalidad(piloto.getNacionalidad());
            }
            if (piloto.getConstructor() != null) {
                existingPiloto.setConstructor(piloto.getConstructor());
            }

            Piloto updated = em.merge(existingPiloto);
            em.getTransaction().commit();

            logger.info("Piloto actualizado exitosamente: {} (ID: {})", updated.getNombre(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar piloto: " + (piloto != null ? piloto.getNombre() : "null"), e);
            throw new RuntimeException("Error al actualizar piloto", e);
        } finally {
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Elimina un piloto por su ID.
     *
     * @param id ID del piloto a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = dbManager.getEntityManager();
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
            dbManager.closeEntityManager(em);
        }
    }

    /**
     * Cuenta el total de pilotos en la base de datos.
     *
     * @return Número total de pilotos
     */
    public long count() {
        EntityManager em = dbManager.getEntityManager();
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
            dbManager.closeEntityManager(em);
        }
    }
}