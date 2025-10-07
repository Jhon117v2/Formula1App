package co.com.service;

import co.com.dao.CarreraDAO;
import co.com.dao.CircuitoDAO;
import co.com.dao.ConstructorDAO;
import co.com.dao.PilotoDAO;
import co.com.dao.ResultadoDAO;
import co.com.model.Carrera;
import co.com.model.Circuito;
import co.com.model.Constructor;
import co.com.model.Piloto;
import co.com.model.Resultado;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestionar consultas relacionadas con temporadas de Formula 1.
 * Utiliza los DAOs para acceder a la base de datos.
 */
public class F1Servicio {
    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final CircuitoDAO circuitoDAO = new CircuitoDAO();
    private final ConstructorDAO constructorDAO = new ConstructorDAO();
    private final PilotoDAO pilotoDAO = new PilotoDAO();
    private final ResultadoDAO resultadoDAO = new ResultadoDAO();

    /**
     * Obtiene el calendario de carreras para una temporada.
     *
     * @param anio Año de la temporada
     * @return Lista de carreras en la temporada
     */
    public List<Carrera> getCarreras(int anio) {
        return carreraDAO.findByTemporada(anio);
    }

    /**
     * Obtiene los circuitos utilizados en una temporada.
     *
     * @param anio Año de la temporada
     * @return Lista de circuitos en la temporada
     */
    public List<Circuito> getCircuitos(int anio) {
        return circuitoDAO.findByTemporada(anio);
    }

    /**
     * Obtiene la clasificación de pilotos para una temporada.
     *
     * @param anio Año de la temporada
     * @return Lista de mapas con datos de clasificación de pilotos
     */
    public List<Map<String, Object>> getClasificacionPilotos(int anio) {
        return resultadoDAO.getClasificacionPilotos(anio);
    }

    /**
     * Obtiene la clasificación de constructores para una temporada.
     *
     * @param anio Año de la temporada
     * @return Lista de mapas con datos de clasificación de constructores
     */
    public List<Map<String, Object>> getClasificacionConstructores(int anio) {
        return resultadoDAO.getClasificacionConstructores(anio);
    }

    /**
     * Obtiene los resultados de una carrera específica.
     *
     * @param carreraId ID de la carrera
     * @return Lista de resultados de la carrera
     */
    public List<Resultado> getResultadosCarrera(Long carreraId) {
        return resultadoDAO.findByCarrera(carreraId);
    }

    /**
     * Obtiene todos los pilotos (asumiendo que son de la temporada actual).
     *
     * @return Lista de pilotos
     */
    public List<Piloto> getPilotos() {
        return pilotoDAO.findAll();
    }

    /**
     * Obtiene todos los constructores.
     *
     * @return Lista de constructores
     */
    public List<Constructor> getConstructores() {
        return constructorDAO.findAll();
    }

    /**
     * Busca una carrera por ID.
     *
     * @param id ID de la carrera
     * @return Optional con la carrera
     */
    public Optional<Carrera> getCarreraById(Long id) {
        return carreraDAO.findById(id);
    }
}