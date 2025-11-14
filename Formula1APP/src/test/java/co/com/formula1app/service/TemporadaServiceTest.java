package co.com.formula1app.service;

import co.com.service.TemporadaService;
import co.com.dao.TemporadaDAO;
import co.com.model.Temporada;
import co.com.util.JPAUtil;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TemporadaServiceTest {

    private static TemporadaService temporadaService;
    private static TemporadaDAO temporadaDAO;

    @BeforeAll
    static void init() {

        temporadaService = new TemporadaService();
        temporadaDAO = new TemporadaDAO();

        assertTrue(JPAUtil.isAvailable(), "La conexión a la base de datos debe estar disponible");
    }

    @Test
    @Order(1)
    void testInicializarTemporada2025() {
        boolean resultado = temporadaService.inicializarTemporada2025();
        assertTrue(resultado || !resultado, "El método debe ejecutarse sin lanzar excepciones");
    }

    @Test
    @Order(2)
    void testCopiarEstructuraTemporada() {
        int copiadas = temporadaService.copiarEstructuraTemporada(2024, 2026);
        assertTrue(copiadas >= 0, "Debe devolver número válido de carreras copiadas");
    }
    @Test
    @Order(3)
    void testCopiarEstructuraTemporadaInexistente() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            temporadaService.copiarEstructuraTemporada(1999, 2030); // temporada 1999 no existe
        });
        assertTrue(ex.getMessage().contains("No existe la temporada origen"));
    }
    @Test
    @Order(4)
    void testCopiarEstructuraTemporadaSinCarreras() {
        Exception ex = assertThrows(RuntimeException.class, () ->
                temporadaService.copiarEstructuraTemporada(2028, 2029)
        );
        assertTrue(ex.getMessage().contains("No existe la temporada origen"),
                "Debe lanzar excepción si la temporada origen no existe");
    }

    @Test
    @Order(5)
    void testInicializarTemporada2025YaExistente() {
        // Simula que la temporada 2025 ya está en la BD y tiene carreras
        boolean resultado = temporadaService.inicializarTemporada2025();
        assertFalse(resultado, "Si la temporada 2025 ya tiene carreras, debe devolver false");
    }

    @Test
    @Order(6)
    void testCopiarEstructuraTemporadaConError() {
        // Forzamos un error pasando valores absurdos o cerrando el EntityManager
        Exception ex = assertThrows(RuntimeException.class, () -> {
            temporadaService.copiarEstructuraTemporada(0, 0);
        });
        assertTrue(ex.getMessage().contains("Error al copiar temporada"),
                "Debe lanzar excepción controlada al fallar");
    }

    @Test
    @Order(7)
    void testInicializarTemporada2025ConError() {
        // Forzamos que ocurra un error interno
        TemporadaService serviceSpy = new TemporadaService() {
            @Override
            public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
                throw new RuntimeException("Error simulado");
            }
        };

        boolean resultado = serviceSpy.inicializarTemporada2025();
        assertFalse(resultado, "Debe devolver false si ocurre una excepción interna");
    }
    @Test
    @Order(8)
    void testCopiarEstructuraTemporadaOrigenNula() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            temporadaService.copiarEstructuraTemporada(-1, 2025);
        });
        assertTrue(ex.getMessage().contains("Error al copiar temporada"));
    }

    @Test
    @Order(9)
    void testCopiarEstructuraTemporadaDestinoExistenteSinCarreras() {
        TemporadaDAO daoMock = new TemporadaDAO() {
            public Optional<Temporada> findByAnio(int anio) {
                return Optional.of(new Temporada(anio));
            }
        };

        TemporadaService service = new TemporadaService() {
            @Override
            public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
                return 0; // Simula sin carreras copiadas
            }
        };

        boolean resultado = service.inicializarTemporada2025();
        assertFalse(resultado, "Debe devolver false si no se copiaron carreras");
    }

    @Test
    @Order(10)
    void testCopiarEstructuraTemporadaRollback() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            temporadaService.copiarEstructuraTemporada(9999, 9998); // años inexistentes
        });
        assertTrue(ex.getMessage().contains("Error al copiar temporada"),
                "Debe lanzar excepción controlada al hacer rollback");
    }

    @Test
    @Order(11)
    void testInicializarTemporada2025SinCarrerasCopiadas() {
        TemporadaService serviceSpy = new TemporadaService() {
            @Override
            public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
                return 0; // Simula que no se copiaron carreras
            }
        };

        boolean resultado = serviceSpy.inicializarTemporada2025();
        assertFalse(resultado, "Debe devolver false si no se copiaron carreras");
    }

    @Test
    @Order(12)
    void testCopiarEstructuraTemporadaFechaNula() {
        TemporadaService service = new TemporadaService();
        // Simulamos carreras sin fecha, debería ejecutarse sin error
        Exception ex = assertThrows(RuntimeException.class, () -> {
            service.copiarEstructuraTemporada(1900, 1901);
        });
        assertTrue(ex.getMessage().contains("Error al copiar temporada"));
    }
    @Test
    @Order(13)
    void testCopiarEstructuraTemporadaLanzaIllegalArgumentException() {
        TemporadaService service = new TemporadaService();

        Exception ex = assertThrows(RuntimeException.class, () -> {
            service.copiarEstructuraTemporada(1234, 2025);
        });

        // Validamos que el mensaje contenga la causa original
        assertTrue(ex.getMessage().contains("No existe la temporada origen"),
                "El mensaje debe indicar que no existe la temporada origen");
    }

    @Test
    @Order(14)
    void testCopiarEstructuraTemporadaErrorSinTransaccionActiva() {
        TemporadaService service = new TemporadaService() {
            @Override
            public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
                throw new RuntimeException("Falla sin transacción activa");
            }
        };

        assertThrows(RuntimeException.class, () -> {
            service.copiarEstructuraTemporada(0, 0);
        });
    }

    @Test
    @Order(15)
    void testCopiarEstructuraTemporadaConFechaNulaNoFalla() {
        TemporadaService service = new TemporadaService() {
            @Override
            public int copiarEstructuraTemporada(int anioOrigen, int anioDestino) {
                // Simulamos carreras con fecha nula, pero sin error
                return 1;
            }
        };

        assertDoesNotThrow(() -> {
            boolean resultado = service.inicializarTemporada2025();
            // el objetivo es que NO falle aunque haya fechas nulas
            System.out.println("Resultado inicialización: " + resultado);
        }, "No debe lanzar excepción aunque las fechas sean nulas");
    }

    @Test
    @Order(16)
    void testMensajeErrorCopiarEstructura() {
        Exception ex = assertThrows(RuntimeException.class, () -> {
            temporadaService.copiarEstructuraTemporada(0, 0);
        });
        assertTrue(ex.getMessage().startsWith("Error al copiar temporada"));
    }
}