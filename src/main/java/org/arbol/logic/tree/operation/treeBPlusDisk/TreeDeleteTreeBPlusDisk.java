package org.arbol.logic.tree.operation.treeBPlusDisk;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.Node;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.utils.Result;

import java.io.Serializable;

public class TreeDeleteTreeBPlusDisk<K extends Comparable<K> & Serializable, V extends Serializable>
        implements TreeDelete<K, V> {

    @Override
    public Result<Void, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key, int maxSize) {
        return new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }
}

