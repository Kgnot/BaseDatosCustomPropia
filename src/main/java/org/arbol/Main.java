package org.arbol;

import org.arbol.bussines.StopQuery;
import org.arbol.database.Database;
import org.arbol.database.loader.CsvLoader;
import org.arbol.database.models.Route;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.Trips;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.table.Table;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        // cargo toda mi base de datos
        Database db = new Database();
        logger.info("Parada portal Usme");
        Table<String, Stop> stopsTable = db.getTable("stops");
        List<Stop> found = stopsTable.findByField("stopName", "AV. Caracas - CL 65 Sur Portal Usme");
        // Z_53252_STOP,010A12,AV. Caracas - CL 65 Sur Portal Usme,4.5313231334599733,-74.118934559484245,0,,0
        System.out.println("Parada encontrada: " + found);
        System.out.println(
                stopsTable.select("Z_53252_STOP")
        );
        // aqui el join complejo:
        logger.info("Calculando paradas activas : ");
        StopQuery query = new StopQuery(db);

        List<Stop> activeStops = query.findActiveStops();
        logger.info("Paradas activas encontradas: {}", activeStops.size());

    }

    private static Database resetearData(Database currentDb) {
        System.out.print("Esto borrará data/stops.dat, routes.dat, trips.dat y stop_times.dat. ¿Continuar? (s/n): ");
        String confirm = sc.nextLine().trim();
        if (!"s".equalsIgnoreCase(confirm)) {
            System.out.println("Operación cancelada.");
            return currentDb;
        }

        currentDb.close();

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

    private static void buscarRoute(Database db) {
        Table<String, Route> routesTable = db.getTable("routes");

        System.out.print("Ingresa route_id a buscar: ");
        String routeId = sc.nextLine();

        Result<Route, NodeError.NodeNotFoundError> result = routesTable.select(routeId);

        if (result.isSuccess()) {
            Route route = result.unwrap();
            System.out.println("Encontrado: " + route);
        } else {
            System.out.println("Route no encontrada.");
        }
    }

    private static void buscarTrip(Database db) {
        Table<String, Trips> tripsTable = db.getTable("trips");

        System.out.print("Ingresa trip_id a buscar: ");
        String tripId = sc.nextLine();

        Result<Trips, NodeError.NodeNotFoundError> result = tripsTable.select(tripId);

        if (result.isSuccess()) {
            Trips trip = result.unwrap();
            System.out.println("Encontrado: " + trip);
        } else {
            System.out.println("Trip no encontrado.");
        }
    }

    private static void buscarStopTime(Database db) {
        Table<StopTimesKey, StopTimes> stopTimesTable = db.getTable("stop_times");

        System.out.print("Ingresa trip_id: ");
        String tripId = sc.nextLine();
        System.out.print("Ingresa stop_sequence: ");
        String sequence = sc.nextLine();

        try {
            StopTimesKey key = StopTimesKey.from(tripId, sequence);
            Result<StopTimes, NodeError.NodeNotFoundError> result = stopTimesTable.select(key);

            if (result.isSuccess()) {
                StopTimes stopTime = result.unwrap();
                System.out.println("Encontrado: " + stopTime);
            } else {
                System.out.println("StopTime no encontrado.");
            }
        } catch (NumberFormatException e) {
            System.out.println("stop_sequence debe ser un número válido.");
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