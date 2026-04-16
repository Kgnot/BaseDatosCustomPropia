package org.arbol;

import org.arbol.database.Database;
import org.arbol.database.loader.CsvLoader;
import org.arbol.database.models.Stop;
import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.table.Table;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * <h2>Pruebas Árbol B+</h2>
 * <p> Cliente de prueba para la estructura de datos B+ Tree. </p>
 *
 * @author Henry
 * @version 1.0
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Inicio de la Base de Datos B+ (Simulando SITP)");

        // 1. Cargar el esquema de la base de datos
        // Esto automáticamente buscará/creará data/stops.dat y data/routes.dat
        Database db = new Database();

        boolean running = true;
        while (running) {
            System.out.println("\n===== MENU SISTEMA SITP (Bogotá) =====");
            System.out.println("1. Agregar Parada (Stops Table)");
            System.out.println("2. Buscar Parada por ID");
            System.out.println("3. Listar todas las paradas");
            System.out.println("4. Listar paradas paginado");
            System.out.println("5. Cargar datos");
            System.out.println("6. Resetear archivos de data (.dat)");
            System.out.println("7. Salir");
            System.out.print("Opción: ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1" -> insertarStop(db);
                case "2" -> buscarStop(db);
                case "3" -> listarTodasLasParadas(db);
                case "4" -> listarParadasPaginado(db);
                case "5" -> cargarDatos(db);
                case "6" -> db = resetearData(db);
                case "7" -> {
                    running = false;
                    System.out.println("Saliendo...");
                }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private static Database resetearData(Database currentDb) {
        System.out.print("Esto borrará data/stops.dat, routes.dat, trips.dat y stop_times.dat. ¿Continuar? (s/n): ");
        String confirm = sc.nextLine().trim();
        if (!"s".equalsIgnoreCase(confirm)) {
            System.out.println("Operación cancelada.");
            return currentDb;
        }

        String[] files = {"stops.dat", "routes.dat", "trips.dat", "stop_times.dat"};
        int removed = 0;

        for (String file : files) {
            Path path = Path.of("data", file);
            try {
                if (Files.deleteIfExists(path)) {
                    removed++;
                }
            } catch (IOException e) {
                logger.error("No se pudo borrar {}", path, e);
                System.out.println("No se pudo borrar: " + path);
            }
        }

        System.out.println("Archivos borrados: " + removed);
        System.out.println("Recargando base de datos vacía...");
        return new Database();
    }

    private static void insertarStop(Database db) {
        // Obtenemos la tabla stops
        Table<String, Stop> stopsTable = db.getTable("stops");

        System.out.print("ID Parada (ej. ST001): ");
        String id = sc.nextLine();
        System.out.print("Nombre Parada (ej. Portal Norte): ");
        String name = sc.nextLine();
        System.out.print("Latitud: ");
        double lat = Double.parseDouble(sc.nextLine());
        System.out.print("Longitud: ");
        double lon = Double.parseDouble(sc.nextLine());

        Stop stop = new Stop(id, "CODE_" + id, name, lat, lon);

        // Insertar en el B+ Tree de disco
        Result<Void, NodeError.DuplicateKeyError> result = stopsTable.insert(id, stop);

        if (result.isSuccess()) {
            System.out.println("✓ Parada guardada en disco en 'stops.dat'");
        } else {
            System.out.println("✗ Error: ID duplicado");
        }
    }

    private static void buscarStop(Database db) {
        Table<String, Stop> stopsTable = db.getTable("stops");

        System.out.print("Ingresa ID a buscar: ");
        String id = sc.nextLine();

        Result<Stop, NodeError.NodeNotFoundError> result = stopsTable.select(id);

        if (result.isSuccess()) {
            Stop s = result.unwrap();
            System.out.println("Encontrado: " + s);
        } else {
            System.out.println("No encontrada.");
        }
    }

    private static void listarTodasLasParadas(Database db) {
        Table<String, Stop> stopsTable = db.getTable("stops");
        var stops = stopsTable.findAll();

        if (stops.isEmpty()) {
            System.out.println("No hay paradas registradas.");
            return;
        }

        System.out.println("\n--- Todas las paradas ---");
        for (int i = 0; i < stops.size(); i++) {
            System.out.println((i + 1) + ". " + stops.get(i));
        }
        System.out.println("Total: " + stops.size());
    }

    private static void listarParadasPaginado(Database db) {
        Table<String, Stop> stopsTable = db.getTable("stops");

        try {
            System.out.print("Offset (desde qué registro): ");
            int offset = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Límite (cuántos mostrar): ");
            int limit = Integer.parseInt(sc.nextLine().trim());

            if (offset < 0 || limit < 0) {
                System.out.println("Offset y límite deben ser >= 0.");
                return;
            }

            var stops = stopsTable.findAll(offset, limit);
            if (stops.isEmpty()) {
                System.out.println("No hay resultados para ese rango.");
                return;
            }

            System.out.println("\n--- Paradas paginadas (offset=" + offset + ", limit=" + limit + ") ---");
            for (int i = 0; i < stops.size(); i++) {
                System.out.println((offset + i + 1) + ". " + stops.get(i));
            }
            System.out.println("Mostradas: " + stops.size());

        } catch (NumberFormatException e) {
            System.out.println("Debes ingresar números enteros válidos para offset/limit.");
        }
    }

    private static void cargarDatos(Database db) {
        CsvLoader loader = new CsvLoader(db);
        System.out.println("Iniciando carga de datos GTFS...");

        // Asegúrate de que los archivos existan en esta ruta relativa
        String basePath = "gtfs_data/";

        // Cargamos primero las tablas pequeñas para probar
        loader.loadRoutes(basePath + "routes.txt");
        loader.loadStops(basePath + "stops.txt");
        loader.loadTrips(basePath + "trips.txt");

        System.out.println("\n¿Deseas cargar StopTimes? (Pesado: 333MB - Puede tardar 30min+) (s/n)");
        String confirm = sc.nextLine();
        if ("s".equalsIgnoreCase(confirm)) {
            loader.loadStopTimes(basePath + "stop_times.txt");
        }

        System.out.println("Carga finalizada.");
    }
}