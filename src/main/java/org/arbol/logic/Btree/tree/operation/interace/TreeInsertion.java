package org.arbol.logic.Btree.tree.operation.interace;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.structures.SplitResult;
import org.arbol.logic.utils.Result;

public interface TreeInsertion<K extends Comparable<K>, V> {

    Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute
            (Node<K, V> root, NodeElement<K, V> element, int maxSize);

}
