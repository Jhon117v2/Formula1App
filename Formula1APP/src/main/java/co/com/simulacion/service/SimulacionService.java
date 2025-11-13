package co.com.simulacion.service;

import co.com.config.ConfiguracionTemporada;
import co.com.constants.F1PointsSystem;
import co.com.dao.CarreraDAO;
import co.com.dao.PilotoDAO;
import co.com.dao.ResultadoDAO;
import co.com.model.Carrera;
import co.com.model.Piloto;
import co.com.model.Resultado;
import co.com.service.F1Servicio;
import co.com.simulacion.factory.ResultadoBuilder;
import co.com.simulacion.strategy.ProbabilidadStrategy;
import co.com.simulacion.strategy.SimulacionStrategy;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Servicio principal para ejecutar simulaciones de carreras.
 * Aplica patrones Strategy, Factory y Builder.
 */
public class SimulacionService {
    private static final Logger logger = LoggerFactory.getLogger(SimulacionService.class);

    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final PilotoDAO pilotoDAO = new PilotoDAO();
    private final ResultadoDAO resultadoDAO = new ResultadoDAO();
    private final F1Servicio f1Servicio = new F1Servicio();

    private SimulacionStrategy estrategia;

    // Guarda las últimas carreras simuladas
    private final List<Carrera> ultimasCarrerasSimuladas = new ArrayList<>();

    public SimulacionService() {
        // Por defecto: usar estrategia ponderada por puntos (ProbabilidadStrategy)
        Map<Long, BigDecimal> puntosPorPiloto = new HashMap<>();
        this.estrategia = new ProbabilidadStrategy(puntosPorPiloto);
    }

    public void setEstrategia(SimulacionStrategy estrategia) {
        this.estrategia = estrategia;
    }

    /**
     * Simula las dos primeras carreras posteriores a la fecha de congelación.
     */
    public int simularCarrerasPosteriores() {
        logger.info("Iniciando simulación de carreras posteriores a la fecha de congelación...");

        List<Carrera> carreras2025 = carreraDAO.findByTemporada(ConfiguracionTemporada.TEMPORADA_2025);

        // Filtrar solo las carreras después de la fecha de congelación
        List<Carrera> carrerasPosteriores = carreras2025.stream()
                .filter(c -> c.getFecha().isAfter(ConfiguracionTemporada.FECHA_CONGELACION))
                .sorted(Comparator.comparing(Carrera::getFecha))
                .toList();

        if (carrerasPosteriores.isEmpty()) {
            logger.warn("No hay carreras posteriores a la fecha de congelación");
            System.out.println("\n[ADVERTENCIA] No hay carreras posteriores a la fecha de congelación.\n");
            return 0;
        }

        int simuladas = 0;
        List<Carrera> carrerasSimuladas = new ArrayList<>();

        for (int i = 0; i < Math.min(2, carrerasPosteriores.size()); i++) {
            Carrera carrera = carrerasPosteriores.get(i);

            logger.info("➡️ Simulando carrera posterior a la congelación: {} (ID: {}, Fecha: {})",
                    carrera.getNombreGp(), carrera.getId(), carrera.getFecha());

            simularCarrera(carrera);
            simuladas++;
            carrerasSimuladas.add(carrera);
        }

        // Guardamos las últimas simuladas para consultarlas luego con la opción 9
        ultimasCarrerasSimuladas.clear();
        ultimasCarrerasSimuladas.addAll(carrerasSimuladas);

        logger.info("Simulación completada: {} carreras generadas", simuladas);

        // Mostrar resumen rápido en consola
        if (!carrerasSimuladas.isEmpty()) {
            System.out.println("\n[RESUMEN DE SIMULACIÓN]");
            System.out.println("-----------------------------------------------");
            for (Carrera c : carrerasSimuladas) {
                long totalResultados = resultadoDAO.countByCarrera(c.getId());
                System.out.printf("Carrera: %-30s  →  %d resultados generados%n",
                        c.getNombreGp(), totalResultados);
            }
            System.out.println("-----------------------------------------------\n");
        }

        return simuladas;
    }

    /**
     * Simula una carrera completa con tiempos, posiciones y resultados.
     */
    public void simularCarrera(Carrera carrera) {
        logger.info("Simulando carrera: {}", carrera.getNombreGp());

        List<Piloto> pilotos = pilotoDAO.findAll();

        if (pilotos.isEmpty()) {
            logger.error("No hay pilotos registrados, no se puede simular");
            return;
        }

        List<Piloto> posiciones = estrategia.simularResultados(pilotos);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // Eliminar resultados previos si existen
            List<Resultado> existentes = resultadoDAO.findByCarrera(carrera.getId());
            for (Resultado r : existentes) {
                em.remove(em.merge(r));
            }

            Random random = new Random();
            int vueltasTotales = 50 + random.nextInt(10);

            // Tiempo base para el ganador
            int minutosBase = 90 + random.nextInt(10);
            int segundosBase = random.nextInt(60);
            int milisBase = random.nextInt(1000);
            String tiempoGanador = String.format("1:%02d:%02d.%03d", minutosBase, segundosBase, milisBase);

            for (int i = 0; i < posiciones.size(); i++) {
                Piloto piloto = posiciones.get(i);
                int posicion = i + 1;
                boolean retirado = posicion > 18 && random.nextDouble() < 0.2;
                String motivo = retirado ? "Falla mecánica" : null;
                boolean vueltaRapida = posicion == 1 && random.nextDouble() < 0.4;

                BigDecimal puntos = retirado ? BigDecimal.ZERO :
                        F1PointsSystem.calculatePoints(posicion, vueltaRapida);

                String tiempo;
                if (retirado) {
                    tiempo = null;
                } else if (posicion == 1) {
                    tiempo = tiempoGanador;
                } else {
                    int diferencia = 2 + random.nextInt(90);
                    tiempo = String.format("+%ds", diferencia);
                }

                Resultado resultado = new ResultadoBuilder()
                        .carrera(carrera)
                        .piloto(piloto)
                        .posicion(posicion)
                        .puntos(puntos)
                        .vueltas(retirado ? vueltasTotales - random.nextInt(10) : vueltasTotales)
                        .tiempo(tiempo)
                        .retirado(retirado, motivo)
                        .build();

                em.persist(resultado);
            }

            em.getTransaction().commit();
            logger.info("Carrera '{}' simulada exitosamente", carrera.getNombreGp());

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Error al simular carrera", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    /**
     * Muestra los resultados de las últimas carreras simuladas (opción 9 del menú)
     */
    public void mostrarUltimosResultadosSimulados() {
        if (ultimasCarrerasSimuladas.isEmpty()) {
            System.out.println("\n[INFO] No hay simulaciones recientes. Ejecuta primero la opción 8.\n");
            return;
        }

        System.out.println("\n RESULTADOS DE LAS ÚLTIMAS CARRERAS SIMULADAS");
        System.out.println("---------------------------------------------------------------");

        for (Carrera c : ultimasCarrerasSimuladas) {
            List<Resultado> resultados = resultadoDAO.findByCarrera(c.getId());

            System.out.printf("\n %s (%s)\n", c.getNombreGp(), c.getFecha());
            System.out.println("---------------------------------------------------------------");

            if (resultados.isEmpty()) {
                System.out.println("⚠️  No hay resultados registrados para esta carrera.");
                continue;
            }

            resultados.stream()
                    .sorted(Comparator.comparing(Resultado::getPosicionFinal))
                    .limit(5)
                    .forEach(r -> System.out.printf("Pos %d - Piloto #%d - %.0f pts\n",
                            r.getPosicionFinal(), r.getPiloto().getId(), r.getPuntosObtenidos()));

            System.out.printf("Total resultados: %d\n", resultados.size());
        }

        System.out.println("---------------------------------------------------------------\n");
    }
}