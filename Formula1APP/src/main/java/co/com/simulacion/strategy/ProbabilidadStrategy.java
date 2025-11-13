package co.com.simulacion.strategy;

import co.com.model.Piloto;

import java.math.BigDecimal;
import java.util.*;

/**
 * Estrategia que realiza muestreo sin reemplazo ponderado por puntos.
 * Recibe un mapa con los puntos actuales por piloto para construir los pesos.
 */
public class ProbabilidadStrategy implements SimulacionStrategy {

    private final Map<Long, BigDecimal> puntosPorPiloto;
    private final Random rng;
    private final double floorPeso; // peso mínimo para pilotos con 0 puntos (ej: 1.0)

    /**
     * @param puntosPorPiloto mapa idPiloto -> puntos (puede ser vacío)
     * @param seed semilla aleatoria (usa System.nanoTime() si es null)
     * @param floorPeso peso mínimo para pilotos sin puntos (>= 0.1)
     */
    public ProbabilidadStrategy(Map<Long, BigDecimal> puntosPorPiloto, Long seed, double floorPeso) {
        this.puntosPorPiloto = puntosPorPiloto != null ? puntosPorPiloto : Collections.emptyMap();
        this.rng = (seed == null) ? new Random() : new Random(seed);
        this.floorPeso = Math.max(0.1, floorPeso);
    }

    public ProbabilidadStrategy(Map<Long, BigDecimal> puntosPorPiloto) {
        this(puntosPorPiloto, null, 1.0);
    }

    @Override
    public List<Piloto> simularResultados(List<Piloto> pilotos) {
        if (pilotos == null || pilotos.isEmpty()) return Collections.emptyList();

        // Trabajamos con una copia mutable
        List<Piloto> candidatos = new ArrayList<>(pilotos);

        // Construir mapa de pesos double
        Map<Long, Double> pesos = new HashMap<>();
        double sumaTotal = 0.0;
        for (Piloto p : candidatos) {
            BigDecimal pts = puntosPorPiloto.getOrDefault(p.getId(), BigDecimal.ZERO);
            double w = pts == null ? 0.0 : pts.doubleValue();
            if (w <= 0.0) w = floorPeso; // floor para no excluir pilotos
            pesos.put(p.getId(), w);
            sumaTotal += w;
        }

        // Si por alguna razón sumaTotal = 0, fallback a shuffle simple
        if (sumaTotal <= 0.0) {
            Collections.shuffle(candidatos, rng);
            return candidatos;
        }

        List<Piloto> orden = new ArrayList<>(candidatos.size());

        // Muestreo sin reemplazo ponderado
        while (!candidatos.isEmpty()) {
            double r = rng.nextDouble() * sumaTotal;
            double acumulado = 0.0;
            Piloto seleccionado = null;

            for (Piloto p : candidatos) {
                acumulado += pesos.getOrDefault(p.getId(), 0.0);
                if (r <= acumulado) {
                    seleccionado = p;
                    break;
                }
            }

            // fallback de seguridad
            if (seleccionado == null) {
                seleccionado = candidatos.get(rng.nextInt(candidatos.size()));
            }

            orden.add(seleccionado);

            // actualizar suma y remover seleccionado
            double wSel = pesos.getOrDefault(seleccionado.getId(), 0.0);
            sumaTotal -= wSel;
            pesos.remove(seleccionado.getId());
            candidatos.remove(seleccionado);

            // protección contra redondeos negativos
            if (sumaTotal < 0) sumaTotal = 0;
        }

        return orden;
    }
}
