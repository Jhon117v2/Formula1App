package co.com;

import co.com.config.ConfiguracionTemporada;
import co.com.model.Carrera;
import co.com.model.Circuito;
import co.com.model.Piloto;
import co.com.model.Resultado;
import co.com.service.F1Servicio;
import co.com.service.ResultadoService;
import co.com.service.ResultadoService.ResultadoDTO;
import co.com.service.TemporadaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Clase principal con menú de consola para consultar datos de temporadas de Formula 1.
 */
public class Main {
    private static final F1Servicio servicio = new F1Servicio();
    private static final ResultadoService resultadoService = new ResultadoService();
    private static final TemporadaService temporadaService = new TemporadaService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final String SEPARADOR_GRUESO = "=".repeat(100);
    private static final String SEPARADOR_FINO = "-".repeat(100);

    private static int temporadaActual = 2024;

    public static void main(String[] args) {
        seleccionarTemporada();

        boolean salir = false;

        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerOpcion();

            switch (opcion) {
                case 1 -> mostrarCalendario(temporadaActual);
                case 2 -> mostrarClasificacionPilotos(temporadaActual);
                case 3 -> mostrarClasificacionConstructores(temporadaActual);
                case 4 -> mostrarResultadosCarrera(temporadaActual);
                case 5 -> mostrarCircuitos(temporadaActual);
                case 6 -> {
                    if (temporadaActual == 2025) {
                        menuGestionResultados2025();
                    } else {
                        imprimirMensaje("La gestión de resultados solo está disponible para la temporada 2025.", "ADVERTENCIA");
                    }
                }
                case 7 -> cambiarTemporada();
                case 0 -> {
                    salir = true;
                    imprimirMensaje("Gracias por usar F1 Manager. Hasta pronto!", "INFO");
                }
                default -> imprimirMensaje("Opción inválida. Intente nuevamente.", "ERROR");
            }

            if (!salir && opcion != 0) {
                esperarEnter();
            }
        }

        scanner.close();
    }

    private static void seleccionarTemporada() {
        imprimirEncabezado("SELECCIÓN DE TEMPORADA");
        System.out.println("1. Temporada 2024 (Solo consulta - Datos históricos)");
        System.out.println("2. Temporada 2025 (Consulta + Ingreso manual de resultados)");
        System.out.println(SEPARADOR_FINO);
        System.out.print("Seleccione temporada: ");

        int opcion = leerOpcion();
        temporadaActual = (opcion == 1) ? 2024 : 2025;

        imprimirMensaje("Temporada seleccionada: " + temporadaActual, "EXITO");

        if (temporadaActual == 2025) {
            System.out.println("\nInformación importante:");
            System.out.println(ConfiguracionTemporada.getMensajeCongelacion());
            System.out.println("Puede ingresar resultados manualmente para carreras posteriores a esta fecha.");
        }
    }

    private static void mostrarMenuPrincipal() {
        imprimirEncabezado("MENU PRINCIPAL - TEMPORADA " + temporadaActual);

        System.out.println("CONSULTAS");
        System.out.println("  1. Ver calendario de carreras");
        System.out.println("  2. Ver clasificación de pilotos");
        System.out.println("  3. Ver clasificación de constructores");
        System.out.println("  4. Ver resultados de una carrera");
        System.out.println("  5. Ver circuitos");

        if (temporadaActual == 2025) {
            System.out.println("\nGESTION (Solo temporada 2025)");
            System.out.println("  6. Gestión de resultados (Ingresar/Modificar/Eliminar)");
        }

        System.out.println("\nOPCIONES");
        System.out.println("  7. Cambiar de temporada");
        System.out.println("  0. Salir");
        System.out.println(SEPARADOR_FINO);
        System.out.print("Seleccione una opción: ");
    }

    private static void cambiarTemporada() {
        seleccionarTemporada();
    }

    // ============================================================================
    // MÉTODOS DE CONSULTA
    // ============================================================================

    private static void mostrarCalendario(int anio) {
        List<Carrera> carreras = servicio.getCarreras(anio);

        if (carreras.isEmpty()) {
            imprimirMensaje("No hay carreras registradas para " + anio + ".", "ADVERTENCIA");
            return;
        }

        imprimirEncabezado("CALENDARIO DE CARRERAS " + anio);
        System.out.printf("%-5s %-35s %-15s %-8s %-30s%n",
                "GP#", "Nombre del Gran Premio", "Fecha", "ID", "Circuito");
        System.out.println(SEPARADOR_FINO);

        for (Carrera c : carreras) {
            String circuito = c.getCircuito() != null ? c.getCircuito().getNombre() : "N/A";
            System.out.printf("%-5d %-35s %-15s %-8d %-30s%n",
                    c.getGpNumero(),
                    truncar(c.getNombreGp(), 35),
                    c.getFecha(),
                    c.getId(),
                    truncar(circuito, 30));
        }

        System.out.println(SEPARADOR_GRUESO);
        System.out.println("Total de carreras: " + carreras.size());
    }

    private static void mostrarClasificacionPilotos(int anio) {
        List<Map<String, Object>> clasificacion = servicio.getClasificacionPilotos(anio);

        if (clasificacion.isEmpty()) {
            imprimirMensaje("No hay datos de clasificación de pilotos para " + anio + ".", "ADVERTENCIA");
            return;
        }

        imprimirEncabezado("CAMPEONATO MUNDIAL DE PILOTOS " + anio);
        System.out.printf("%-5s %-30s %-8s %-20s %-25s %-10s %-10s %-10s%n",
                "Pos", "Piloto", "Dorsal", "Nacionalidad", "Escuderia", "Puntos", "Victorias", "Podios");
        System.out.println(SEPARADOR_FINO);

        for (Map<String, Object> p : clasificacion) {
            System.out.printf("%-5d %-30s #%-7s %-20s %-25s %-10s %-10d %-10d%n",
                    p.get("posicion"),
                    truncar(p.get("nombre").toString(), 30),
                    p.get("dorsal"),
                    truncar(p.get("nacionalidad").toString(), 20),
                    truncar(p.get("constructor").toString(), 25),
                    p.get("puntos"),
                    p.get("victorias"),
                    p.get("podios"));
        }

        System.out.println(SEPARADOR_GRUESO);
    }

    private static void mostrarClasificacionConstructores(int anio) {
        List<Map<String, Object>> clasificacion = servicio.getClasificacionConstructores(anio);

        if (clasificacion.isEmpty()) {
            imprimirMensaje("No hay datos de clasificación de constructores para " + anio + ".", "ADVERTENCIA");
            return;
        }

        imprimirEncabezado("CAMPEONATO MUNDIAL DE CONSTRUCTORES " + anio);
        System.out.printf("%-5s %-35s %-20s %-12s %-12s %-12s%n",
                "Pos", "Escuderia", "Nacionalidad", "Puntos", "Victorias", "Podios");
        System.out.println(SEPARADOR_FINO);

        for (Map<String, Object> c : clasificacion) {
            System.out.printf("%-5d %-35s %-20s %-12s %-12d %-12d%n",
                    c.get("posicion"),
                    truncar(c.get("nombre").toString(), 35),
                    truncar(c.get("nacionalidad").toString(), 20),
                    c.get("puntos"),
                    c.get("victorias"),
                    c.get("podios"));
        }

        System.out.println(SEPARADOR_GRUESO);
    }

    private static void mostrarResultadosCarrera(int anio) {
        System.out.print("\nIngrese el ID de la carrera: ");
        Long id = (long) leerOpcion();

        Optional<Carrera> carreraOpt = servicio.getCarreraById(id);
        if (carreraOpt.isEmpty()) {
            imprimirMensaje("No se encontró la carrera con ID: " + id, "ERROR");
            return;
        }

        Carrera carrera = carreraOpt.get();

        if (!carrera.getTemporada().getAnio().equals(anio)) {
            imprimirMensaje("La carrera con ID " + id + " no pertenece a la temporada " + anio + ".", "ERROR");
            return;
        }

        List<Resultado> resultados = servicio.getResultadosCarrera(id);
        if (resultados.isEmpty()) {
            imprimirMensaje("No hay resultados para la carrera: " + carrera.getNombreGp(), "ADVERTENCIA");
            return;
        }

        imprimirEncabezado("RESULTADOS: " + carrera.getNombreGp() + " - " + carrera.getFecha());
        if (carrera.getCircuito() != null) {
            System.out.println("Circuito: " + carrera.getCircuito().getNombre());
            System.out.println(SEPARADOR_FINO);
        }

        System.out.printf("%-5s %-30s %-25s %-10s %-10s %-15s %-20s%n",
                "Pos", "Piloto", "Escuderia", "Puntos", "Vueltas", "Tiempo", "Estado");
        System.out.println(SEPARADOR_FINO);

        for (Resultado r : resultados) {
            String constructor = r.getPiloto().getConstructor() != null ?
                    r.getPiloto().getConstructor().getNombre() : "N/A";
            String estado = r.getRetirado() ? "RETIRADO" : "Finalizado";

            System.out.printf("%-5d %-30s %-25s %-10s %-10s %-15s %-20s%n",
                    r.getPosicionFinal(),
                    truncar(r.getPiloto().getNombre(), 30),
                    truncar(constructor, 25),
                    r.getPuntosObtenidos(),
                    r.getVueltas() != null ? r.getVueltas() : "-",
                    r.getTiempo() != null ? r.getTiempo() : "-",
                    estado);

            if (r.getRetirado() && r.getMotivoRetiro() != null && !r.getMotivoRetiro().isEmpty()) {
                System.out.println("      Motivo: " + r.getMotivoRetiro());
            }
        }

        System.out.println(SEPARADOR_GRUESO);
        System.out.println("Total de clasificados: " + resultados.size());
    }

    private static void mostrarCircuitos(int anio) {
        List<Circuito> circuitos = servicio.getCircuitos(anio);

        if (circuitos.isEmpty()) {
            imprimirMensaje("No hay circuitos registrados para " + anio + ".", "ADVERTENCIA");
            return;
        }

        imprimirEncabezado("CIRCUITOS DEL CALENDARIO " + anio);
        System.out.printf("%-50s %-45s%n", "Nombre del Circuito", "Ubicacion");
        System.out.println(SEPARADOR_FINO);

        for (Circuito c : circuitos) {
            System.out.printf("%-50s %-45s%n",
                    truncar(c.getNombre(), 50),
                    truncar(c.getUbicacion(), 45));
        }

        System.out.println(SEPARADOR_GRUESO);
        System.out.println("Total de circuitos: " + circuitos.size());
    }

    // ============================================================================
    // GESTIÓN DE RESULTADOS 2025
    // ============================================================================

    private static void menuGestionResultados2025() {
        boolean volver = false;

        while (!volver) {
            imprimirEncabezado("GESTION DE RESULTADOS - TEMPORADA 2025");
            System.out.println(ConfiguracionTemporada.getMensajeCongelacion());
            System.out.println(SEPARADOR_FINO);
            System.out.println("1. Ingresar resultados de una carrera");
            System.out.println("2. Ver resultados de una carrera (con detalle)");
            System.out.println("3. Eliminar resultados de una carrera");
            System.out.println("4. Ver carreras disponibles para ingreso");
            System.out.println("0. Volver al menú principal");
            System.out.println(SEPARADOR_FINO);
            System.out.print("Seleccione una opción: ");

            int opcion = leerOpcion();

            switch (opcion) {
                case 1 -> ingresarResultados2025();
                case 2 -> verResultadosDetallados();
                case 3 -> eliminarResultados2025();
                case 4 -> mostrarCarrerasEditables();
                case 0 -> volver = true;
                default -> imprimirMensaje("Opción inválida.", "ERROR");
            }

            if (!volver && opcion != 0) {
                esperarEnter();
            }
        }
    }

    private static void mostrarCarrerasEditables() {
        try {
            List<Carrera> carreras = resultadoService.obtenerCarrerasEditables(2025);

            if (carreras.isEmpty()) {
                imprimirMensaje("No hay carreras disponibles para ingreso manual en 2025.", "ADVERTENCIA");
                System.out.println("Verifique que la temporada 2025 esté inicializada.");
                return;
            }

            imprimirEncabezado("CARRERAS DISPONIBLES PARA INGRESO MANUAL - 2025");
            System.out.printf("%-8s %-40s %-35s %-15s%n", "ID", "Gran Premio", "Circuito", "Fecha");
            System.out.println(SEPARADOR_FINO);

            for (Carrera carrera : carreras) {
                String circuito = carrera.getCircuito() != null ? carrera.getCircuito().getNombre() : "N/A";

                System.out.printf("%-8d %-40s %-35s %-15s%n",
                        carrera.getId(),
                        truncar(carrera.getNombreGp(), 40),
                        truncar(circuito, 35),
                        carrera.getFecha());
            }

            System.out.println(SEPARADOR_GRUESO);
            System.out.println("Total de carreras editables: " + carreras.size());

        } catch (Exception e) {
            imprimirMensaje("Error al obtener carreras: " + e.getMessage(), "ERROR");
        }
    }

    private static void ingresarResultados2025() {
        imprimirEncabezado("INGRESAR RESULTADOS DE CARRERA");

        List<Carrera> carrerasEditables = resultadoService.obtenerCarrerasEditables(2025);

        if (carrerasEditables.isEmpty()) {
            imprimirMensaje("No hay carreras disponibles para ingreso manual.", "ADVERTENCIA");
            return;
        }

        System.out.println("\nCarreras disponibles:");
        for (int i = 0; i < carrerasEditables.size(); i++) {
            Carrera c = carrerasEditables.get(i);
            System.out.printf("%d. %s - %s (ID: %d)%n", i + 1, c.getNombreGp(), c.getFecha(), c.getId());
        }

        System.out.print("\nSeleccione el número de carrera (0 para cancelar): ");
        int seleccion = leerOpcion();

        if (seleccion == 0 || seleccion > carrerasEditables.size()) {
            System.out.println("Operación cancelada.");
            return;
        }

        Carrera carreraSeleccionada = carrerasEditables.get(seleccion - 1);

        System.out.println("\nCarrera seleccionada: " + carreraSeleccionada.getNombreGp());
        System.out.println("Fecha: " + carreraSeleccionada.getFecha());

        List<Resultado> resultadosExistentes = resultadoService.obtenerResultadosCarrera(carreraSeleccionada.getId());

        if (!resultadosExistentes.isEmpty()) {
            System.out.println("\nADVERTENCIA: Esta carrera ya tiene " + resultadosExistentes.size() + " resultados registrados.");
            System.out.print("¿Desea reemplazarlos? (S/N): ");
            String respuesta = leerTexto().toUpperCase();

            if (!respuesta.equals("S")) {
                System.out.println("Operación cancelada.");
                return;
            }
        }

        List<Piloto> pilotos = servicio.getPilotos();

        if (pilotos.isEmpty()) {
            imprimirMensaje("No hay pilotos registrados en el sistema.", "ERROR");
            return;
        }

        System.out.println("\n" + SEPARADOR_FINO);
        System.out.println("INGRESO DE RESULTADOS");
        System.out.println(SEPARADOR_FINO);
        System.out.println("Ingrese los resultados de cada piloto.");
        System.out.println("Para pilotos que no participaron, ingrese posición 0 o presione ENTER.");
        System.out.println(SEPARADOR_FINO);

        List<ResultadoDTO> resultados = new ArrayList<>();

        for (Piloto piloto : pilotos) {
            System.out.println("\n" + piloto.getNombre() + " (#" + piloto.getDorsal() + ")");
            System.out.print("   Posición final (1-20, 0 o ENTER=no participó): ");

            String inputPosicion = leerTexto();

            if (inputPosicion.isEmpty() || inputPosicion.equals("0")) {
                continue;
            }

            try {
                int posicion = Integer.parseInt(inputPosicion);

                if (posicion < 1 || posicion > 20) {
                    System.out.println("   Posición inválida. Piloto omitido.");
                    continue;
                }

                ResultadoDTO dto = new ResultadoDTO(piloto.getId(), posicion);

                System.out.print("   ¿Retirado? (S/N): ");
                String retirado = leerTexto().toUpperCase();
                dto.setRetirado(retirado.equals("S"));

                if (dto.getRetirado()) {
                    System.out.print("   Motivo de retiro: ");
                    dto.setMotivoRetiro(leerTexto());
                } else {
                    if (posicion <= 10) {
                        System.out.print("   ¿Vuelta más rápida? (S/N): ");
                        String vuelaRapida = leerTexto().toUpperCase();
                        dto.setVuelaRapida(vuelaRapida.equals("S"));
                    }
                }

                System.out.print("   Tiempo (opcional, ENTER para omitir): ");
                String tiempo = leerTexto();
                if (!tiempo.isEmpty()) {
                    dto.setTiempo(tiempo);
                }

                resultados.add(dto);
                System.out.println("   Resultado registrado");

            } catch (NumberFormatException e) {
                System.out.println("   Entrada inválida. Piloto omitido.");
            }
        }

        if (resultados.isEmpty()) {
            imprimirMensaje("No se ingresaron resultados.", "ADVERTENCIA");
            return;
        }

        System.out.println("\n" + SEPARADOR_FINO);
        System.out.println("RESUMEN: Se ingresarán resultados de " + resultados.size() + " pilotos");
        System.out.print("¿Confirmar ingreso? (S/N): ");
        String confirmar = leerTexto().toUpperCase();

        if (!confirmar.equals("S")) {
            System.out.println("Operación cancelada.");
            return;
        }

        try {
            int ingresados = resultadoService.ingresarResultadosCarrera(carreraSeleccionada.getId(), resultados);

            imprimirMensaje("Resultados ingresados exitosamente!", "EXITO");
            System.out.println("Total de resultados: " + ingresados);
            System.out.println("\nLas clasificaciones de pilotos y constructores se actualizaron automáticamente.");

        } catch (Exception e) {
            imprimirMensaje("Error al ingresar resultados: " + e.getMessage(), "ERROR");
        }
    }

    private static void verResultadosDetallados() {
        mostrarResultadosCarrera(2025);
    }

    private static void eliminarResultados2025() {
        imprimirEncabezado("ELIMINAR RESULTADOS DE CARRERA");

        System.out.print("Ingrese el ID de la carrera: ");
        int carreraId = leerOpcion();

        if (carreraId <= 0) {
            System.out.println("ID inválido.");
            return;
        }

        try {
            List<Resultado> resultados = resultadoService.obtenerResultadosCarrera((long) carreraId);

            if (resultados.isEmpty()) {
                imprimirMensaje("Esta carrera no tiene resultados registrados.", "ADVERTENCIA");
                return;
            }

            System.out.println("\nADVERTENCIA: Se eliminarán " + resultados.size() + " resultados.");
            System.out.print("¿Está seguro? (S/N): ");
            String confirmar = leerTexto().toUpperCase();

            if (!confirmar.equals("S")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int eliminados = resultadoService.eliminarResultadosCarrera((long) carreraId);

            imprimirMensaje("Se eliminaron " + eliminados + " resultados exitosamente.", "EXITO");

        } catch (IllegalStateException e) {
            imprimirMensaje(e.getMessage(), "ERROR");
        } catch (Exception e) {
            imprimirMensaje("Error al eliminar resultados: " + e.getMessage(), "ERROR");
        }
    }

    // ============================================================================
    // MÉTODOS AUXILIARES
    // ============================================================================

    private static void imprimirEncabezado(String titulo) {
        System.out.println("\n" + SEPARADOR_GRUESO);
        System.out.println(titulo);
        System.out.println(SEPARADOR_GRUESO);
    }

    private static void imprimirMensaje(String mensaje, String tipo) {
        String prefijo = switch (tipo) {
            case "ERROR" -> "[ERROR] ";
            case "ADVERTENCIA" -> "[ADVERTENCIA] ";
            case "EXITO" -> "[EXITO] ";
            case "INFO" -> "[INFO] ";
            default -> "";
        };
        System.out.println("\n" + prefijo + mensaje);
    }

    private static String truncar(String texto, int maxLength) {
        if (texto == null || texto.length() <= maxLength) {
            return texto;
        }
        return texto.substring(0, maxLength - 2) + "..";
    }

    private static int leerOpcion() {
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1;
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            imprimirMensaje("Entrada inválida. Ingrese un número.", "ADVERTENCIA");
            return -1;
        }
    }

    private static String leerTexto() {
        return scanner.nextLine().trim();
    }

    private static void esperarEnter() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
    }
}