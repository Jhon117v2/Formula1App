package co.com.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase utilitaria para la gestión de conexiones JDBC utilizando HikariCP.
 * Proporciona un pool de conexiones optimizado para alto rendimiento.
 */
public class JDBCUtil {
    private static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
    private static HikariDataSource dataSource;

    // Configuración de la base de datos
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/f1_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    // Bloque estático para inicializar el pool de conexiones
    static {
        try {
            logger.info("Inicializando pool de conexiones JDBC con HikariCP");

            HikariConfig config = new HikariConfig();

            // Configuración básica
            config.setJdbcUrl(JDBC_URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.setDriverClassName(DRIVER_CLASS);

            // Configuración del pool
            config.setMaximumPoolSize(10);              // Máximo de conexiones en el pool
            config.setMinimumIdle(5);                   // Mínimo de conexiones inactivas
            config.setConnectionTimeout(30000);         // Timeout para obtener conexión (30s)
            config.setIdleTimeout(600000);              // Timeout para conexiones inactivas (10m)
            config.setMaxLifetime(1800000);             // Vida máxima de una conexión (30m)
            config.setAutoCommit(true);                 // Auto-commit habilitado por defecto
            config.setConnectionTestQuery("SELECT 1");  // Query para validar conexiones

            // Configuración de pool adicional
            config.setPoolName("F1ManagerPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            dataSource = new HikariDataSource(config);

            logger.info("Pool de conexiones JDBC inicializado correctamente");
            logger.info("URL: {}", JDBC_URL);
            logger.info("Pool size: {} (min) - {} (max)", config.getMinimumIdle(), config.getMaximumPoolSize());

            // Registrar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Ejecutando shutdown hook para cerrar pool de conexiones JDBC");
                close();
            }));

            // Verificar conectividad
            testConnection();

        } catch (Exception e) {
            logger.error("Error crítico al inicializar pool de conexiones JDBC", e);
            logger.error("Verifique que MySQL esté ejecutándose y las credenciales sean correctas");
            throw new ExceptionInInitializerError("Error al inicializar JDBC: " + e.getMessage());
        }
    }

    /**
     * Constructor privado para prevenir instanciación
     */
    private JDBCUtil() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una conexión del pool.
     * La conexión debe ser cerrada después de su uso.
     *
     * @return Connection conexión de base de datos
     * @throws SQLException si no se puede obtener una conexión
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            logger.error("DataSource no está disponible o ha sido cerrado");
            throw new SQLException("Pool de conexiones no disponible");
        }

        try {
            Connection conn = dataSource.getConnection();
            logger.debug("Conexión obtenida del pool. Conexiones activas: {}",
                    dataSource.getHikariPoolMXBean().getActiveConnections());
            return conn;
        } catch (SQLException e) {
            logger.error("Error al obtener conexión del pool", e);
            throw e;
        }
    }

    /**
     * Cierra el pool de conexiones.
     * Este método debe ser llamado al finalizar la aplicación.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                logger.info("Cerrando pool de conexiones JDBC...");
                logger.info("Conexiones activas antes de cerrar: {}",
                        dataSource.getHikariPoolMXBean().getActiveConnections());

                dataSource.close();
                logger.info("Pool de conexiones JDBC cerrado correctamente");
            } catch (Exception e) {
                logger.error("Error al cerrar pool de conexiones JDBC", e);
            }
        } else {
            logger.debug("Pool de conexiones ya estaba cerrado o es null");
        }
    }

    /**
     * Cierra recursos JDBC de forma segura (Connection, Statement, ResultSet).
     *
     * @param conn Connection a cerrar
     * @param stmt Statement a cerrar
     * @param rs ResultSet a cerrar
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }

    /**
     * Cierra una conexión de forma segura.
     *
     * @param conn Connection a cerrar
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Conexión cerrada y devuelta al pool");
            } catch (SQLException e) {
                logger.error("Error al cerrar conexión", e);
            }
        }
    }

    /**
     * Cierra un Statement de forma segura.
     *
     * @param stmt Statement a cerrar
     */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
                logger.debug("Statement cerrado");
            } catch (SQLException e) {
                logger.error("Error al cerrar Statement", e);
            }
        }
    }

    /**
     * Cierra un PreparedStatement de forma segura.
     *
     * @param pstmt PreparedStatement a cerrar
     */
    public static void close(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
                logger.debug("PreparedStatement cerrado");
            } catch (SQLException e) {
                logger.error("Error al cerrar PreparedStatement", e);
            }
        }
    }

    /**
     * Cierra un ResultSet de forma segura.
     *
     * @param rs ResultSet a cerrar
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                logger.debug("ResultSet cerrado");
            } catch (SQLException e) {
                logger.error("Error al cerrar ResultSet", e);
            }
        }
    }

    /**
     * Realiza rollback de una transacción de forma segura.
     *
     * @param conn Connection con transacción activa
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                    logger.debug("Rollback ejecutado");
                }
            } catch (SQLException e) {
                logger.error("Error al realizar rollback", e);
            }
        }
    }

    /**
     * Verifica la conectividad con la base de datos.
     *
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                logger.info("Test de conexión exitoso");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Test de conexión falló", e);
        }
        return false;
    }

    /**
     * Obtiene estadísticas del pool de conexiones.
     *
     * @return String con estadísticas del pool
     */
    public static String getPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            return String.format(
                    "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool no disponible";
    }

    /**
     * Verifica si el pool de conexiones está disponible.
     *
     * @return true si está disponible, false en caso contrario
     */
    public static boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
}
