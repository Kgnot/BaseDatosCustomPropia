package org.arbol.logic.tree.operation.interace;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.Node;
import org.arbol.utils.Result;

import java.io.Serializable;

public interface TreeDelete<K extends Comparable<K> & Serializable, V extends Serializable> {

    Result<Void, NodeError.NodeNotFoundError> execute
            (Node<K, V> root, K key, int maxSize);


}
