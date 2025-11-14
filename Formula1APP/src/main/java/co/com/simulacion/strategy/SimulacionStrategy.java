package co.com.simulacion.strategy;

import co.com.model.Piloto;

import java.util.List;

/**
 * Interfaz Strategy para generar el orden de llegada de una carrera.
 * Implementaciones diferentes (probabilística, aleatoria, por circuito, etc.)
 * deberán devolver la lista de pilotos ordenada (index 0 = ganador).
 */
public interface SimulacionStrategy {
    /**
     * Genera y devuelve la lista de pilotos en el orden final simulado.
     *
     * @param pilotos lista de pilotos candidatos (no nula)
     * @return lista ordenada por posición final (no debe contener nulos)
     */
    List<Piloto> simularResultados(List<Piloto> pilotos);
}