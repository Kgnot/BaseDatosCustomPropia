package org.arbol;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.NodeElement;
import org.arbol.logic.structures.Tree;
import org.arbol.logic.tree.TreeBPlusDisk;
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
        logger.info("Inicio de la aplicación Árbol B+");

        Tree<Integer, String> tree = new TreeBPlusDisk<>("bplus-main", 4);

        boolean running = true;
        while (running) {
            System.out.println("\n===== ÁRBOL B+ (MENU PRUEBAS) =====");
            System.out.println("1. Agregar elemento (Insert)");
            System.out.println("2. Eliminar elemento (Delete)");
            System.out.println("3. Buscar elemento (Search)");
            System.out.println("4. Ver estructura completa (Tree View)");
            System.out.println("5. Ver hojas secuencialmente (Leaf Scan) <- Único de B+");
            System.out.println("6. Salir");
            System.out.print("Selecciona una opción: ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1" -> agregarElemento(tree);
                case "2" -> eliminarElemento(tree);
                case "3" -> buscarElemento(tree);
                case "4" -> verArbol(tree);
                case "5" -> verHojasSecuenciales();
                case "6" -> {
                    running = false;
                    logger.info("Árbol B+ final:\n{}", tree);
                    System.out.println("Saliendo...");
                }
                default -> System.out.println("Opción inválida. Intenta de nuevo.");
            }
        }
    }

    private static void agregarElemento(Tree<Integer, String> tree) {
        System.out.print("Ingresa la clave a agregar: ");
        try {
            int key = Integer.parseInt(sc.nextLine().trim());
            // Creamos el elemento. El valor es dummy "v: key" para probar
            var result = tree.insert(new NodeElement<>(key, "Valor para " + key));

            if (result.isSuccess()) {
                System.out.println("✓ Elemento agregado exitosamente");
                logger.info("Elemento {} insertado en el árbol B+", key);
            } else {
                if (result instanceof Result.Failure<?, ?> failure) {
                    System.out.println("✗ Error: " + ((NodeError) failure.error()).getMessage());
                }
            }
            // Opcional: Mostrar árbol automático después de insertar
            // verArbol(tree);
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Debes ingresar un número válido");
            logger.error("Error en entrada: {}", e.getMessage());
        }
    }

    private static void eliminarElemento(Tree<Integer, String> tree) {
        System.out.print("Ingresa la clave a eliminar: ");
        try {
            int key = Integer.parseInt(sc.nextLine().trim());
            var result = tree.delete(key);

            if (result.isSuccess()) {
                System.out.println("✓ Elemento eliminado exitosamente");
                logger.info("Elemento {} eliminado del árbol B+", key);
            } else {
                if (result instanceof Result.Failure<?, ?> failure) {
                    System.out.println("✗ Error: " + ((NodeError) failure.error()).getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Debes ingresar un número válido");
            logger.error("Error en entrada: {}", e.getMessage());
        }
    }

    private static void buscarElemento(Tree<Integer, String> tree) {
        System.out.print("Ingresa la clave a buscar: ");
        try {
            int key = Integer.parseInt(sc.nextLine().trim());
            var result = tree.search(key);

            if (result.isSuccess()) {
                var element = result.unwrap();
                System.out.println("✓ Elemento encontrado: Clave=" + element.key() + ", Valor=" + element.value());
                logger.info("Elemento {} encontrado en el árbol B+", key);
            } else {
                if (result instanceof Result.Failure<?, ?> failure) {
                    System.out.println("✗ Error: " + ((NodeError) failure.error()).getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Debes ingresar un número válido");
            logger.error("Error en entrada: {}", e.getMessage());
        }
    }

    private static void verArbol(Tree<Integer, String> tree) {
        System.out.println("\n--- Estructura Jerárquica (Niveles) ---");
        System.out.println(tree);
    }

    /**
     * Método específico para probar la lista enlazada de hojas del B+ Tree.
     * Demuestra la capacidad de recorrido secuencial optimizado.
     */
    private static void verHojasSecuenciales() {
        System.out.println("\n--- Recorrido Secuencial de Hojas (B+ Feature) ---");
        System.out.println("En modo disco esta vista no está implementada aún.");
    }
}