package co.com.service;

import co.com.config.ConfiguracionTemporada;
import co.com.constants.F1PointsSystem;
import co.com.dao.CarreraDAO;
import co.com.dao.PilotoDAO;
import co.com.dao.ResultadoDAO;
import co.com.model.Carrera;
import co.com.model.Piloto;
import co.com.model.Resultado;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultadoService {
    private static final Logger logger = LoggerFactory.getLogger(ResultadoService.class);

    private final ResultadoDAO resultadoDAO;
    private final CarreraDAO carreraDAO;
    private final PilotoDAO pilotoDAO;

    public ResultadoService() {
        this.resultadoDAO = new ResultadoDAO();
        this.carreraDAO = new CarreraDAO();
        this.pilotoDAO = new PilotoDAO();
    }

    /**
     * DTO para ingresar resultado de un piloto en una carrera.
     */
    public static class ResultadoDTO {
        private Long pilotoId;
        private Integer posicionFinal;
        private Integer vueltas;
        private String tiempo;
        private Boolean retirado;
        private String motivoRetiro;
        private Boolean vuelaRapida;

        // Constructor, getters y setters
        public ResultadoDTO() {}

        public ResultadoDTO(Long pilotoId, Integer posicionFinal) {
            this.pilotoId = pilotoId;
            this.posicionFinal = posicionFinal;
            this.retirado = false;
            this.vuelaRapida = false;
        }

        // Getters y Setters
        public Long getPilotoId() { return pilotoId; }
        public void setPilotoId(Long pilotoId) { this.pilotoId = pilotoId; }

        public Integer getPosicionFinal() { return posicionFinal; }
        public void setPosicionFinal(Integer posicionFinal) { this.posicionFinal = posicionFinal; }

        public Integer getVueltas() { return vueltas; }
        public void setVueltas(Integer vueltas) { this.vueltas = vueltas; }

        public String getTiempo() { return tiempo; }
        public void setTiempo(String tiempo) { this.tiempo = tiempo; }

        public Boolean getRetirado() { return retirado; }
        public void setRetirado(Boolean retirado) { this.retirado = retirado; }

        public String getMotivoRetiro() { return motivoRetiro; }
        public void setMotivoRetiro(String motivoRetiro) { this.motivoRetiro = motivoRetiro; }

        public Boolean getVuelaRapida() { return vuelaRapida; }
        public void setVuelaRapida(Boolean vuelaRapida) { this.vuelaRapida = vuelaRapida; }
    }

    /**
     * Ingresa los resultados completos de una carrera.
     * Valida que la carrera permita ingreso manual y calcula puntos automáticamente.
     *
     * @param carreraId ID de la carrera
     * @param resultados Lista de resultados de los pilotos
     * @return Número de resultados ingresados
     */
    public int ingresarResultadosCarrera(Long carreraId, List<ResultadoDTO> resultados) {
        logger.info("Ingresando resultados para carrera ID: {}", carreraId);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Obtener la carrera
            Optional<Carrera> carreraOpt = carreraDAO.findById(carreraId);
            if (carreraOpt.isEmpty()) {
                throw new IllegalArgumentException("No existe la carrera con ID: " + carreraId);
            }

            Carrera carrera = carreraOpt.get();

            // Validar fecha de congelación
            if (!ConfiguracionTemporada.permiteIngresoManual(carrera.getFecha())) {
                throw new IllegalStateException(
                        "Esta carrera (" + carrera.getFecha() + ") es anterior a la fecha de congelación. " +
                                "No se permite ingreso manual de resultados."
                );
            }

            // Verificar si ya tiene resultados y eliminarlos
            List<Resultado> resultadosExistentes = resultadoDAO.findByCarrera(carreraId);
            if (!resultadosExistentes.isEmpty()) {
                logger.warn("La carrera {} ya tiene {} resultados. Serán eliminados y reemplazados.",
                        carreraId, resultadosExistentes.size());

                // Eliminar usando merge para asegurar que están gestionados
                for (Resultado r : resultadosExistentes) {
                    Resultado managed = em.merge(r);
                    em.remove(managed);
                }
                em.flush(); // Asegurar que se eliminan antes de insertar nuevos
            }

            int resultadosIngresados = 0;

            // Ingresar nuevos resultados
            for (ResultadoDTO dto : resultados) {
                // Validar piloto
                Optional<Piloto> pilotoOpt = pilotoDAO.findById(dto.getPilotoId());
                if (pilotoOpt.isEmpty()) {
                    logger.warn("Piloto con ID {} no existe, se omite", dto.getPilotoId());
                    continue;
                }

                Piloto piloto = pilotoOpt.get();

                // Crear resultado
                Resultado resultado = new Resultado();
                resultado.setCarrera(carrera);
                resultado.setPiloto(piloto);
                resultado.setPosicionFinal(dto.getPosicionFinal());
                resultado.setVueltas(dto.getVueltas());
                resultado.setTiempo(dto.getTiempo());
                resultado.setRetirado(dto.getRetirado() != null ? dto.getRetirado() : false);
                resultado.setMotivoRetiro(dto.getMotivoRetiro());

                // Calcular puntos automáticamente
                BigDecimal puntos;
                if (resultado.getRetirado()) {
                    puntos = BigDecimal.ZERO;
                } else {
                    boolean tieneVuelaRapida = dto.getVuelaRapida() != null && dto.getVuelaRapida();
                    puntos = F1PointsSystem.calculatePoints(dto.getPosicionFinal(), tieneVuelaRapida);
                }

                resultado.setPuntosObtenidos(puntos);

                em.persist(resultado);
                resultadosIngresados++;

                logger.debug("Resultado ingresado: {} - Posición {} - {} puntos",
                        piloto.getNombre(), dto.getPosicionFinal(), puntos);
            }

            em.getTransaction().commit();
            logger.info("Se ingresaron {} resultados para la carrera {}",
                    resultadosIngresados, carrera.getNombreGp());

            return resultadosIngresados;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al ingresar resultados de carrera", e);
            throw new RuntimeException("Error al ingresar resultados: " + e.getMessage(), e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Obtiene los resultados de una carrera.
     *
     * @param carreraId ID de la carrera
     * @return Lista de resultados
     */
    public List<Resultado> obtenerResultadosCarrera(Long carreraId) {
        return resultadoDAO.findByCarrera(carreraId);
    }

    /**
     * Verifica si una carrera permite ingreso manual de resultados.
     *
     * @param carreraId ID de la carrera
     * @return true si permite ingreso manual
     */
    public boolean permiteIngresoManual(Long carreraId) {
        Optional<Carrera> carreraOpt = carreraDAO.findById(carreraId);
        if (carreraOpt.isEmpty()) {
            return false;
        }

        LocalDate fechaCarrera = carreraOpt.get().getFecha();
        return ConfiguracionTemporada.permiteIngresoManual(fechaCarrera);
    }

    /**
     * Obtiene las carreras que permiten ingreso manual (después de congelación).
     *
     * @param anio Año de la temporada
     * @return Lista de carreras que permiten ingreso manual
     */
    public List<Carrera> obtenerCarrerasEditables(int anio) {
        List<Carrera> todasLasCarreras = carreraDAO.findByTemporada(anio);
        List<Carrera> editables = new ArrayList<>();

        for (Carrera carrera : todasLasCarreras) {
            if (ConfiguracionTemporada.permiteIngresoManual(carrera.getFecha())) {
                editables.add(carrera);
            }
        }

        logger.info("Carreras editables en {}: {} de {}", anio, editables.size(), todasLasCarreras.size());
        return editables;
    }

    /**
     * Elimina todos los resultados de una carrera.
     *
     * @param carreraId ID de la carrera
     * @return Número de resultados eliminados
     */
    public int eliminarResultadosCarrera(Long carreraId) {
        logger.info("Eliminando resultados de carrera ID: {}", carreraId);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Verificar permiso
            if (!permiteIngresoManual(carreraId)) {
                throw new IllegalStateException(
                        "No se pueden eliminar resultados de carreras anteriores a la fecha de congelación"
                );
            }

            // Opción 1: Eliminar con merge (recomendado para pocas entidades)
            List<Resultado> resultados = resultadoDAO.findByCarrera(carreraId);
            int eliminados = 0;

            for (Resultado resultado : resultados) {
                // Merge trae la entidad al contexto de persistencia
                Resultado managed = em.merge(resultado);
                em.remove(managed);
                eliminados++;
            }

            // Opción 2: Eliminar con query JPQL (más eficiente para muchos registros)
            // int eliminados = em.createQuery(
            //     "DELETE FROM Resultado r WHERE r.carrera.id = :carreraId")
            //     .setParameter("carreraId", carreraId)
            //     .executeUpdate();

            em.getTransaction().commit();
            logger.info("Se eliminaron {} resultados", eliminados);

            return eliminados;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al eliminar resultados", e);
            throw new RuntimeException("Error al eliminar resultados: " + e.getMessage(), e);
        } finally {
            JPAUtil.close(em);
        }
    }
}