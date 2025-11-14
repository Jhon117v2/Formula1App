package co.com.util;

import co.com.util.JDBCUtil;
import co.com.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase para gestionar conexiones a la base de datos, tanto para JPA (EntityManager) como para JDBC (Connection).
 * Esto centraliza el manejo de conexiones, aplicando SRP (responsabilidad única para gestión de conexiones)
 * y DIP (los DAOs dependerán de esta abstracción en lugar de utilitarios concretos como JPAUtil o JDBCUtil).
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    public EntityManager getEntityManager() {
        return JPAUtil.getEntityManager();
    }

    public void closeEntityManager(EntityManager em) {
        JPAUtil.close(em);
    }

    public Connection getConnection() throws SQLException {
        return JDBCUtil.getConnection();
    }

    // No se necesita close para Connection ya que se usa try-with-resources en los DAOs
}