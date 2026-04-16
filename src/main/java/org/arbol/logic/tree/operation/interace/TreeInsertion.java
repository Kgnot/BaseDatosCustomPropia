package org.arbol.logic.tree.operation.interace;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.structures.node.SplitResult;
import org.arbol.utils.Result;

import java.io.Serializable;

public interface TreeInsertion<K extends Comparable<K> & Serializable, V extends Serializable> {

    Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute
            (Node<K, V> root, NodeElement<K, V> element, int maxSize);

}
