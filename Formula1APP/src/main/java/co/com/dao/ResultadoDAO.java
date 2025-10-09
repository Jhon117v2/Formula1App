package co.com.dao;

import co.com.model.Resultado;
import co.com.util.JPAUtil;
import co.com.util.JDBCUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultadoDAO {
    private static final Logger logger = LoggerFactory.getLogger(ResultadoDAO.class);

    public List<Resultado> findByCarrera(Long carreraId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Resultado> query = em.createQuery(
                    "SELECT r FROM Resultado r " +
                            "JOIN FETCH r.piloto p " +
                            "LEFT JOIN FETCH p.constructor " +
                            "WHERE r.carrera.id = :carreraId " +
                            "ORDER BY r.posicionFinal",
                    Resultado.class
            );
            query.setParameter("carreraId", carreraId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al listar resultados por carrera: " + carreraId, e);
            throw new RuntimeException("Error al obtener resultados", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    public void save(Resultado resultado) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(resultado);
            em.getTransaction().commit();
            logger.info("Resultado guardado");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error al guardar resultado", e);
            throw new RuntimeException("Error al guardar resultado", e);
        } finally {
            JPAUtil.close(em);
        }
    }

    // Usando JDBC para consultas optimizadas de clasificación
    public List<Map<String, Object>> getClasificacionPilotos(Integer anio) {
        String sql = """
            SELECT 
                combined.id,
                combined.nombre,
                combined.dorsal,
                combined.nacionalidad,
                combined.constructor,
                SUM(combined.puntos) AS total_puntos,
                SUM(combined.victorias) AS victorias,
                SUM(combined.podios) AS podios
            FROM (
                SELECT 
                    p.id,
                    p.nombre,
                    p.dorsal,
                    p.nacionalidad,
                    con.nombre AS constructor,
                    r.puntos_obtenidos AS puntos,
                    CASE WHEN r.posicion_final = 1 THEN 1 ELSE 0 END AS victorias,
                    CASE WHEN r.posicion_final <= 3 THEN 1 ELSE 0 END AS podios
                FROM piloto p 
                JOIN resultado r ON r.piloto_id = p.id 
                JOIN carrera c ON r.carrera_id = c.id 
                JOIN temporada t ON c.temporada_id = t.id 
                LEFT JOIN constructor con ON p.constructor_id = con.id
                WHERE t.anio = ?
                UNION ALL
                SELECT 
                    p.id,
                    p.nombre,
                    p.dorsal,
                    p.nacionalidad,
                    con.nombre AS constructor,
                    rs.puntos_obtenidos AS puntos,
                    0 AS victorias,
                    0 AS podios
                FROM piloto p 
                JOIN resultado_sprint rs ON rs.piloto_id = p.id 
                JOIN carrera c ON rs.carrera_id = c.id 
                JOIN temporada t ON c.temporada_id = t.id 
                LEFT JOIN constructor con ON p.constructor_id = con.id
                WHERE t.anio = ?
            ) AS combined 
            GROUP BY combined.id, combined.nombre, combined.dorsal, combined.nacionalidad, combined.constructor
            HAVING total_puntos > 0
            ORDER BY total_puntos DESC, victorias DESC, podios DESC
        """;

        List<Map<String, Object>> clasificacion = new ArrayList<>();

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
            stmt.setInt(2, anio);
            ResultSet rs = stmt.executeQuery();

            int posicion = 1;
            while (rs.next()) {
                Map<String, Object> pilotoData = new HashMap<>();
                pilotoData.put("posicion", posicion++);
                pilotoData.put("id", rs.getLong("id"));
                pilotoData.put("nombre", rs.getString("nombre"));
                pilotoData.put("dorsal", rs.getString("dorsal"));
                pilotoData.put("nacionalidad", rs.getString("nacionalidad"));
                pilotoData.put("constructor", rs.getString("constructor"));
                pilotoData.put("puntos", rs.getBigDecimal("total_puntos"));
                pilotoData.put("victorias", rs.getInt("victorias"));
                pilotoData.put("podios", rs.getInt("podios"));
                clasificacion.add(pilotoData);
            }

            logger.info("Clasificación de pilotos obtenida: {} pilotos", clasificacion.size());
            return clasificacion;

        } catch (Exception e) {
            logger.error("Error al obtener clasificación de pilotos", e);
            throw new RuntimeException("Error al obtener clasificación de pilotos", e);
        }
    }

    public List<Map<String, Object>> getClasificacionConstructores(Integer anio) {
        String sql = """
            SELECT 
                combined.id,
                combined.nombre,
                combined.nacionalidad,
                SUM(combined.puntos) AS total_puntos,
                SUM(combined.victorias) AS victorias,
                SUM(combined.podios) AS podios
            FROM (
                SELECT 
                    con.id,
                    con.nombre,
                    con.nacionalidad,
                    r.puntos_obtenidos AS puntos,
                    CASE WHEN r.posicion_final = 1 THEN 1 ELSE 0 END AS victorias,
                    CASE WHEN r.posicion_final <= 3 THEN 1 ELSE 0 END AS podios
                FROM constructor con 
                JOIN piloto p ON p.constructor_id = con.id 
                JOIN resultado r ON r.piloto_id = p.id 
                JOIN carrera c ON r.carrera_id = c.id 
                JOIN temporada t ON c.temporada_id = t.id 
                WHERE t.anio = ?
                UNION ALL
                SELECT 
                    con.id,
                    con.nombre,
                    con.nacionalidad,
                    rs.puntos_obtenidos AS puntos,
                    0 AS victorias,
                    0 AS podios
                FROM constructor con 
                JOIN piloto p ON p.constructor_id = con.id 
                JOIN resultado_sprint rs ON rs.piloto_id = p.id 
                JOIN carrera c ON rs.carrera_id = c.id 
                JOIN temporada t ON c.temporada_id = t.id 
                WHERE t.anio = ?
            ) AS combined 
            GROUP BY combined.id, combined.nombre, combined.nacionalidad
            HAVING total_puntos > 0
            ORDER BY total_puntos DESC, victorias DESC, podios DESC
        """;

        List<Map<String, Object>> clasificacion = new ArrayList<>();

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
            stmt.setInt(2, anio);
            ResultSet rs = stmt.executeQuery();

            int posicion = 1;
            while (rs.next()) {
                Map<String, Object> constructorData = new HashMap<>();
                constructorData.put("posicion", posicion++);
                constructorData.put("id", rs.getLong("id"));
                constructorData.put("nombre", rs.getString("nombre"));
                constructorData.put("nacionalidad", rs.getString("nacionalidad"));
                constructorData.put("puntos", rs.getBigDecimal("total_puntos"));
                constructorData.put("victorias", rs.getInt("victorias"));
                constructorData.put("podios", rs.getInt("podios"));
                clasificacion.add(constructorData);
            }

            logger.info("Clasificación de constructores obtenida: {} constructores", clasificacion.size());
            return clasificacion;

        } catch (Exception e) {
            logger.error("Error al obtener clasificación de constructores", e);
            throw new RuntimeException("Error al obtener clasificación de constructores", e);
        }
    }
}