package org.arbol;

import org.arbol.bussines.StopQuery;
import org.arbol.bussines.TripStopEdge;
import org.arbol.bussines.StopTransition;
import org.arbol.database.Database;
import org.arbol.database.loader.CsvLoader;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.structures.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <h2>Pruebas Árbol B+</h2>
 * <p> Cliente de prueba para la estructura de datos B+ Tree. </p>
 *
 * @author Henry
 * @version 1.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Inicio de la Base de Datos B+ (Simulando SITP)");
        // cargo toda mi base de datos
        Database db = new Database();
        // cargamos los datos
//        cargarDatos(db);
//        logger.info("Parada portal Usme");
//        Table<String, Stop> stopsTable = db.getTable("stops");
//        List<Stop> found = stopsTable.findByField("stopName", "AV. Caracas - CL 65 Sur Portal Usme");
//        // Z_53252_STOP,010A12,AV. Caracas - CL 65 Sur Portal Usme,4.5313231334599733,-74.118934559484245,0,,0
//        System.out.println("Parada encontrada: " + found);
//        System.out.println(
//                stopsTable.select("Z_53252_STOP")
//        );
//        // aqui el join complejo:
//        logger.info("Calculando paradas activas : ");
//        StopQuery query = new StopQuery(db);
//
//        List<Stop> activeStops = query.findActiveStops();
//        logger.info("Paradas activas encontradas: {}", activeStops.size());
//        System.out.println("Primeras 10 paradas activas: " + activeStops.subList(0, Math.min(10, activeStops.size())));
//
//        List<TripStopEdge> edges = query.findConsecutiveStopEdges();
//        logger.info("Aristas consecutivas encontradas: {}", edges.size());
//        System.out.println("Primeras 10 aristas: " + edges.subList(0, Math.min(10, edges.size())));
//
//        List<StopTransition> grouped = query.findGroupedConsecutiveStopEdges();
//        logger.info("Pares agregados encontrados: {}", grouped.size());
//        System.out.println("Primeras 10 transiciones agrupadas: " + grouped.subList(0, Math.min(10, grouped.size())));

        Table<StopTimesKey, StopTimes> stopTimes = db.getTable("stop_times");
        logger.info("Paradas encontradas: {}", stopTimes.findAll().subList(0,20));

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
        loader.loadStopTimes(basePath + "stop_times.txt");


        System.out.println("Carga finalizada.");
    }
}