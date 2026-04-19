package org.arbol;

import org.arbol.bussines.StopQuery;
import org.arbol.database.Database;
import org.arbol.database.models.Stop;
import org.arbol.logic.structures.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        System.out.println("Primeras 10 paradas activas: " + activeStops.subList(0, Math.min(10, activeStops.size())));

    }
}