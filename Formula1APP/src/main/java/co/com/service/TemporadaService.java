package co.com.service;


import co.com.dao.*;
import co.com.model.Carrera;
import co.com.model.Temporada;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TemporadaService {
    private static final Logger logger = LoggerFactory.getLogger(TemporadaService.class);

    private final TemporadaDAO temporadaDAO;
    private final CircuitoDAO circuitoDAO;
    private final CarreraDAO carreraDAO;

    public TemporadaService() {
        this.temporadaDAO = new TemporadaDAO();
        this.circuitoDAO = new CircuitoDAO();
        this.carreraDAO = new CarreraDAO();
    }

    /**
     * Copia la estructura de carreras de una temporada a otra.
     * Útil para crear la temporada 2025 basada en 2024.
     *
     * @param anioOrigen Año de la temporada origen (ej: 2024)
     * @param anioDestino Año de la temporada destino (ej: 2025)
     * @return Número de carreras copiadas
     */
    public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
        logger.info("Copiando estructura de temporada {} a {}", anioOrigen, anioDestino);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Verificar si existe temporada origen
            Optional<Temporada> temporadaOrigenOpt = temporadaDAO.findByAnio(anioOrigen);
            if (temporadaOrigenOpt.isEmpty()) {
                throw new IllegalArgumentException("No existe la temporada origen: " + anioOrigen);
            }

            // Crear o obtener temporada destino
            Temporada temporadaDestino = temporadaDAO.findByAnio(anioDestino)
                    .orElseGet(() -> {
                        Temporada nueva = new Temporada(anioDestino);
                        em.persist(nueva);
                        logger.info("Temporada {} creada", anioDestino);
                        return nueva;
                    });

            // Obtener carreras de la temporada origen
            List<Carrera> carrerasOrigen = carreraDAO.findByTemporada(anioOrigen);

            if (carrerasOrigen.isEmpty()) {
                logger.warn("No hay carreras en la temporada origen {}", anioOrigen);
                em.getTransaction().rollback();
                return 0;
            }

            int carrerasCopiadas = 0;

            // Copiar cada carrera
            for (Carrera carreraOrigen : carrerasOrigen) {
                Carrera carreraNueva = new Carrera();
                carreraNueva.setNombreGp(carreraOrigen.getNombreGp());
                carreraNueva.setCircuito(carreraOrigen.getCircuito());
                carreraNueva.setTemporada(temporadaDestino);
                carreraNueva.setGpNumero(carreraOrigen.getGpNumero());

                // Ajustar fecha al año siguiente (mismo mes y día si es posible)
                LocalDate fechaOrigen = carreraOrigen.getFecha();
                if (fechaOrigen != null) {
                    LocalDate fechaNueva = fechaOrigen.plusYears(1);
                    carreraNueva.setFecha(fechaNueva);
                }

                em.persist(carreraNueva);
                carrerasCopiadas++;

                logger.debug("Carrera copiada: {} (GP #{})",
                        carreraNueva.getNombreGp(), carreraNueva.getGpNumero());
            }

            em.getTransaction().commit();
            logger.info("Se copiaron {} carreras de {} a {}",
                    carrerasCopiadas, anioOrigen, anioDestino);

            return carrerasCopiadas;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al copiar estructura de temporada", e);
            throw new RuntimeException("Error al copiar temporada: " + e.getMessage(), e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Inicializa la temporada 2025 heredando de 2024.
     *
     * @return true si se inicializó correctamente
     */
    public boolean inicializarTemporada2025() {
        try {
            logger.info("Inicializando temporada 2025...");

            // Verificar si ya existe
            Optional<Temporada> temp2025 = temporadaDAO.findByAnio(2025);
            if (temp2025.isPresent()) {
                List<Carrera> carreras = carreraDAO.findByTemporada(2025);
                if (!carreras.isEmpty()) {
                    logger.warn("La temporada 2025 ya está inicializada con {} carreras", carreras.size());
                    return false;
                }
            }

            // Copiar estructura de 2024
            int carrerasCopiadas = copiarEstructuraTemporada(2024, 2025);

            logger.info("Temporada 2025 inicializada exitosamente con {} carreras", carrerasCopiadas);
            return carrerasCopiadas > 0;

        } catch (Exception e) {
            logger.error("Error al inicializar temporada 2025", e);
            return false;
        }
    }
}