package org.arbol.logic.tree.operation;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;
import org.arbol.logic.nodes.Node;
import org.arbol.logic.nodes.NodeElement;

public class TreeSearch<K extends Comparable<K>,V> {


    public Result<NodeElement<K,V>, NodeError.NodeNotFoundError> search(Node<K,V> root,K key) {
        // implementar la logica aqui jiji
        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}