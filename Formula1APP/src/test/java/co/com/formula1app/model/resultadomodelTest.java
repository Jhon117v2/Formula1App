
package co.com.formula1app.model;
import co.com.dao.ResultadoDAO;
import co.com.model.Carrera;
import co.com.model.Piloto;
import co.com.model.Resultado;
import co.com.util.JPAUtil;
import co.com.util.JDBCUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResultadoDAOTest {

    private ResultadoDAO resultadoDAO;
    private Resultado resultado;
    private Carrera carrera;
    private Piloto piloto;

    @BeforeEach
    void setUp() {
        resultadoDAO = new ResultadoDAO();

        carrera = new Carrera();
        carrera.setId(1L);

        piloto = new Piloto();
        piloto.setId(1L);

        resultado = new Resultado();
        resultado.setCarrera(carrera);
        resultado.setPiloto(piloto);
        resultado.setPosicionFinal(1);
        resultado.setPuntosObtenidos(new BigDecimal("25.00"));
    }

    // ---------- TESTS DEL MÉTODO findByCarrera ----------

    @Test
    @Order(1)
    void testFindByCarrera_Exito() {
        EntityManager emMock = mock(EntityManager.class);
        TypedQuery<Resultado> queryMock = mock(TypedQuery.class);
        List<Resultado> listaEsperada = List.of(resultado);

        when(emMock.createQuery(any(String.class), eq(Resultado.class))).thenReturn(queryMock);
        when(queryMock.setParameter(any(String.class), any())).thenReturn(queryMock);
        when(queryMock.getResultList()).thenReturn(listaEsperada);

        try (MockedStatic<JPAUtil> jpa = Mockito.mockStatic(JPAUtil.class)) {
            jpa.when(JPAUtil::getEntityManager).thenReturn(emMock);
            jpa.when(() -> JPAUtil.close(any())).thenAnswer(inv -> null);

            List<Resultado> result = resultadoDAO.findByCarrera(1L);
            assertEquals(1, result.size());
            assertEquals(listaEsperada, result);
        }
    }

    @Test
    @Order(2)
    void testFindByCarrera_IdNulo() {
        assertThrows(RuntimeException.class, () -> resultadoDAO.findByCarrera(null));
    }

    @Test
    @Order(3)
    void testFindByCarrera_ErrorEnQuery() {
        EntityManager emMock = mock(EntityManager.class);
        when(emMock.createQuery(any(String.class), eq(Resultado.class))).thenThrow(new RuntimeException("Error de prueba"));

        try (MockedStatic<JPAUtil> jpa = Mockito.mockStatic(JPAUtil.class)) {
            jpa.when(JPAUtil::getEntityManager).thenReturn(emMock);
            jpa.when(() -> JPAUtil.close(any())).thenAnswer(inv -> null);

            assertThrows(RuntimeException.class, () -> resultadoDAO.findByCarrera(1L));
        }
    }

    // ---------- TESTS DEL MÉTODO save ----------

    @Test
    @Order(4)
    void testSave_Exito() {
        EntityManager emMock = mock(EntityManager.class);

        when(emMock.getTransaction()).thenReturn(mock(jakarta.persistence.EntityTransaction.class));

        try (MockedStatic<JPAUtil> jpa = Mockito.mockStatic(JPAUtil.class)) {
            jpa.when(JPAUtil::getEntityManager).thenReturn(emMock);
            jpa.when(() -> JPAUtil.close(any())).thenAnswer(inv -> null);

            Resultado guardado = resultadoDAO.save(resultado);
            assertNotNull(guardado);
            assertEquals(resultado, guardado);
        }
    }

    @Test
    @Order(5)
    void testSave_Nulo() {
        assertThrows(RuntimeException.class, () -> resultadoDAO.save(null));
    }

    @Test
    @Order(6)
    void testSave_SinCarrera() {
        resultado.setCarrera(null);
        assertThrows(RuntimeException.class, () -> resultadoDAO.save(resultado));
    }

    @Test
    @Order(7)
    void testSave_SinPiloto() {
        resultado.setPiloto(null);
        assertThrows(RuntimeException.class, () -> resultadoDAO.save(resultado));
    }

    @Test
    @Order(8)
    void testSave_ErrorAlPersistir() {
        EntityManager emMock = mock(EntityManager.class);
        jakarta.persistence.EntityTransaction tx = mock(jakarta.persistence.EntityTransaction.class);

        when(emMock.getTransaction()).thenReturn(tx);
        doThrow(new RuntimeException("Fallo persistencia")).when(emMock).persist(any(Resultado.class));
        when(tx.isActive()).thenReturn(true);

        try (MockedStatic<JPAUtil> jpa = Mockito.mockStatic(JPAUtil.class)) {
            jpa.when(JPAUtil::getEntityManager).thenReturn(emMock);
            jpa.when(() -> JPAUtil.close(any())).thenAnswer(inv -> null);

            assertThrows(RuntimeException.class, () -> resultadoDAO.save(resultado));
        }
    }

    // ---------- TESTS DEL MÉTODO getClasificacionPilotos ----------

    @Test
    @Order(9)
    void testGetClasificacionPilotos_Exito() throws Exception {
        Connection connMock = mock(Connection.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);

        when(connMock.prepareStatement(any(String.class))).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true, false);
        when(rsMock.getLong("id")).thenReturn(1L);
        when(rsMock.getString("nombre")).thenReturn("Lewis Hamilton");
        when(rsMock.getString("dorsal")).thenReturn("44");
        when(rsMock.getString("nacionalidad")).thenReturn("Británica");
        when(rsMock.getString("constructor")).thenReturn("Mercedes");
        when(rsMock.getBigDecimal("total_puntos")).thenReturn(new BigDecimal("387.5"));
        when(rsMock.getInt("victorias")).thenReturn(9);
        when(rsMock.getInt("podios")).thenReturn(15);

        try (MockedStatic<JDBCUtil> jdbc = Mockito.mockStatic(JDBCUtil.class)) {
            jdbc.when(JDBCUtil::getConnection).thenReturn(connMock);

            List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionPilotos(2024);
            assertEquals(1, clasificacion.size());
            assertEquals("Lewis Hamilton", clasificacion.get(0).get("nombre"));
        }
    }

    @Test
    @Order(10)
    void testGetClasificacionPilotos_ErrorSQL() throws Exception {
        try (MockedStatic<JDBCUtil> jdbc = Mockito.mockStatic(JDBCUtil.class)) {
            jdbc.when(JDBCUtil::getConnection).thenThrow(new RuntimeException("Error SQL simulado"));
            assertThrows(RuntimeException.class, () -> resultadoDAO.getClasificacionPilotos(2024));
        }
    }

    // ---------- TESTS DEL MÉTODO getClasificacionConstructores ----------

    @Test
    @Order(11)
    void testGetClasificacionConstructores_Exito() throws Exception {
        Connection connMock = mock(Connection.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);

        when(connMock.prepareStatement(any(String.class))).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true, false);
        when(rsMock.getLong("id")).thenReturn(10L);
        when(rsMock.getString("nombre")).thenReturn("Red Bull Racing");
        when(rsMock.getString("nacionalidad")).thenReturn("Austríaca");
        when(rsMock.getBigDecimal("total_puntos")).thenReturn(new BigDecimal("765.0"));
        when(rsMock.getInt("victorias")).thenReturn(20);
        when(rsMock.getInt("podios")).thenReturn(25);

        try (MockedStatic<JDBCUtil> jdbc = Mockito.mockStatic(JDBCUtil.class)) {
            jdbc.when(JDBCUtil::getConnection).thenReturn(connMock);

            List<Map<String, Object>> clasificacion = resultadoDAO.getClasificacionConstructores(2024);
            assertEquals(1, clasificacion.size());
            assertEquals("Red Bull Racing", clasificacion.get(0).get("nombre"));
        }
    }

    @Test
    @Order(12)
    void testGetClasificacionConstructores_ErrorSQL() {
        try (MockedStatic<JDBCUtil> jdbc = Mockito.mockStatic(JDBCUtil.class)) {
            jdbc.when(JDBCUtil::getConnection).thenThrow(new RuntimeException("Error SQL simulado"));
            assertThrows(RuntimeException.class, () -> resultadoDAO.getClasificacionConstructores(2024));
        }
    }
}
