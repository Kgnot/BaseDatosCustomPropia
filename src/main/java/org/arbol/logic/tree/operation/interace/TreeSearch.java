package org.arbol.logic.tree.operation.interace;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.Node;
import org.arbol.logic.structures.NodeElement;
import org.arbol.utils.Result;

import java.io.Serializable;

public interface TreeSearch<K extends Comparable<K> & Serializable, V extends Serializable> {

    Result<NodeElement<K, V>, NodeError.NodeNotFoundError> execute(Node<K, V> root, K key);

}
