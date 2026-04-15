package org.arbol.logic.Btree.tree.operation.treeBPlus;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.tree.operation.interace.TreeSearch;
import org.arbol.logic.utils.Result;

public class TreeSearchTreeBPlus<K extends Comparable<K>, V> implements TreeSearch<K, V> {


    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key) {
        // implementar la logica aqui jiji
        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}