package org.arbol.logic.tree.operation;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;
import org.arbol.logic.nodes.Node;

public class TreeDelete<K extends Comparable<K>,V> {


    public Result<Void, NodeError.NodeNotFoundError> delete(Node<K,V> root, K key) {
        // implementar la logica aqui jiji
        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}
