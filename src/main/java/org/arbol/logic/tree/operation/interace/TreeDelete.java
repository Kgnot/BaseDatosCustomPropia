package org.arbol.logic.tree.operation.interace;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.Node;
import org.arbol.utils.Result;

public interface TreeDelete<K extends Comparable<K>, V> {

    Result<Void, NodeError.NodeNotFoundError> execute
            (Node<K, V> root, K key, int maxSize);


}
