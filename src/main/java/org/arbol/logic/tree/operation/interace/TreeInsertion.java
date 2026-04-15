package org.arbol.logic.tree.operation.interace;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.Node;
import org.arbol.logic.structures.NodeElement;
import org.arbol.logic.structures.SplitResult;
import org.arbol.utils.Result;

public interface TreeInsertion<K extends Comparable<K>, V> {

    Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute
            (Node<K, V> root, NodeElement<K, V> element, int maxSize);

}
