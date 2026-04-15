package org.arbol.logic.Btree.tree.operation.interace;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.utils.Result;

public interface TreeDelete<K extends Comparable<K>, V> {

    Result<Void, NodeError.NodeNotFoundError> execute
            (Node<K, V> root, K key, int maxSize);


}
