package co.com;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import co.com.model.Carrera;
import co.com.model.Resultado;
import co.com.service.F1Servicio;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Clase principal con menú de consola para consultar datos de temporadas de Formula 1.
 */
public class Main {
    private static final F1Servicio servicio = new F1Servicio();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\n=== Menú Principal - Formula 1 ===");
        System.out.println("Seleccione la temporada:");
        System.out.println("1. 2024");
        System.out.println("2. 2025");
        System.out.print("Opción: ");
        int opcionTemporada = scanner.nextInt();
        scanner.nextLine(); // Limpiar buffer

        int anio = (opcionTemporada == 1) ? 2024 : 2025;

        if (anio == 2025) {
            System.out.println("Nota: Actualmente solo hay datos para la temporada 2024 en la base de datos. Para 2025 se mostrarán datos disponibles (posiblemente vacíos).");
        }

        boolean salir = false;

        while (!salir) {
            System.out.println("\n=== Menú Formula 1 - Temporada " + anio + " ===");
            System.out.println("1. Ver calendario de carreras");
            System.out.println("2. Ver clasificación de pilotos");
            System.out.println("3. Ver clasificación de constructores");
            System.out.println("4. Ver resultados de una carrera");
            System.out.println("5. Ver circuitos");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            switch (opcion) {
                case 1:
                    mostrarCalendario(anio);
                    break;
                case 2:
                    mostrarClasificacionPilotos(anio);
                    break;
                case 3:
                    mostrarClasificacionConstructores(anio);
                    break;
                case 4:
                    mostrarResultadosCarrera(anio);
                    break;
                case 5:
                    mostrarCircuitos(anio);
                    break;
                case 6:
                    salir = true;
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
            }
        }

        scanner.close();
    }

    private static void mostrarCalendario(int anio) {
        List<Carrera> carreras = servicio.getCarreras(anio);
        if (carreras.isEmpty()) {
            System.out.println("No hay carreras registradas para " + anio + ".");
            return;
        }

        System.out.println("\nCalendario de Carreras " + anio + ":");
        System.out.printf("%-5s %-30s %-15s %-10s %-20s%n",
                "GP#", "Nombre GP", "Fecha", "ID", "Circuito");
        for (Carrera c : carreras) {
            System.out.printf("%-5d %-30s %-15s %-10d %-20s%n",
                    c.getGpNumero(), c.getNombreGp(), c.getFecha(),
                    c.getId(), c.getCircuito() != null ? c.getCircuito().getNombre() : "N/A");
        }
    }

    private static void mostrarClasificacionPilotos(int anio) {
        List<Map<String, Object>> clasificacion = servicio.getClasificacionPilotos(anio);
        if (clasificacion.isEmpty()) {
            System.out.println("No hay datos de clasificación de pilotos para " + anio + ".");
            return;
        }

        System.out.println("\nClasificación de Pilotos " + anio + ":");
        System.out.printf("%-5s %-25s %-10s %-15s %-20s %-10s %-10s %-10s%n",
                "Pos", "Piloto", "Dorsal", "Nacionalidad", "Constructor", "Puntos", "Victorias", "Podios");
        for (Map<String, Object> p : clasificacion) {
            System.out.printf("%-5d %-25s %-10s %-15s %-20s %-10s %-10d %-10d%n",
                    p.get("posicion"), p.get("nombre"), p.get("dorsal"),
                    p.get("nacionalidad"), p.get("constructor"), p.get("puntos"),
                    p.get("victorias"), p.get("podios"));
        }
    }

    private static void mostrarClasificacionConstructores(int anio) {
        List<Map<String, Object>> clasificacion = servicio.getClasificacionConstructores(anio);
        if (clasificacion.isEmpty()) {
            System.out.println("No hay datos de clasificación de constructores para " + anio + ".");
            return;
        }

        System.out.println("\nClasificación de Constructores " + anio + ":");
        System.out.printf("%-5s %-25s %-15s %-10s %-10s %-10s%n",
                "Pos", "Constructor", "Nacionalidad", "Puntos", "Victorias", "Podios");
        for (Map<String, Object> c : clasificacion) {
            System.out.printf("%-5d %-25s %-15s %-10s %-10d %-10d%n",
                    c.get("posicion"), c.get("nombre"), c.get("nacionalidad"),
                    c.get("puntos"), c.get("victorias"), c.get("podios"));
        }
    }

    private static void mostrarResultadosCarrera(int anio) {
        System.out.print("Ingrese el ID de la carrera: ");
        Long id = scanner.nextLong();
        scanner.nextLine();

        Optional<Carrera> carreraOpt = servicio.getCarreraById(id);
        if (carreraOpt.isEmpty()) {
            System.out.println("No se encontró la carrera con ID: " + id);
            return;
        }
        Carrera carrera = carreraOpt.get();

        if (!carrera.getTemporada().getAnio().equals(anio)) {
            System.out.println("La carrera con ID " + id + " no pertenece a la temporada " + anio + ".");
            return;
        }

        List<Resultado> resultados = servicio.getResultadosCarrera(id);
        if (resultados.isEmpty()) {
            System.out.println("No hay resultados para la carrera: " + carrera.getNombreGp());
            return;
        }

        System.out.println("\nResultados de la Carrera: " + carrera.getNombreGp() + " (" + carrera.getFecha() + ")");
        System.out.printf("%-5s %-25s %-20s %-10s %-10s %-10s %-15s%n",
                "Pos", "Piloto", "Constructor", "Puntos", "Vueltas", "Tiempo", "Estado");
        for (Resultado r : resultados) {
            String estado = r.getRetirado() ? "Retirado (" + (r.getMotivoRetiro() != null ? r.getMotivoRetiro() : "") + ")" : "Finalizado";
            System.out.printf("%-5d %-25s %-20s %-10s %-10d %-10s %-15s%n",
                    r.getPosicionFinal(), r.getPiloto().getNombre(),
                    r.getPiloto().getConstructor() != null ? r.getPiloto().getConstructor().getNombre() : "N/A",
                    r.getPuntosObtenidos(), r.getVueltas(), r.getTiempo(), estado);
        }
    }

    private static void mostrarCircuitos(int anio) {
        List<co.com.model.Circuito> circuitos = servicio.getCircuitos(anio);
        if (circuitos.isEmpty()) {
            System.out.println("No hay circuitos registrados para " + anio + ".");
            return;
        }

        System.out.println("\nCircuitos " + anio + ":");
        System.out.printf("%-30s %-20s%n", "Nombre", "Ubicación");
        for (co.com.model.Circuito c : circuitos) {
            System.out.printf("%-30s %-20s%n", c.getNombre(), c.getUbicacion());
        }
    }
}