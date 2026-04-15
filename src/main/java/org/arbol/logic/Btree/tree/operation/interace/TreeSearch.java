package org.arbol.logic.Btree.tree.operation.interace;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.structures.SplitResult;
import org.arbol.logic.utils.Result;

public interface TreeSearch<K extends Comparable<K>, V> {

    Result<NodeElement<K, V>, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key);

}
