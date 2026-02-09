package org.arbol;

import org.arbol.logic.nodes.NodeElement;
import org.arbol.logic.tree.Tree;
import org.arbol.logic.tree.TreeB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Inicio de la aplicación");
        Tree<Integer, String> tree = new TreeB<>(5);
        tree.insert(new NodeElement<>(15, "xd"));
        tree.insert(new NodeElement<>(12, "xd"));
        tree.insert(new NodeElement<>(54, "xd"));
        tree.insert(new NodeElement<>(5, "xd"));
        tree.insert(new NodeElement<>(0, "xd"));
        logger.info("Info de arbol antes de insertar 98: {}", tree);
        tree.insert(new NodeElement<>(98, "xd"));
        tree.insert(new NodeElement<>(153, "xd"));
        tree.insert(new NodeElement<>(265, "xd"));
        tree.insert(new NodeElement<>(784, "xd"));
        tree.insert(new NodeElement<>(996, "xd"));
        logger.info("Arbol final: {}", tree);


    }
}
