package co.com.simulacion.factory;

import co.com.model.Carrera;
import co.com.model.Temporada;

import java.time.LocalDate;

/**
 * Fábrica que crea instancias de carreras simuladas.
 * Aplica el patrón Factory Method.
 */
public class CarreraSimuladaFactory {

    /**
     * Crea una nueva carrera simulada basada en una carrera existente.
     *
     * @param carreraBase carrera original (usada como modelo)
     * @param temporada temporada a la que pertenece la simulación
     * @param fechaSimulada nueva fecha (puede ser la misma)
     * @return objeto Carrera configurado como simulada
     */
    public Carrera crearCarreraSimulada(Carrera carreraBase, Temporada temporada, LocalDate fechaSimulada) {
        Carrera c = new Carrera();
        c.setNombreGp(carreraBase.getNombreGp() + " (Simulada)");
        c.setTemporada(temporada);
        c.setCircuito(carreraBase.getCircuito());
        c.setGpNumero(carreraBase.getGpNumero());
        c.setFecha(fechaSimulada);
        return c;
    }
}
