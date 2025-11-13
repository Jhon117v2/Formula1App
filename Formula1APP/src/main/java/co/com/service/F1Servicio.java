package co.com.service;

import co.com.dao.*;
import co.com.model.*;
import java.util.*;

public class F1Servicio {
    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final CircuitoDAO circuitoDAO = new CircuitoDAO();
    private final ConstructorDAO constructorDAO = new ConstructorDAO();
    private final PilotoDAO pilotoDAO = new PilotoDAO();
    private final ResultadoDAO resultadoDAO = new ResultadoDAO();

    public List<Carrera> getCarreras(int anio) {
        if (anio <= 0) {
            System.err.println("⚠️ Año inválido: " + anio);
            return List.of();
        }
        try {
            return carreraDAO.findByTemporada(anio);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Circuito> getCircuitos(int anio) {
        if (anio <= 0) return List.of();
        try {
            return circuitoDAO.findByTemporada(anio);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getClasificacionPilotos(int anio) {
        if (anio <= 0) return List.of();
        try {
            return resultadoDAO.getClasificacionPilotos(anio);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getClasificacionConstructores(int anio) {
        if (anio <= 0) return List.of();
        try {
            return resultadoDAO.getClasificacionConstructores(anio);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Resultado> getResultadosCarrera(Long carreraId) {
        if (carreraId == null || carreraId <= 0) {
            return List.of();
        }
        try {
            return resultadoDAO.findByCarrera(carreraId);
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Piloto> getPilotos() {
        try {
            return pilotoDAO.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Constructor> getConstructores() {
        try {
            return constructorDAO.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Optional<Carrera> getCarreraById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        try {
            return carreraDAO.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}