package co.com.constants;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class F1PointsSystem {

    // Sistema de puntos para las primeras 10 posiciones
    private static final Map<Integer, BigDecimal> POINTS_MAP = new HashMap<>();

    static {
        POINTS_MAP.put(1, new BigDecimal("25"));   // 1er lugar
        POINTS_MAP.put(2, new BigDecimal("18"));   // 2do lugar
        POINTS_MAP.put(3, new BigDecimal("15"));   // 3er lugar
        POINTS_MAP.put(4, new BigDecimal("12"));   // 4to lugar
        POINTS_MAP.put(5, new BigDecimal("10"));   // 5to lugar
        POINTS_MAP.put(6, new BigDecimal("8"));    // 6to lugar
        POINTS_MAP.put(7, new BigDecimal("6"));    // 7mo lugar
        POINTS_MAP.put(8, new BigDecimal("4"));    // 8vo lugar
        POINTS_MAP.put(9, new BigDecimal("2"));    // 9no lugar
        POINTS_MAP.put(10, new BigDecimal("1"));   // 10mo lugar
    }

    // Punto extra por vuelta rápida (si termina en top 10)
    public static final BigDecimal FASTEST_LAP_POINTS = new BigDecimal("1");

    /**
     * Obtiene los puntos correspondientes a una posición.
     *
     * @param position Posición final (1-10 dan puntos, resto 0)
     * @return Puntos obtenidos
     */
    public static BigDecimal getPointsForPosition(int position) {
        return POINTS_MAP.getOrDefault(position, BigDecimal.ZERO);
    }

    /**
     * Calcula los puntos totales incluyendo vuelta rápida si aplica.
     *
     * @param position Posición final
     * @param hasFastestLap Si tiene la vuelta más rápida
     * @return Puntos totales
     */
    public static BigDecimal calculatePoints(int position, boolean hasFastestLap) {
        BigDecimal basePoints = getPointsForPosition(position);

        // Vuelta rápida solo suma si terminas en top 10
        if (hasFastestLap && position <= 10) {
            return basePoints.add(FASTEST_LAP_POINTS);
        }

        return basePoints;
    }

    /**
     * Verifica si una posición otorga puntos.
     *
     * @param position Posición a verificar
     * @return true si da puntos, false en caso contrario
     */
    public static boolean isPointScoringPosition(int position) {
        return position >= 1 && position <= 10;
    }
}

