package org.arbol.logic.tree.operation.treeB;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.utils.Result;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;

import java.io.Serializable;

public class TreeSearchTreeB<K extends Comparable<K> & Serializable, V extends Serializable> implements TreeSearch<K, V> {


    public Result<NodeElement<K,V>, NodeError.NodeNotFoundError> execute(Node<K,V> root,K key) {
        // implementar la logica aqui jiji
        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}