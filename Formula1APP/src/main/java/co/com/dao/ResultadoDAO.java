package co.com.dao;

import co.com.model.Resultado;
import co.com.util.JPAUtil;
import co.com.util.JDBCUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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
                p.id,
                p.nombre,
                p.dorsal,
                p.nacionalidad,
                c.nombre as constructor,
                COALESCE(SUM(r.puntos_obtenidos), 0) as total_puntos,
                COUNT(CASE WHEN r.posicion_final = 1 THEN 1 END) as victorias,
                COUNT(CASE WHEN r.posicion_final <= 3 THEN 1 END) as podios
            FROM piloto p
            LEFT JOIN constructor c ON p.constructor_id = c.id
            LEFT JOIN resultado r ON r.piloto_id = p.id
            LEFT JOIN carrera ca ON r.carrera_id = ca.id
            LEFT JOIN temporada t ON ca.temporada_id = t.id
            WHERE t.anio = ? OR t.anio IS NULL
            GROUP BY p.id, p.nombre, p.dorsal, p.nacionalidad, c.nombre
            HAVING total_puntos > 0
            ORDER BY total_puntos DESC, victorias DESC, podios DESC
        """;

        List<Map<String, Object>> clasificacion = new ArrayList<>();

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
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
                c.id,
                c.nombre,
                c.nacionalidad,
                COALESCE(SUM(r.puntos_obtenidos), 0) as total_puntos,
                COUNT(CASE WHEN r.posicion_final = 1 THEN 1 END) as victorias,
                COUNT(CASE WHEN r.posicion_final <= 3 THEN 1 END) as podios
            FROM constructor c
            LEFT JOIN piloto p ON p.constructor_id = c.id
            LEFT JOIN resultado r ON r.piloto_id = p.id
            LEFT JOIN carrera ca ON r.carrera_id = ca.id
            LEFT JOIN temporada t ON ca.temporada_id = t.id
            WHERE t.anio = ? OR t.anio IS NULL
            GROUP BY c.id, c.nombre, c.nacionalidad
            HAVING total_puntos > 0
            ORDER BY total_puntos DESC, victorias DESC, podios DESC
        """;

        List<Map<String, Object>> clasificacion = new ArrayList<>();

        try (Connection conn = JDBCUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, anio);
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
