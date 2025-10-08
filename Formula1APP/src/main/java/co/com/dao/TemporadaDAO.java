package co.com.dao;

import co.com.model.Temporada;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TemporadaDAO {
    private static final Logger logger = LoggerFactory.getLogger(TemporadaDAO.class);

    /**
     * Obtiene todas las temporadas ordenadas por año descendente.
     *
     * @return Lista de todas las temporadas
     */
    public List<Temporada> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Temporada> query = em.createQuery(
                    "SELECT t FROM Temporada t ORDER BY t.anio DESC",
                    Temporada.class
            );
            List<Temporada> temporadas = query.getResultList();
            logger.info("Se obtuvieron {} temporadas", temporadas.size());
            return temporadas;
        } catch (Exception e) {
            logger.error("Error al listar temporadas", e);
            throw new RuntimeException("Error al obtener temporadas", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca una temporada por su ID.
     *
     * @param id ID de la temporada
     * @return Optional con la temporada si existe
     */
    public Optional<Temporada> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Temporada temporada = em.find(Temporada.class, id);
            if (temporada != null) {
                logger.debug("Temporada encontrada con ID {}: {}", id, temporada.getAnio());
            } else {
                logger.debug("No se encontró temporada con ID: {}", id);
            }
            return Optional.ofNullable(temporada);
        } catch (Exception e) {
            logger.error("Error al buscar temporada por ID: " + id, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Busca una temporada por año.
     * Este método es crucial para el sistema de temporadas 2024/2025.
     *
     * @param anio Año de la temporada
     * @return Optional con la temporada si existe
     */
    public Optional<Temporada> findByAnio(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Temporada> query = em.createQuery(
                    "SELECT t FROM Temporada t WHERE t.anio = :anio",
                    Temporada.class
            );
            query.setParameter("anio", anio);
            Optional<Temporada> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Temporada encontrada: {}", anio);
            } else {
                logger.debug("No se encontró temporada para el año: {}", anio);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar temporada por año: " + anio, e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene las temporadas en un rango de años.
     *
     * @param anioInicio Año de inicio (inclusive)
     * @param anioFin Año de fin (inclusive)
     * @return Lista de temporadas en el rango
     */
    public List<Temporada> findByRangoAnios(Integer anioInicio, Integer anioFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Temporada> query = em.createQuery(
                    "SELECT t FROM Temporada t WHERE t.anio BETWEEN :inicio AND :fin ORDER BY t.anio DESC",
                    Temporada.class
            );
            query.setParameter("inicio", anioInicio);
            query.setParameter("fin", anioFin);
            List<Temporada> temporadas = query.getResultList();
            logger.debug("Se encontraron {} temporadas entre {} y {}",
                    temporadas.size(), anioInicio, anioFin);
            return temporadas;
        } catch (Exception e) {
            logger.error("Error al buscar temporadas por rango", e);
            throw new RuntimeException("Error al buscar temporadas por rango", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene la temporada más reciente.
     *
     * @return Optional con la temporada más reciente
     */
    public Optional<Temporada> findMasReciente() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Temporada> query = em.createQuery(
                    "SELECT t FROM Temporada t ORDER BY t.anio DESC",
                    Temporada.class
            );
            query.setMaxResults(1);
            Optional<Temporada> result = query.getResultStream().findFirst();

            if (result.isPresent()) {
                logger.debug("Temporada más reciente: {}", result.get().getAnio());
            }

            return result;
        } catch (Exception e) {
            logger.error("Error al buscar temporada más reciente", e);
            return Optional.empty();
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Verifica si existe una temporada para un año específico.
     *
     * @param anio Año a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existeTemporada(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Temporada t WHERE t.anio = :anio",
                    Long.class
            );
            query.setParameter("anio", anio);
            Long count = query.getSingleResult();

            boolean existe = count > 0;
            logger.debug("Temporada {} {}", anio, existe ? "existe" : "no existe");

            return existe;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de temporada: " + anio, e);
            return false;
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda una nueva temporada en la base de datos.
     *
     * @param temporada Temporada a guardar
     * @return Temporada guardada con ID asignado
     */
    public Temporada save(Temporada temporada) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Validar que no exista ya una temporada con ese año
            if (existeTemporada(temporada.getAnio())) {
                throw new IllegalArgumentException(
                        "Ya existe una temporada para el año " + temporada.getAnio()
                );
            }

            em.persist(temporada);
            em.getTransaction().commit();
            logger.info("Temporada guardada exitosamente: {} (ID: {})",
                    temporada.getAnio(), temporada.getId());
            return temporada;
        } catch (IllegalArgumentException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.warn("No se puede guardar temporada duplicada: {}", temporada.getAnio());
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar temporada: " + temporada.getAnio(), e);
            throw new RuntimeException("Error al guardar temporada", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Guarda una temporada sin validar duplicados (para uso interno).
     * Útil cuando se copia de otra temporada.
     *
     * @param temporada Temporada a guardar
     * @param em EntityManager existente (debe estar en transacción)
     * @return Temporada guardada
     */
    public Temporada saveWithoutValidation(Temporada temporada, EntityManager em) {
        try {
            em.persist(temporada);
            logger.debug("Temporada persistida: {}", temporada.getAnio());
            return temporada;
        } catch (Exception e) {
            logger.error("Error al persistir temporada", e);
            throw new RuntimeException("Error al persistir temporada", e);
        }
    }

    /**
     * Actualiza una temporada existente.
     *
     * @param temporada Temporada con datos actualizados
     * @return Temporada actualizada
     */
    public Temporada update(Temporada temporada) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Temporada updated = em.merge(temporada);
            em.getTransaction().commit();
            logger.info("Temporada actualizada exitosamente: {} (ID: {})",
                    updated.getAnio(), updated.getId());
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al actualizar temporada: " + temporada.getAnio(), e);
            throw new RuntimeException("Error al actualizar temporada", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina una temporada por su ID.
     * ADVERTENCIA: Esto también eliminará todas las carreras asociadas
     * si se configuró CASCADE en la entidad.
     *
     * @param id ID de la temporada a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Temporada temporada = em.find(Temporada.class, id);
            if (temporada != null) {
                // Verificar si tiene carreras asociadas
                TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(c) FROM Carrera c WHERE c.temporada.id = :tempId",
                        Long.class
                );
                query.setParameter("tempId", id);
                Long carrerasCount = query.getSingleResult();

                if (carrerasCount > 0) {
                    logger.warn("La temporada {} tiene {} carreras asociadas",
                            temporada.getAnio(), carrerasCount);
                }

                em.remove(temporada);
                em.getTransaction().commit();
                logger.info("Temporada eliminada: {} (ID: {})", temporada.getAnio(), id);
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró temporada con ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar temporada con ID: " + id, e);
            throw new RuntimeException("Error al eliminar temporada. Puede tener carreras asociadas.", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Elimina una temporada por año.
     *
     * @param anio Año de la temporada a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean deleteByAnio(Integer anio) {
        Optional<Temporada> temporada = findByAnio(anio);
        if (temporada.isPresent()) {
            return delete(temporada.get().getId());
        }
        logger.warn("No se encontró temporada para el año: {}", anio);
        return false;
    }

    /**
     * Cuenta el total de temporadas en la base de datos.
     *
     * @return Número total de temporadas
     */
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Temporada t",
                    Long.class
            );
            Long count = query.getSingleResult();
            logger.debug("Total de temporadas: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error al contar temporadas", e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene el número de carreras de una temporada.
     *
     * @param anio Año de la temporada
     * @return Número de carreras
     */
    public long contarCarrerasPorTemporada(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(c) FROM Carrera c WHERE c.temporada.anio = :anio",
                    Long.class
            );
            query.setParameter("anio", anio);
            Long count = query.getSingleResult();
            logger.debug("Temporada {} tiene {} carreras", anio, count);
            return count;
        } catch (Exception e) {
            logger.error("Error al contar carreras de temporada: " + anio, e);
            return 0;
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene estadísticas de una temporada.
     *
     * @param anio Año de la temporada
     * @return Array con: [carreras, resultados, pilotos únicos]
     */
    public long[] obtenerEstadisticas(Integer anio) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Carreras
            TypedQuery<Long> queryCarreras = em.createQuery(
                    "SELECT COUNT(c) FROM Carrera c WHERE c.temporada.anio = :anio",
                    Long.class
            );
            queryCarreras.setParameter("anio", anio);
            Long carreras = queryCarreras.getSingleResult();

            // Resultados
            TypedQuery<Long> queryResultados = em.createQuery(
                    "SELECT COUNT(r) FROM Resultado r " +
                            "JOIN r.carrera c WHERE c.temporada.anio = :anio",
                    Long.class
            );
            queryResultados.setParameter("anio", anio);
            Long resultados = queryResultados.getSingleResult();

            // Pilotos únicos
            TypedQuery<Long> queryPilotos = em.createQuery(
                    "SELECT COUNT(DISTINCT r.piloto.id) FROM Resultado r " +
                            "JOIN r.carrera c WHERE c.temporada.anio = :anio",
                    Long.class
            );
            queryPilotos.setParameter("anio", anio);
            Long pilotos = queryPilotos.getSingleResult();

            return new long[]{carreras, resultados, pilotos};

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de temporada: " + anio, e);
            return new long[]{0, 0, 0};
        } finally {
            JPAUtil.close(em);
        }
    }
}
