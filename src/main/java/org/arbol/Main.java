package org.arbol;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.utils.Result;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.shared.Tree;
import org.arbol.logic.Btree.tree.TreeB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * <h2>Arbol B</h2>
 * <p> Sea N el grado del arbol</p>
 * <h3>Reglas: </h3>
 * <ul>
 *    <li>Los nodos tienen un maximo de claves = n-1 elementos</li>
 *    <li>Exceptuando la raiz cada nodo tiene como minimo (n-1)/2 elementos</li>
 *    <li>Tiene como mínimo 2 hijos y como máximo n</li>
 * <ul>
 *
 * @author Henry
 * @version 1.0
 */


public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Inicio de la aplicación");

        Tree<Integer, String> tree = new TreeB<>(3);

        boolean running = true;
        while (running) {
            System.out.println("\n===== ÁRBOL B =====");
            System.out.println("1. Agregar elemento");
            System.out.println("2. Eliminar elemento");
            System.out.println("3. Buscar elemento");
            System.out.println("4. Ver árbol");
            System.out.println("5. Salir");
            System.out.print("Selecciona una opción: ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1" -> agregarElemento(tree);
                case "2" -> eliminarElemento(tree);
                case "3" -> buscarElemento(tree);
                case "4" -> verArbol(tree);
                case "5" -> {
                    running = false;
                    logger.info("Árbol final:\n{}", tree);
                }
                default -> System.out.println("Opción inválida. Intenta de nuevo.");
            }
        }
    }

    private static void agregarElemento(Tree<Integer, String> tree) {
        System.out.print("Ingresa la clave a agregar: ");
        try {
            int key = Integer.parseInt(sc.nextLine().trim());
            var result = tree.insert(new NodeElement<>(key, "v: " + key));

            if (result.isSuccess()) {
                System.out.println("✓ Elemento agregado exitosamente");
                logger.info("Elemento {} insertado en el árbol", key);
            } else {
                if (result instanceof Result.Failure<?, ?>(Object error)) {
                    System.out.println("✗ Error: " + ((NodeError) error).getMessage());
                }
            }
            verArbol(tree);
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
                logger.info("Elemento {} eliminado del árbol", key);
            } else {
                if (result instanceof Result.Failure<?, ?> failure) {
                    System.out.println("✗ Error: " + ((NodeError) failure.error()).getMessage());
                }
            }
            verArbol(tree);
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
                logger.info("Elemento {} encontrado en el árbol", key);
            } else {
                if (result instanceof Result.Failure<?, ?>(Object error)) {
                    System.out.println("✗ Error: " + ((NodeError) error).getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Error: Debes ingresar un número válido");
            logger.error("Error en entrada: {}", e.getMessage());
        }
    }

    private static void verArbol(Tree<Integer, String> tree) {
        System.out.println("\n--- Estado del árbol ---");
        System.out.println(tree);
    }
}

