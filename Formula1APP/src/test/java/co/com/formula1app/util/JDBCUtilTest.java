package co.com.formula1app.util;

import co.com.util.JDBCUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JDBCUtilTest {

    private static Connection mockConn;
    private static Statement mockStmt;
    private static ResultSet mockRs;

    @BeforeAll
    static void setUp() throws Exception {
        mockConn = mock(Connection.class);
        mockStmt = mock(Statement.class);
        mockRs = mock(ResultSet.class);
    }

    @AfterAll
    static void tearDown() {
        JDBCUtil.close();
    }

    @Test
    void testConexionExitosa() {
        // Solo verificamos que el método no lance excepción
        assertDoesNotThrow(JDBCUtil::testConnection,
                "El test de conexión no debe lanzar excepción incluso si la base no está disponible");
    }

    @Test
    void testObtenerConexionYcerrar() throws Exception {
        Connection conn = JDBCUtil.getConnection();
        assertNotNull(conn, "La conexión no debe ser nula");
        assertFalse(conn.isClosed(), "La conexión no debe estar cerrada al obtenerse");

        JDBCUtil.close(conn);
        assertTrue(conn.isClosed(), "La conexión debe cerrarse correctamente");
    }

    @Test
    void testCerrarRecursosNulosNoLanzaExcepcion() {
        assertDoesNotThrow(() -> JDBCUtil.close((Connection) null));
        assertDoesNotThrow(() -> JDBCUtil.close((Statement) null));
        assertDoesNotThrow(() -> JDBCUtil.close((PreparedStatement) null));
        assertDoesNotThrow(() -> JDBCUtil.close((ResultSet) null));
    }

    @Test
    void testCerrarRecursosMockeados() throws SQLException {
        PreparedStatement mockPstmt = mock(PreparedStatement.class);

        JDBCUtil.close(mockRs);
        JDBCUtil.close(mockStmt);
        JDBCUtil.close(mockPstmt);
        JDBCUtil.close(mockConn);

        verify(mockRs, atMostOnce()).close();
        verify(mockStmt, atMostOnce()).close();
        verify(mockPstmt, atMostOnce()).close();
        verify(mockConn, atMostOnce()).close();
    }

    @Test
    void testRollbackConTransaccionActiva() throws SQLException {
        when(mockConn.getAutoCommit()).thenReturn(false);
        assertDoesNotThrow(() -> JDBCUtil.rollback(mockConn));
        verify(mockConn, times(1)).rollback();
    }

    @Test
    void testGetPoolStatsNoDisponible() throws Exception {
        // Simulamos cierre del pool
        JDBCUtil.close();
        String result = JDBCUtil.getPoolStats();
        assertEquals("Pool no disponible", result);
    }

    @Test
    void testConstructorPrivado() throws Exception {
        var constructor = JDBCUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Exception ex = assertThrows(Exception.class, constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        assertEquals("Esta es una clase utilitaria y no debe ser instanciada", ex.getCause().getMessage());
    }
}