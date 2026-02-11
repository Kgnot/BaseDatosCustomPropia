package org.arbol.logic.tree.operation;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;
import org.arbol.logic.nodes.InternalNode;
import org.arbol.logic.nodes.Node;
import org.arbol.logic.nodes.NodeElement;
import org.arbol.logic.nodes.SplitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeInsertion<K extends Comparable<K>, V> {

    private final Logger logger = LoggerFactory.getLogger(TreeInsertion.class);

    public Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insert
            (Node<K, V> root, NodeElement<K, V> element, int maxSize) {
        return insertRecursive(root, element, maxSize);
    }

    private Result<SplitResult<K, V>, NodeError.DuplicateKeyError>
    insertRecursive(
            Node<K, V> node,
            NodeElement<K, V> element,
            int maxSize
    ) {
        if (node instanceof InternalNode<K, V> internal && internal.hasChildren()) {
            int index = internal.findChildIndex(element.key());
            logger.debug("El nodo interno es : {} ", internal);
            logger.debug("El índice del hijo a seguir es: {} ", index);
            Node<K, V> child = internal.getChild(index);

            var result = insertRecursive(child, element, maxSize);

            if (result.isFailure()) return result;

            SplitResult<K, V> split = result.unwrap();

            if (split.newNode() != null) {
                // el hijo se ha dividido, hay que insertar el nuevo nodo y el elemento promovido en el nodo actual
                internal.insertElementAt(index, split.promotedElement());
                internal.insertChildAt(index + 1, split.newNode());


                if (internal.isFull()) {
                    // el nodo actual se ha llenado, hay que dividirlo
                    return new Result.Success<>(internal.split());
                }
            }
            return new Result.Success<>(new SplitResult<>(null, null));
        }
        // el tema si no tiene hijos:
        logger.debug("El nodo hoja es: {} ", node);
        if (node.containsKey(element.key())) {
            return new Result.Failure<>(new NodeError.DuplicateKeyError(element.key()));
        }

        node.insertInOrder(element);

        if (node.isFull()) {
            // si se llena se divíde
            return new Result.Success<>(node.split());
        }
        // como es hoja no hizo split, entonces devolvemos null
        return new Result.Success<>(new SplitResult<>(null, null));
    }
}
