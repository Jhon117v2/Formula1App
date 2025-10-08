package co.com.config;

import java.time.LocalDate;

public class ConfiguracionTemporada {

    // Temporadas disponibles
    public static final int TEMPORADA_2024 = 2024;
    public static final int TEMPORADA_2025 = 2025;
    public static final int TEMPORADA_ACTUAL = TEMPORADA_2025;

    // Fecha de congelación del proyecto
    // Después de esta fecha, se pueden ingresar resultados manualmente
    public static final LocalDate FECHA_CONGELACION = LocalDate.of(2025, 1, 7);

    /**
     * Verifica si una fecha está después de la fecha de congelación.
     *
     * @param fecha Fecha a verificar
     * @return true si está después de la congelación
     */
    public static boolean esDespuesDeCongelacion(LocalDate fecha) {
        return fecha.isAfter(FECHA_CONGELACION);
    }

    /**
     * Verifica si una carrera permite ingreso manual de resultados.
     *
     * @param fechaCarrera Fecha de la carrera
     * @return true si permite ingreso manual
     */
    public static boolean permiteIngresoManual(LocalDate fechaCarrera) {
        return esDespuesDeCongelacion(fechaCarrera) || fechaCarrera.equals(FECHA_CONGELACION);
    }

    /**
     * Obtiene un mensaje sobre el estado de congelación.
     *
     * @return Mensaje informativo
     */
    public static String getMensajeCongelacion() {
        return String.format(
                "Fecha de congelación del proyecto: %s\n" +
                        "Las carreras posteriores a esta fecha permiten ingreso manual de resultados.",
                FECHA_CONGELACION
        );
    }
}
