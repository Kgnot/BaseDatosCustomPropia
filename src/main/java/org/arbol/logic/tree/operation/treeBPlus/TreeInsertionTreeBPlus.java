package org.arbol.logic.tree.operation.treeBPlus;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.*;
import org.arbol.logic.tree.operation.interace.TreeInsertion;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeInsertionTreeBPlus<K extends Comparable<K>, V> implements TreeInsertion<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeInsertionTreeBPlus.class);

    @Override
    public Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute(Node<K, V> root, NodeElement<K, V> element, int maxSize) {
        // Llamamos al método recursivo auxiliar
        return insertRecursive(root, element, maxSize);
    }

    private Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertRecursive(Node<K, V> node, NodeElement<K, V> element, int maxSize) {

        // CASO 1: Somos un Nodo Hoja
        if (node instanceof BPlusLeafNode<K, V> leaf) {
            // Verificar duplicados (En B+ los datos están solo en las hojas)
            if (leaf.containsKey(element.key())) {
                return new Result.Failure<>(new NodeError.DuplicateKeyError(element.key()));
            }
            // Insertar en orden
            leaf.insertInOrder(element);
            // Verificar si hay overflow (Split)
            if (leaf.isFull()) {
                logger.debug("Hoja llena, haciendo split...");
                return new Result.Success<>(leaf.split());
            }
            // Inserción exitosa sin split
            return new Result.Success<>(new SplitResult<>(null, null));
        }
        // CASO 2: Somos un Nodo Interno
        else if (node instanceof BPlusInternalNode<K, V> internal) {
            // Encontrar por qué hijo bajar
            int index = node.findPosition(element.key());
            Node<K, V> child = internal.getChild(index);
            // Llamada recursiva al hijo
            Result<SplitResult<K, V>, NodeError.DuplicateKeyError> childResult = insertRecursive(child, element, maxSize);
            // Si falla (duplicado), propagamos el error
            if (childResult.isFailure()) {
                return childResult;
            }

            SplitResult<K, V> splitResult = childResult.unwrap();

            // Si el hijo hizo split (newNode no es null), debemos insertar la clave promovida en este nodo
            if (splitResult.newNode() != null) {
                // Insertar la clave promocionada
                internal.addElementAt(index, splitResult.promotedElement());

                // Insertar el nuevo hijo a la derecha de la clave
                internal.insertChildAt(index + 1, splitResult.newNode());

                // Verificar si este nodo interno ahora se llenó
                if (internal.isFull()) {
                    logger.debug("Nodo interno lleno, haciendo split...");
                    return new Result.Success<>(internal.split());
                }
            }

            // Exito
            return new Result.Success<>(new SplitResult<>(null, null));
        }

        // No debería llegar aquí
        return new Result.Failure<>(new NodeError.DuplicateKeyError(element.key()));
    }
}
