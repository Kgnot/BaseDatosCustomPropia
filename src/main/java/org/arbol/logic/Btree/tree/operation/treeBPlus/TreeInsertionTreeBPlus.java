package org.arbol.logic.Btree.tree.operation.treeBPlus;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.InternalNode;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.structures.SplitResult;
import org.arbol.logic.Btree.tree.operation.interace.TreeInsertion;
import org.arbol.logic.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeInsertionTreeBPlus<K extends Comparable<K>, V> implements TreeInsertion<K, V> {

    private final Logger logger = LoggerFactory.getLogger(TreeInsertionTreeBPlus.class);

    public Result<SplitResult<K, V>, NodeError.DuplicateKeyError> execute
            (Node<K, V> root, NodeElement<K, V> element, int maxSize) {
        //     return insertRecursive(root, element, maxSize);
        return null;
    }

}
