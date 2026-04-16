package org.arbol;

import org.arbol.database.Database;
import org.arbol.database.models.Stop;
import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.table.Table;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            System.out.println("3. Listar todas las paradas (Range Scan - Avanzado)");
            System.out.println("4. Salir");
            System.out.print("Opción: ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1" -> insertarStop(db);
                case "2" -> buscarStop(db);
                case "3" -> System.out.println("Función de range scan pendiente de implementar ;)");
                case "4" -> {
                    running = false;
                    System.out.println("Guardando y cerrando base de datos...");
                }
                default -> System.out.println("Opción inválida");
            }
        }
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
}